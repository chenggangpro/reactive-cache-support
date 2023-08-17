package pro.chenggang.project.reactive.cache.support.defaults.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RedisReactiveCacheMonoAdapterTest extends BaseTestWithRedis {

    RedisReactiveCacheMonoAdapter redisReactiveCacheMonoAdapter;

    @BeforeEach
    void beforeEach() {
        redisReactiveCacheMonoAdapter = new RedisReactiveCacheMonoAdapter(reactiveRedisTemplate);
    }

    @Order(1)
    @Test
    void cacheData() {
        redisReactiveCacheMonoAdapter.cacheData(cacheKey, Duration.ofSeconds(3), Mono.just(true))
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();
        redisReactiveCacheMonoAdapter.cacheData(cacheKey, Duration.ofSeconds(3), Mono.just(true))
                .then(redisReactiveCacheMonoAdapter.cacheData(cacheKey, Duration.ofSeconds(3), Mono.just(false)))
                .as(StepVerifier::create)
                .expectNext(false)
                .verifyComplete();
    }

    @Order(3)
    @Test
    void hasData() {
        redisReactiveCacheMonoAdapter.hasData(cacheKey)
                .as(StepVerifier::create)
                .expectNext(false)
                .verifyComplete();
        redisReactiveCacheMonoAdapter.cacheData(cacheKey, Duration.ofSeconds(3), Mono.just(true))
                .then(redisReactiveCacheMonoAdapter.hasData(cacheKey))
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();
    }

    @Order(4)
    @Test
    void loadData() {
        redisReactiveCacheMonoAdapter.cleanupData(cacheKey)
                .as(StepVerifier::create)
                .verifyComplete();
        redisReactiveCacheMonoAdapter.loadData(cacheKey)
                .as(StepVerifier::create)
                .verifyComplete();
        redisReactiveCacheMonoAdapter.cacheData(cacheKey, Duration.ofSeconds(3), Mono.just(true))
                .then(redisReactiveCacheMonoAdapter.loadData(cacheKey))
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();
    }

    @Order(2)
    @Test
    void cleanupData() {
        redisReactiveCacheMonoAdapter.cleanupData(cacheKey)
                .as(StepVerifier::create)
                .verifyComplete();
        redisReactiveCacheMonoAdapter.cacheData(cacheKey, Duration.ofSeconds(3), Mono.just(true))
                .then(redisReactiveCacheMonoAdapter.cleanupData(cacheKey))
                .as(StepVerifier::create)
                .verifyComplete();
    }
}