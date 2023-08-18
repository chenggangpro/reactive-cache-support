package pro.chenggang.project.reactive.cache.support.defaults.executor;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pro.chenggang.project.reactive.cache.support.core.ReactiveCacheLock;
import pro.chenggang.project.reactive.cache.support.core.adapter.ReactiveCacheMonoAdapter;
import pro.chenggang.project.reactive.cache.support.core.executor.ReactiveMonoCache;
import pro.chenggang.project.reactive.cache.support.exception.NoSuchCachedReactiveDataException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The default reactive mono cache
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultReactiveMonoCache implements ReactiveMonoCache {

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
     * The reactive mono adapter
     */
    @NonNull
    private final ReactiveCacheMonoAdapter reactiveCacheMonoAdapter;

    @Override
    public <T> Mono<T> get(@NonNull String cacheKey) {
        return reactiveCacheLock.checkInitializeLock(cacheName, cacheKey, maxWaitingDuration)
                .then(reactiveCacheMonoAdapter.hasData(cacheKey))
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("[Reactive Cache](Get-Mono)Cached data didn't exist, " +
                            "return no such cached data exception instead of Mono.empty()"
                    );
                    return Mono.error(new NoSuchCachedReactiveDataException(cacheName, cacheKey));
                }))
                .flatMap(hasData -> reactiveCacheMonoAdapter.loadData(cacheKey));
    }

    @Override
    public <T> Mono<T> cacheIfNecessary(@NonNull String cacheKey,
                                        @NonNull Duration cacheDuration,
                                        @NonNull Mono<T> sourceMono) {
        return reactiveCacheLock.checkInitializeLock(cacheName, cacheKey, maxWaitingDuration)
                .then(reactiveCacheMonoAdapter.hasData(cacheKey))
                .flatMap(hasData -> {
                    if (hasData) {
                        log.debug(
                                "[Reactive Cache](Mono)Cached data exist, return the cached data, " +
                                        "CacheName:{}, CacheKey:{}",
                                cacheName,
                                cacheKey
                        );
                        return reactiveCacheMonoAdapter.loadData(cacheKey);
                    }
                    return Mono.usingWhen(
                            this.reactiveCacheLock.tryLockInitializeLock(cacheName,
                                    cacheKey,
                                    maxWaitingDuration
                            ),
                            currentOperationId -> reactiveCacheMonoAdapter.<T>loadData(cacheKey)
                                    .switchIfEmpty(Mono.defer(() -> reactiveCacheMonoAdapter.<T>cacheData(
                                                            cacheKey,
                                                            cacheDuration,
                                                            sourceMono
                                                    )
                                                    .onErrorResume(throwable -> reactiveCacheMonoAdapter.cleanupData(
                                                                    cacheKey)
                                                            .then(Mono.error(throwable))
                                                    )
                                    ))
                            ,
                            currentOperationId -> this.reactiveCacheLock.releaseInitializeLock(cacheName, cacheKey)
                                    .doOnNext(operationId -> log.debug(
                                            "[Reactive Cache](Mono)Release initialization lock, CacheName:{}, CacheKey:{}, " +
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
                                            "[Reactive Cache](Mono)Release initialization lock on Error, " +
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
                                            "[Reactive Cache](Mono)Release initialization lock, " +
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
        final AtomicBoolean alreadyReleaseFlag = new AtomicBoolean(false);
        return Mono.usingWhen(
                this.reactiveCacheLock.tryLockInitializeLock(cacheName,
                        cacheKey,
                        maxWaitingDuration
                ),
                currentOperationId -> reactiveCacheMonoAdapter.hasData(cacheKey)
                        .filter(Boolean::booleanValue)
                        .flatMap(hasData -> reactiveCacheMonoAdapter.cleanupData(cacheKey))
                ,
                currentOperationId -> {
                    if (alreadyReleaseFlag.compareAndSet(false, true)) {
                        return this.reactiveCacheLock.releaseInitializeLock(cacheName, cacheKey)
                                .doOnNext(operationId -> log.debug(
                                        "[Reactive Cache](Cleanup)Release initialization lock, CacheName:{}, CacheKey:{}, " +
                                                "ReleasedOperationId:{}, CurrentOperationId:{}",
                                        cacheName,
                                        cacheKey,
                                        operationId,
                                        currentOperationId
                                ))
                                .then();
                    }
                    return Mono.empty();
                },
                (currentOperationId, throwable) -> {
                    if (alreadyReleaseFlag.compareAndSet(false, true)) {
                        return this.reactiveCacheLock.releaseInitializeLock(cacheName, cacheKey)
                                .doOnNext(operationId -> log.debug(
                                        "[Reactive Cache](Cleanup)Release initialization lock on Error, " +
                                                "CacheName:{}, CacheKey:{}, " +
                                                "ReleasedOperationId:{}, CurrentOperationId:{}",
                                        cacheName,
                                        cacheKey,
                                        operationId,
                                        currentOperationId
                                ))
                                .then(Mono.error(throwable));
                    }
                    return Mono.error(throwable);
                },
                (currentOperationId) -> {
                    if (alreadyReleaseFlag.compareAndSet(false, true)) {
                        return this.reactiveCacheLock.releaseInitializeLock(cacheName, cacheKey)
                                .doOnNext(operationId -> log.debug(
                                        "[Reactive Cache](Cleanup)Release initialization lock, " +
                                                "CacheName:{}, CacheKey:{}, " +
                                                "ReleasedOperationId:{}, CurrentOperationId:{}",
                                        cacheName,
                                        cacheKey,
                                        operationId,
                                        currentOperationId
                                ))
                                .then();
                    }
                    return Mono.empty();
                }
        );
    }
}
