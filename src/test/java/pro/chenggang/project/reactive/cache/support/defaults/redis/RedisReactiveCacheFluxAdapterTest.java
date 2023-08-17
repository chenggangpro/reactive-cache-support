package pro.chenggang.project.reactive.cache.support.defaults.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RedisReactiveCacheFluxAdapterTest extends BaseTestWithRedis{

    RedisReactiveCacheFluxAdapter redisReactiveCacheFluxAdapter;

    @BeforeEach
    void beforeEach(){
        redisReactiveCacheFluxAdapter = new RedisReactiveCacheFluxAdapter(reactiveRedisTemplate);
    }

    @Order(1)
    @Test
    void cacheData() {
        redisReactiveCacheFluxAdapter.cacheData(cacheKey, Duration.ofSeconds(3), Flux.range(0,3))
                .as(StepVerifier::create)
                .expectNext(0)
                .expectNext(1)
                .expectNext(2)
                .verifyComplete();
        redisReactiveCacheFluxAdapter.cacheData(cacheKey, Duration.ofSeconds(3), Flux.range(0,3))
                .thenMany(redisReactiveCacheFluxAdapter.cacheData(cacheKey, Duration.ofSeconds(3), Flux.range(10,3)))
                .as(StepVerifier::create)
                .expectNext(10)
                .expectNext(11)
                .expectNext(12)
                .verifyComplete();
    }

    @Order(3)
    @Test
    void hasData() {
        redisReactiveCacheFluxAdapter.hasData(cacheKey)
                .as(StepVerifier::create)
                .expectNext(false)
                .verifyComplete();
        redisReactiveCacheFluxAdapter.cacheData(cacheKey, Duration.ofSeconds(3), Flux.range(0,3))
                .then(redisReactiveCacheFluxAdapter.hasData(cacheKey))
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();
    }

    @Order(4)
    @Test
    void loadData() {
        redisReactiveCacheFluxAdapter.cleanupData(cacheKey)
                .as(StepVerifier::create)
                .verifyComplete();
        redisReactiveCacheFluxAdapter.loadData(cacheKey)
                .as(StepVerifier::create)
                .verifyComplete();
        redisReactiveCacheFluxAdapter.cacheData(cacheKey, Duration.ofSeconds(3), Flux.range(0,3))
                .thenMany(redisReactiveCacheFluxAdapter.loadData(cacheKey))
                .as(StepVerifier::create)
                .expectNext(0)
                .expectNext(1)
                .expectNext(2)
                .verifyComplete();
    }

    @Order(2)
    @Test
    void cleanupData() {
        redisReactiveCacheFluxAdapter.cleanupData(cacheKey)
                .as(StepVerifier::create)
                .verifyComplete();
        redisReactiveCacheFluxAdapter.cacheData(cacheKey, Duration.ofSeconds(3), Flux.range(0,3))
                .then(redisReactiveCacheFluxAdapter.cleanupData(cacheKey))
                .as(StepVerifier::create)
                .verifyComplete();
    }
}