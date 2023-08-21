package pro.chenggang.project.reactive.cache.support.defaults.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pro.chenggang.project.reactive.cache.support.core.adapter.ReactiveCacheFluxAdapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The inmemory reactive cache flux adapter by using caffeine
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class CaffeineReactiveCacheFluxAdapter implements ReactiveCacheFluxAdapter {

    private final ConcurrentHashMap<String, Cache<String, ConcurrentLinkedDeque<Object>>> fluxDataCache = new ConcurrentHashMap<>();

    @Override
    public Mono<Boolean> hasData(@NonNull String cacheKey) {
        return Mono.defer(() -> Mono.fromFuture(CompletableFuture.supplyAsync(() ->
                fluxDataCache.containsKey(cacheKey)
                        &&
                        fluxDataCache.get(cacheKey)
                                .asMap()
                                .containsKey(cacheKey)))
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Flux<T> loadData(@NonNull String cacheKey) {
        return Flux.defer(() -> Mono.fromFuture(CompletableFuture.supplyAsync(() ->
                                Optional.ofNullable(fluxDataCache.get(cacheKey)))
                        )
                        .flatMap(Mono::justOrEmpty)
                        .flatMapMany(cache -> Mono.justOrEmpty(cache.getIfPresent(cacheKey))
                                .flatMapMany(cachedData -> (Flux<T>) Flux.fromIterable(cachedData))
                        )
        );
    }

    @Override
    public <T> Flux<T> cacheData(@NonNull String cacheKey,
                                 @NonNull Duration cacheDuration,
                                 @NonNull Flux<T> sourcePublisher) {
        final AtomicBoolean initFlag = new AtomicBoolean(false);
        return Flux.zip(sourcePublisher,
                        sourcePublisher.share()
                                .concatMap(item -> {
                                    if (initFlag.compareAndSet(false, true)) {
                                        return Mono.fromFuture(CompletableFuture.supplyAsync(() -> fluxDataCache.compute(
                                                cacheKey,
                                                (key, value) -> {
                                                    ConcurrentLinkedDeque<Object> data = new ConcurrentLinkedDeque<>();
                                                    data.add(item);
                                                    if (Objects.isNull(value)) {
                                                        Cache<String, ConcurrentLinkedDeque<Object>> cache = Caffeine.newBuilder()
                                                                .expireAfterWrite(cacheDuration)
                                                                .build();
                                                        cache.put(cacheKey, data);
                                                        return cache;
                                                    }
                                                    value.invalidateAll();
                                                    Cache<String, ConcurrentLinkedDeque<Object>> cache = Caffeine.newBuilder()
                                                            .expireAfterWrite(cacheDuration)
                                                            .build();
                                                    cache.put(cacheKey, data);
                                                    return cache;
                                                }
                                        )));
                                    }
                                    return Mono.fromFuture(CompletableFuture.supplyAsync(() -> Optional.ofNullable(
                                                    fluxDataCache.get(cacheKey))))
                                            .flatMap(Mono::justOrEmpty)
                                            .flatMap(asyncCache -> Mono.justOrEmpty(asyncCache.getIfPresent(cacheKey))
                                                    .flatMap(deque -> Mono.fromFuture(CompletableFuture.supplyAsync(() -> {
                                                        deque.add(item);
                                                        return true;
                                                    })))
                                            );
                                })
                )
                .map(Tuple2::getT1);
    }

    @Override
    public Mono<Void> cleanupData(@NonNull String cacheKey) {
        return Mono.fromFuture(CompletableFuture.runAsync(() -> {
            Cache<String, ConcurrentLinkedDeque<Object>> cache = fluxDataCache.remove(cacheKey);
            if (Objects.nonNull(cache)) {
                cache.invalidateAll();
            }
            log.debug("[Caffeine reactive cache flux adapter]Cleanup cached data success, CacheKey: {}", cacheKey);
        }));
    }

}
