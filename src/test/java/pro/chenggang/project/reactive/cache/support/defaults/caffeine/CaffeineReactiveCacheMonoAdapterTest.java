package pro.chenggang.project.reactive.cache.support.defaults.caffeine;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import pro.chenggang.project.reactive.cache.support.BaseTest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CaffeineReactiveCacheMonoAdapterTest extends BaseTest {

    CaffeineReactiveCacheMonoAdapter caffeineReactiveCacheMonoAdapter = new CaffeineReactiveCacheMonoAdapter();

    @Order(1)
    @Test
    void cacheData() {
        caffeineReactiveCacheMonoAdapter.cacheData(cacheKey, Duration.ofSeconds(3), Mono.just(true))
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();
        caffeineReactiveCacheMonoAdapter.cacheData(cacheKey, Duration.ofSeconds(3), Mono.just(true))
                .then(caffeineReactiveCacheMonoAdapter.cacheData(cacheKey, Duration.ofSeconds(3), Mono.just(false)))
                .as(StepVerifier::create)
                .expectNext(false)
                .verifyComplete();
    }

    @Order(3)
    @Test
    void hasData() {
        caffeineReactiveCacheMonoAdapter.hasData(cacheKey)
                .as(StepVerifier::create)
                .expectNext(false)
                .verifyComplete();
        caffeineReactiveCacheMonoAdapter.cacheData(cacheKey, Duration.ofSeconds(3), Mono.just(true))
                .then(caffeineReactiveCacheMonoAdapter.hasData(cacheKey))
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();
    }

    @Order(4)
    @Test
    void loadData() {
        caffeineReactiveCacheMonoAdapter.cleanupData(cacheKey)
                .as(StepVerifier::create)
                .verifyComplete();
        caffeineReactiveCacheMonoAdapter.loadData(cacheKey)
                .as(StepVerifier::create)
                .verifyComplete();
        caffeineReactiveCacheMonoAdapter.cacheData(cacheKey, Duration.ofSeconds(3), Mono.just(true))
                .then(caffeineReactiveCacheMonoAdapter.loadData(cacheKey))
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();
    }

    @Order(2)
    @Test
    void cleanupData() {
        caffeineReactiveCacheMonoAdapter.cleanupData(cacheKey)
                .as(StepVerifier::create)
                .verifyComplete();
        caffeineReactiveCacheMonoAdapter.cacheData(cacheKey, Duration.ofSeconds(3), Mono.just(true))
                .then(caffeineReactiveCacheMonoAdapter.cleanupData(cacheKey))
                .as(StepVerifier::create)
                .verifyComplete();
    }
}