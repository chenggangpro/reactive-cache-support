package pro.chenggang.project.reactive.cache.support.defaults.inmemory;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import pro.chenggang.project.reactive.cache.support.BaseTest;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class InmemoryReactiveCacheFluxAdapterTest extends BaseTest {

    InmemoryReactiveCacheFluxAdapter inmemoryReactiveCacheFluxAdapter = new InmemoryReactiveCacheFluxAdapter();

    @Order(1)
    @Test
    void cacheData() {
        inmemoryReactiveCacheFluxAdapter.cacheData(cacheKey, Duration.ofSeconds(3), Flux.range(0,3))
                .as(StepVerifier::create)
                .expectNext(0)
                .expectNext(1)
                .expectNext(2)
                .verifyComplete();
        inmemoryReactiveCacheFluxAdapter.cacheData(cacheKey, Duration.ofSeconds(3), Flux.range(0,3))
                .thenMany(inmemoryReactiveCacheFluxAdapter.cacheData(cacheKey, Duration.ofSeconds(3), Flux.range(10,3)))
                .as(StepVerifier::create)
                .expectNext(10)
                .expectNext(11)
                .expectNext(12)
                .verifyComplete();
    }

    @Order(3)
    @Test
    void hasData() {
        inmemoryReactiveCacheFluxAdapter.hasData(cacheKey)
                .as(StepVerifier::create)
                .expectNext(false)
                .verifyComplete();
        inmemoryReactiveCacheFluxAdapter.cacheData(cacheKey, Duration.ofSeconds(3), Flux.range(0,3))
                .then(inmemoryReactiveCacheFluxAdapter.hasData(cacheKey))
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();
    }

    @Order(4)
    @Test
    void loadData() {
        inmemoryReactiveCacheFluxAdapter.cleanupData(cacheKey)
                .as(StepVerifier::create)
                .verifyComplete();
        inmemoryReactiveCacheFluxAdapter.loadData(cacheKey)
                .as(StepVerifier::create)
                .verifyComplete();
        inmemoryReactiveCacheFluxAdapter.cacheData(cacheKey, Duration.ofSeconds(3), Flux.range(0,3))
                .thenMany(inmemoryReactiveCacheFluxAdapter.loadData(cacheKey))
                .as(StepVerifier::create)
                .expectNext(0)
                .expectNext(1)
                .expectNext(2)
                .verifyComplete();
    }

    @Order(2)
    @Test
    void cleanupData() {
        inmemoryReactiveCacheFluxAdapter.cleanupData(cacheKey)
                .as(StepVerifier::create)
                .verifyComplete();
        inmemoryReactiveCacheFluxAdapter.cacheData(cacheKey, Duration.ofSeconds(3), Flux.range(0,3))
                .then(inmemoryReactiveCacheFluxAdapter.cleanupData(cacheKey))
                .as(StepVerifier::create)
                .verifyComplete();
    }
}