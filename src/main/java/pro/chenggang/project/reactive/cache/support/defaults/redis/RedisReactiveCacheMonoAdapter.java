package pro.chenggang.project.reactive.cache.support.defaults.redis;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import pro.chenggang.project.reactive.cache.support.core.adapter.ReactiveCacheMonoAdapter;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * The redis reactive cache mono adapter
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class RedisReactiveCacheMonoAdapter implements ReactiveCacheMonoAdapter {

    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    @Override
    public Mono<Boolean> hasData(@NonNull String cacheKey) {
        return reactiveRedisTemplate.hasKey(cacheKey);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Mono<T> loadData(@NonNull String cacheKey) {
        return reactiveRedisTemplate.hasKey(cacheKey)
                .filter(Boolean::booleanValue)
                .flatMap(hasKey -> (Mono<T>) reactiveRedisTemplate.opsForValue()
                        .get(cacheKey)
                );
    }

    @Override
    public <T> Mono<T> cacheData(@NonNull String cacheKey,
                                 @NonNull Duration cacheDuration,
                                 @NonNull Mono<T> sourcePublisher) {
        return sourcePublisher.flatMap(nextData -> reactiveRedisTemplate.opsForValue()
                .set(cacheKey, nextData, cacheDuration)
                .thenReturn(nextData)
        );
    }

    @Override
    public Mono<Void> cleanupData(@NonNull String cacheKey) {
        return reactiveRedisTemplate.delete(cacheKey)
                .then(Mono.defer(() -> {
                    log.debug("[Redis reactive cache mono adapter]Cleanup cached data success, CacheKey: {}",cacheKey);
                    return Mono.empty();
                }));
    }
}
