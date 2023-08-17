package pro.chenggang.project.reactive.cache.support.defaults.inmemory;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pro.chenggang.project.reactive.cache.support.core.adapter.ReactiveCacheMonoAdapter;
import pro.chenggang.project.reactive.cache.support.toolkit.AutoExpiredDataCache;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * The inmemory reactive cache mono adapter
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class InmemoryReactiveCacheMonoAdapter implements ReactiveCacheMonoAdapter {

    private final AutoExpiredDataCache<Object> monoDataCache = AutoExpiredDataCache.newInstance();

    @Override
    public Mono<Boolean> hasData(@NonNull String cacheKey) {
        return Mono.defer(() -> Mono.fromFuture(CompletableFuture.supplyAsync(() -> monoDataCache.hasData(cacheKey))));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Mono<T> loadData(@NonNull String cacheKey) {
        return Mono.defer(() -> Mono.fromFuture(CompletableFuture.supplyAsync(() -> monoDataCache.getData(cacheKey))))
                .flatMap(optionalData -> (Mono<T>) Mono.justOrEmpty(optionalData));
    }

    @Override
    public <T> Mono<T> cacheData(@NonNull String cacheKey,
                                 @NonNull Duration cacheDuration,
                                 @NonNull Mono<T> sourcePublisher) {
        return sourcePublisher.flatMap(nextData -> Mono.fromFuture(CompletableFuture.runAsync(() -> monoDataCache.putData(
                        cacheKey,
                        nextData,
                        cacheDuration
                )))
                .thenReturn(nextData));
    }

    @Override
    public Mono<Void> cleanupData(@NonNull String cacheKey) {
        return Mono.defer(() -> Mono.fromFuture(CompletableFuture.runAsync(() -> monoDataCache.removeData(cacheKey))));
    }
}
