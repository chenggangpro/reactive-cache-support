package pro.chenggang.project.reactive.cache.support.defaults.executor.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.chenggang.project.reactive.cache.support.defaults.executor.DefaultReactiveMonoCache;
import pro.chenggang.project.reactive.cache.support.defaults.redis.BaseTestWithRedis;
import pro.chenggang.project.reactive.cache.support.defaults.redis.RedisReactiveCacheLock;
import pro.chenggang.project.reactive.cache.support.defaults.redis.RedisReactiveCacheMonoAdapter;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
class RedisDefaultReactiveMonoCacheTest extends BaseTestWithRedis {

    DefaultReactiveMonoCache defaultReactiveMonoCache;

    @BeforeEach
    void beforeEach() {
        defaultReactiveMonoCache = new DefaultReactiveMonoCache(cacheName,
                maxWaitingDuration,
                new RedisReactiveCacheLock(reactiveRedisTemplate),
                new RedisReactiveCacheMonoAdapter(reactiveRedisTemplate)
        );
    }

    @Test
    void cacheIfNecessary() {
        defaultReactiveMonoCache.cacheIfNecessary(cacheKey, Duration.ofSeconds(3), Mono.just(true))
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void cacheIfNecessaryWithCancel() {
        defaultReactiveMonoCache.cacheIfNecessary(cacheKey,
                        Duration.ofSeconds(3),
                        Mono.just(true)
                                .delayElement(Duration.ofSeconds(200))
                                .flatMap(value -> Mono.just(
                                                        value
                                                )
                                                .delayElement(Duration.ofMillis(200))
                                )
                )
                .as(StepVerifier::create)
                .thenAwait(Duration.ofMillis(300))
                .thenCancel()
                .verify();
    }

    @Test
    void cacheIfNecessaryWithError() {
        defaultReactiveMonoCache.cacheIfNecessary(cacheKey,
                        Duration.ofSeconds(3),
                        Mono.error(new IllegalStateException())
                )
                .as(StepVerifier::create)
                .expectError(IllegalStateException.class)
                .verify();
        defaultReactiveMonoCache.cacheIfNecessary(cacheKey, Duration.ofSeconds(3), Mono.just(true)
                        .flatMap(value -> Mono.error(new IllegalStateException())
                                .delayElement(Duration.ofSeconds(1)))
                )
                .as(StepVerifier::create)
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    void evictCache() {
        defaultReactiveMonoCache.evictCache(cacheKey)
                .as(StepVerifier::create)
                .verifyComplete();
    }
}