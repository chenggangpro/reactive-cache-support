package pro.chenggang.project.reactive.cache.support.defaults.redis;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import pro.chenggang.project.reactive.cache.support.core.ReactiveCache;
import pro.chenggang.project.reactive.cache.support.core.adapter.ReactiveCacheManagerAdapter;
import pro.chenggang.project.reactive.cache.support.defaults.DefaultReactiveCache;

import java.time.Duration;

/**
 * The redis reactive cache manager adapter.
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
@SuppressWarnings("all")
@Slf4j
@RequiredArgsConstructor
public class RedisReactiveCacheManagerAdapter implements ReactiveCacheManagerAdapter {

    @NonNull
    private final Duration maxWaitingDuration;
    @NonNull
    private final RedisReactiveCacheLock redisReactiveCacheLock;
    @NonNull
    private final ReactiveRedisTemplate reactiveRedisTemplate;

    @Override
    public ReactiveCache initializeReactiveCache(@NonNull String name) {
        return new DefaultReactiveCache(name,
                maxWaitingDuration,
                redisReactiveCacheLock,
                new RedisReactiveCacheMonoAdapter(reactiveRedisTemplate),
                new RedisReactiveCacheFluxAdapter(reactiveRedisTemplate)
        );
    }
}
