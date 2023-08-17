package pro.chenggang.project.reactive.cache.support.defaults.redis;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.chenggang.project.reactive.cache.support.core.ReactiveCache;
import pro.chenggang.project.reactive.cache.support.defaults.DefaultReactiveCache;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
class RedisReactiveCacheManagerAdapterTest extends BaseTestWithRedis {

    RedisReactiveCacheManagerAdapter redisReactiveCacheManagerAdapter;

    @BeforeEach
    void beforeEach() {
        redisReactiveCacheManagerAdapter = new RedisReactiveCacheManagerAdapter(maxWaitingDuration,
                new RedisReactiveCacheLock(reactiveRedisTemplate),
                reactiveRedisTemplate
        );
    }

    @Test
    void initializeReactiveCache() {
        ReactiveCache reactiveCache = redisReactiveCacheManagerAdapter.initializeReactiveCache(cacheName);
        Assertions.assertNotNull(reactiveCache);
        Assertions.assertEquals(reactiveCache.getClass(), DefaultReactiveCache.class);
    }
}