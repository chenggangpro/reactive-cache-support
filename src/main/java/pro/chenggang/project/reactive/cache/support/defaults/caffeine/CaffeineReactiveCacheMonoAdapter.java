package pro.chenggang.project.reactive.cache.support.defaults.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pro.chenggang.project.reactive.cache.support.core.adapter.ReactiveCacheMonoAdapter;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The inmemory reactive cache mono adapter by using caffeine
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class CaffeineReactiveCacheMonoAdapter implements ReactiveCacheMonoAdapter {

    private final ConcurrentHashMap<String, Cache<String, Object>> monoDataCache = new ConcurrentHashMap<>();

    @Override
    public Mono<Boolean> hasData(@NonNull String cacheKey) {
        return Mono.defer(() -> Mono.fromFuture(CompletableFuture.supplyAsync(() ->
                monoDataCache.containsKey(cacheKey)
                        &&
                        monoDataCache.get(cacheKey)
                                .asMap()
                                .containsKey(cacheKey)))
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Mono<T> loadData(@NonNull String cacheKey) {
        return Mono.defer(() -> Mono.fromFuture(CompletableFuture.supplyAsync(() ->
                                Optional.ofNullable(monoDataCache.get(cacheKey)))
                        )
                        .flatMap(Mono::justOrEmpty)
                        .flatMap(cache -> (Mono<T>) Mono.justOrEmpty(cache.getIfPresent(cacheKey)))
        );
    }

    @Override
    public <T> Mono<T> cacheData(@NonNull String cacheKey,
                                 @NonNull Duration cacheDuration,
                                 @NonNull Mono<T> sourcePublisher) {
        return sourcePublisher.flatMap(nextData -> Mono.defer(() -> Mono.fromFuture(CompletableFuture.runAsync(() -> {
                    monoDataCache.compute(cacheKey, (key, value) -> {
                        if (Objects.isNull(value)) {
                            Cache<String, Object> cache = Caffeine.newBuilder()
                                    .expireAfterWrite(cacheDuration)
                                    .build();
                            cache.put(cacheKey, nextData);
                            return cache;
                        }
                        value.invalidateAll();
                        Cache<String, Object> cache = Caffeine.newBuilder()
                                .expireAfterWrite(cacheDuration)
                                .build();
                        cache.put(cacheKey, nextData);
                        return cache;
                    });
                })))
                .thenReturn(nextData));
    }

    @Override
    public Mono<Void> cleanupData(@NonNull String cacheKey) {
        return Mono.defer(() -> Mono.fromFuture(CompletableFuture.runAsync(() -> {
            Cache<String, Object> cache = monoDataCache.remove(cacheKey);
            if (Objects.nonNull(cache)) {
                cache.invalidateAll();
            }
            log.debug("[Caffeine reactive cache mono adapter]Cleanup cached data success, CacheKey: {}", cacheKey);
        })));
    }
}
