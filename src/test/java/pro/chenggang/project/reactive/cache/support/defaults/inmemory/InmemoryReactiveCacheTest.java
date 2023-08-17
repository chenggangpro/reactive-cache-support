package pro.chenggang.project.reactive.cache.support.defaults.inmemory;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import pro.chenggang.project.reactive.cache.support.BaseTest;
import pro.chenggang.project.reactive.cache.support.core.ReactiveCache;
import pro.chenggang.project.reactive.cache.support.defaults.DefaultReactiveCache;
import pro.chenggang.project.reactive.cache.support.exception.NoSuchCachedReactiveDataException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class InmemoryReactiveCacheTest extends BaseTest {

    ReactiveCache inmemoryReactiveCache = new DefaultReactiveCache(
            cacheName,
            maxWaitingDuration,
            new InmemoryReactiveCacheLock(),
            new InmemoryReactiveCacheMonoAdapter(),
            new InmemoryReactiveCacheFluxAdapter()
    );

    @Order(1)
    @Test
    void get() {
        inmemoryReactiveCache.monoCache()
                .get(cacheKey)
                .as(StepVerifier::create)
                .expectError(NoSuchCachedReactiveDataException.class)
                .verify();
    }

    @Order(2)
    @Test
    void cacheIfNecessary() {
        inmemoryReactiveCache.monoCache()
                .cacheIfNecessary(cacheKey, Duration.ofSeconds(3), Mono.just(true))
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();
    }

    @Order(3)
    @Test
    void getMany() {
        inmemoryReactiveCache.fluxCache()
                .get(cacheKey)
                .as(StepVerifier::create)
                .expectError(NoSuchCachedReactiveDataException.class)
                .verify();
    }

    @Order(4)
    @Test
    void cacheManyIfNecessary() {
        inmemoryReactiveCache.fluxCache()
                .cacheIfNecessary(cacheKey, Duration.ofSeconds(3), Flux.range(0,3))
                .as(StepVerifier::create)
                .expectNext(0)
                .expectNext(1)
                .expectNext(2)
                .verifyComplete();
    }

    @Order(5)
    @Test
    void evictCache() {
        inmemoryReactiveCache.monoCache()
                .evictCache(cacheKey)
                .as(StepVerifier::create)
                .verifyComplete();
        inmemoryReactiveCache.fluxCache()
                .evictCache(cacheKey)
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Order(6)
    @Test
    void getInMultiThread() {
        inmemoryReactiveCache.monoCache()
                .cacheIfNecessary(cacheKey, Duration.ofSeconds(30), Mono.just(true))
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();
        ExecutorService executorService = Executors.newScheduledThreadPool(8);
        int totalCount = 10;
        for (int i = 0; i < totalCount; i++) {
            executorService.submit(() -> {
                inmemoryReactiveCache.monoCache()
                        .get(cacheKey)
                        .as(StepVerifier::create)
                        .expectNext(true)
                        .verifyComplete();
            });
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    @Order(7)
    @Test
    void getManyInMultiThread() {
        inmemoryReactiveCache.fluxCache()
                .cacheIfNecessary(cacheKey, Duration.ofSeconds(30), Flux.range(0, 3))
                .then()
                .as(StepVerifier::create)
                .verifyComplete();
        ExecutorService executorService = Executors.newScheduledThreadPool(8);
        int totalCount = 10;
        for (int i = 0; i < totalCount; i++) {
            executorService.submit(() -> {
                inmemoryReactiveCache.fluxCache()
                        .<Integer>get(cacheKey)
                        .as(StepVerifier::create)
                        .expectNext(0)
                        .expectNext(1)
                        .expectNext(2)
                        .verifyComplete();
            });
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    @Order(8)
    @Test
    void cacheIfNecessaryInMultiThread() {
        ExecutorService executorService = Executors.newScheduledThreadPool(8);
        int totalCount = 10;
        for (int i = 0; i < totalCount; i++) {
            int finalI = i;
            executorService.submit(() -> {
                try {
                    TimeUnit.SECONDS.sleep(finalI);
                } catch (InterruptedException e) {
                    // ignore
                }
                inmemoryReactiveCache.monoCache()
                        .cacheIfNecessary(cacheKey, Duration.ofSeconds(2), Mono.just(finalI))
                        .as(StepVerifier::create)
                        .consumeNextWith(value -> System.out.println("I:" + finalI + ",Value:" + value))
                        .verifyComplete();
            });
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    @Order(9)
    @Test
    void cacheManyIfNecessaryInMultiThread() {
        ExecutorService executorService = Executors.newScheduledThreadPool(8);
        int totalCount = 10;
        for (int i = 0; i < totalCount; i++) {
            int finalI = i;
            executorService.submit(() -> {
                try {
                    TimeUnit.SECONDS.sleep(finalI);
                } catch (InterruptedException e) {
                    // ignore
                }
                inmemoryReactiveCache.fluxCache()
                        .cacheIfNecessary(cacheKey, Duration.ofSeconds(2), Flux.range(0, finalI))
                        .collectList()
                        .as(StepVerifier::create)
                        .consumeNextWith(value -> System.out.println("I:" + finalI + ",Value:" + value))
                        .verifyComplete();
            });
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            // ignore
        }
    }

}