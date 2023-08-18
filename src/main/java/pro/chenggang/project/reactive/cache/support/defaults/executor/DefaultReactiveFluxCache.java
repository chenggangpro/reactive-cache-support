package pro.chenggang.project.reactive.cache.support.defaults.executor;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pro.chenggang.project.reactive.cache.support.core.ReactiveCacheLock;
import pro.chenggang.project.reactive.cache.support.core.adapter.ReactiveCacheFluxAdapter;
import pro.chenggang.project.reactive.cache.support.core.executor.ReactiveFluxCache;
import pro.chenggang.project.reactive.cache.support.exception.NoSuchCachedReactiveDataException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * The default reactive flux cache
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultReactiveFluxCache implements ReactiveFluxCache {

    /**
     * The Cache name.
     */
    @NonNull
    private final String cacheName;
    /**
     * The Max waiting duration.
     */
    @NonNull
    private final Duration maxWaitingDuration;
    /**
     * The Reactive cache initialize lock.
     */
    @NonNull
    private final ReactiveCacheLock reactiveCacheLock;
    /**
     * The reactive flux adapter
     */
    @NonNull
    private final ReactiveCacheFluxAdapter reactiveCacheFluxAdapter;

    @Override
    public <T> Flux<T> get(@NonNull String cacheKey) {
        return reactiveCacheLock.checkInitializeLock(cacheName, cacheKey, maxWaitingDuration)
                .then(reactiveCacheFluxAdapter.hasData(cacheKey))
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("[Reactive Cache](Get-Flux)Cached data didn't exist, " +
                            "return no such cached data exception instead of Flux.empty()"
                    );
                    return Mono.error(new NoSuchCachedReactiveDataException(cacheName, cacheKey));
                }))
                .flatMapMany(hasData -> reactiveCacheFluxAdapter.loadData(cacheKey));
    }

    @Override
    public <T> Flux<T> cacheIfNecessary(@NonNull String cacheKey,
                                        @NonNull Duration cacheDuration,
                                        @NonNull Flux<T> sourceFlux) {
        return reactiveCacheLock.checkInitializeLock(cacheName, cacheKey, maxWaitingDuration)
                .then(reactiveCacheFluxAdapter.hasData(cacheKey))
                .flatMapMany(hasData -> {
                    if (hasData) {
                        log.debug(
                                "[Reactive Cache](Flux)Cache data exist, return the cached data, " +
                                        "CacheName:{}, CacheKey:{}",
                                cacheName,
                                cacheKey
                        );
                        return reactiveCacheFluxAdapter.loadData(cacheKey);
                    }
                    return Flux.usingWhen(
                            this.reactiveCacheLock.tryLockInitializeLock(cacheName,
                                    cacheKey,
                                    maxWaitingDuration
                            ),
                            currentOperationId -> reactiveCacheFluxAdapter.<T>loadData(cacheKey)
                                    .switchIfEmpty(Flux.defer(() -> reactiveCacheFluxAdapter.<T>cacheData(
                                                            cacheKey,
                                                            cacheDuration,
                                                            sourceFlux
                                                    )
                                                    .onErrorResume(throwable -> reactiveCacheFluxAdapter.cleanupData(
                                                                    cacheKey)
                                                            .then(Mono.error(throwable))
                                                    ))
                                    )
                            ,
                            currentOperationId -> this.reactiveCacheLock.releaseInitializeLock(cacheName, cacheKey)
                                    .doOnNext(operationId -> log.debug(
                                            "[Reactive Cache](Flux)Release initialization lock, CacheName:{}, CacheKey:{}, " +
                                                    "ReleasedOperationId:{}, CurrentOperationId:{}",
                                            cacheName,
                                            cacheKey,
                                            operationId,
                                            currentOperationId
                                    ))
                                    .then()
                            ,
                            (currentOperationId, throwable) -> this.reactiveCacheLock.releaseInitializeLock(cacheName,
                                            cacheKey
                                    )
                                    .doOnNext(operationId -> log.debug(
                                            "[Reactive Cache](Flux)Release initialization lock on Error, " +
                                                    "CacheName:{}, CacheKey:{}, " +
                                                    "ReleasedOperationId:{}, CurrentOperationId:{}",
                                            cacheName,
                                            cacheKey,
                                            operationId,
                                            currentOperationId
                                    ))
                                    .then()
                            ,
                            (currentOperationId) -> this.reactiveCacheLock.releaseInitializeLock(cacheName, cacheKey)
                                    .doOnNext(operationId -> log.debug(
                                            "[Reactive Cache](Flux)Release initialization lock, " +
                                                    "CacheName:{}, CacheKey:{}, " +
                                                    "ReleasedOperationId:{}, CurrentOperationId:{}",
                                            cacheName,
                                            cacheKey,
                                            operationId,
                                            currentOperationId
                                    ))
                                    .then()
                    );
                });
    }

    @Override
    public Mono<Void> evictCache(@NonNull String cacheKey) {
        return Mono.usingWhen(
                this.reactiveCacheLock.tryLockInitializeLock(cacheName,
                        cacheKey,
                        maxWaitingDuration
                ),
                currentOperationId -> reactiveCacheFluxAdapter.hasData(cacheKey)
                        .filter(Boolean::booleanValue)
                        .flatMap(hasData -> reactiveCacheFluxAdapter.cleanupData(cacheKey))
                ,
                currentOperationId -> this.reactiveCacheLock.releaseInitializeLock(cacheName, cacheKey)
                        .doOnNext(operationId -> log.debug(
                                "[Reactive Cache](Cleanup)Release initialization lock, CacheName:{}, CacheKey:{}, " +
                                        "ReleasedOperationId:{}, CurrentOperationId:{}",
                                cacheName,
                                cacheKey,
                                operationId,
                                currentOperationId
                        ))
                        .then()
                ,
                (currentOperationId, throwable) -> this.reactiveCacheLock.releaseInitializeLock(cacheName, cacheKey)
                        .doOnNext(operationId -> log.debug(
                                "[Reactive Cache](Cleanup)Release initialization lock on Error, " +
                                        "CacheName:{}, CacheKey:{}, " +
                                        "ReleasedOperationId:{}, CurrentOperationId:{}",
                                cacheName,
                                cacheKey,
                                operationId,
                                currentOperationId
                        ))
                        .then(Mono.error(throwable))
                ,
                (currentOperationId) -> this.reactiveCacheLock.releaseInitializeLock(cacheName, cacheKey)
                        .doOnNext(operationId -> log.debug(
                                "[Reactive Cache](Cleanup)Release initialization lock, " +
                                        "CacheName:{}, CacheKey:{}, " +
                                        "ReleasedOperationId:{}, CurrentOperationId:{}",
                                cacheName,
                                cacheKey,
                                operationId,
                                currentOperationId
                        ))
                        .then()
        );
    }
}
