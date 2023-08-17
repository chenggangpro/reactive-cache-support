package pro.chenggang.project.reactive.cache.support.defaults.inmemory;

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
class InmemoryReactiveCacheMonoAdapterTest extends BaseTest {

    InmemoryReactiveCacheMonoAdapter inmemoryReactiveCacheMonoAdapter = new InmemoryReactiveCacheMonoAdapter();

    @Order(1)
    @Test
    void cacheData() {
        inmemoryReactiveCacheMonoAdapter.cacheData(cacheKey, Duration.ofSeconds(3), Mono.just(true))
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();
        inmemoryReactiveCacheMonoAdapter.cacheData(cacheKey, Duration.ofSeconds(3), Mono.just(true))
                .then(inmemoryReactiveCacheMonoAdapter.cacheData(cacheKey, Duration.ofSeconds(3), Mono.just(false)))
                .as(StepVerifier::create)
                .expectNext(false)
                .verifyComplete();
    }

    @Order(3)
    @Test
    void hasData() {
        inmemoryReactiveCacheMonoAdapter.hasData(cacheKey)
                .as(StepVerifier::create)
                .expectNext(false)
                .verifyComplete();
        inmemoryReactiveCacheMonoAdapter.cacheData(cacheKey, Duration.ofSeconds(3), Mono.just(true))
                .then(inmemoryReactiveCacheMonoAdapter.hasData(cacheKey))
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();
    }

    @Order(4)
    @Test
    void loadData() {
        inmemoryReactiveCacheMonoAdapter.cleanupData(cacheKey)
                .as(StepVerifier::create)
                .verifyComplete();
        inmemoryReactiveCacheMonoAdapter.loadData(cacheKey)
                .as(StepVerifier::create)
                .verifyComplete();
        inmemoryReactiveCacheMonoAdapter.cacheData(cacheKey, Duration.ofSeconds(3), Mono.just(true))
                .then(inmemoryReactiveCacheMonoAdapter.loadData(cacheKey))
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();
    }

    @Order(2)
    @Test
    void cleanupData() {
        inmemoryReactiveCacheMonoAdapter.cleanupData(cacheKey)
                .as(StepVerifier::create)
                .verifyComplete();
        inmemoryReactiveCacheMonoAdapter.cacheData(cacheKey, Duration.ofSeconds(3), Mono.just(true))
                .then(inmemoryReactiveCacheMonoAdapter.cleanupData(cacheKey))
                .as(StepVerifier::create)
                .verifyComplete();
    }
}