package pro.chenggang.project.reactive.cache.support.defaults.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.Scheduler;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import pro.chenggang.project.reactive.cache.support.core.adapter.ReactiveCacheMonoAdapter;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;
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
        return Mono.defer(() -> Mono.fromCallable(() ->
                monoDataCache.containsKey(cacheKey)
                        &&
                        monoDataCache.get(cacheKey)
                                .asMap()
                                .containsKey(cacheKey))
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Mono<T> loadData(@NonNull String cacheKey) {
        return Mono.fromCallable(() -> monoDataCache.get(cacheKey))
                .flatMap(cache -> (Mono<T>) Mono.fromCallable(() -> cache.getIfPresent(cacheKey)));
    }

    @Override
    public <T> Mono<T> cacheData(@NonNull String cacheKey, @NonNull Duration cacheDuration, @NonNull Mono<T> sourcePublisher) {
        return sourcePublisher.flatMap(nextData -> {
            return Mono.fromRunnable(() -> {
                        monoDataCache.compute(cacheKey, (key, value) -> {
                                    if (Objects.isNull(value)) {
                                        Cache<String, Object> cache = Caffeine.newBuilder()
                                                .expireAfterWrite(cacheDuration)
                                                .removalListener(this::removalListener)
                                                .scheduler(Scheduler.systemScheduler())
                                                .build();
                                        cache.put(cacheKey, nextData);
                                        return cache;
                                    }
                                    value.invalidateAll();
                                    value.cleanUp();
                                    value = null;
                                    Cache<String, Object> cache = Caffeine.newBuilder()
                                            .expireAfterWrite(cacheDuration)
                                            .removalListener(this::removalListener)
                                            .scheduler(Scheduler.systemScheduler())
                                            .build();
                                    cache.put(cacheKey, nextData);
                                    return cache;
                                }
                        );
                    })
                    .thenReturn(nextData);
        });
    }

    private void removalListener(@Nullable String key, @Nullable Object value, RemovalCause cause) {
        log.debug("Cache removed for key : {} cause : {}", key, cause);
    }

    @Override
    public Mono<Void> cleanupData(@NonNull String cacheKey) {
        return Mono.defer(() -> Mono.fromRunnable(() -> {
            Cache<String, Object> cache = monoDataCache.remove(cacheKey);
            if (Objects.nonNull(cache)) {
                cache.invalidateAll();
                cache.cleanUp();
            }
            log.debug("[Caffeine reactive cache mono adapter]Cleanup cached data success, CacheKey: {}", cacheKey);
        }));
    }
}
