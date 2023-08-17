package pro.chenggang.project.reactive.cache.support.defaults.executor;

import org.junit.jupiter.api.Test;
import pro.chenggang.project.reactive.cache.support.BaseTest;
import pro.chenggang.project.reactive.cache.support.defaults.inmemory.InmemoryReactiveCacheFluxAdapter;
import pro.chenggang.project.reactive.cache.support.defaults.inmemory.InmemoryReactiveCacheLock;
import pro.chenggang.project.reactive.cache.support.exception.NoSuchCachedReactiveDataException;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
class DefaultReactiveFluxCacheTest extends BaseTest {

    DefaultReactiveFluxCache defaultReactiveFluxCache = new DefaultReactiveFluxCache(cacheName,
            maxWaitingDuration,
            new InmemoryReactiveCacheLock(),
            new InmemoryReactiveCacheFluxAdapter()
    );

    @Test
    void get() {
        defaultReactiveFluxCache.get(cacheKey)
                .as(StepVerifier::create)
                .expectError(NoSuchCachedReactiveDataException.class)
                .verify();
    }

    @Test
    void cacheIfNecessary() {
        defaultReactiveFluxCache.cacheIfNecessary(cacheKey, Duration.ofSeconds(3), Flux.range(0, 3))
                .as(StepVerifier::create)
                .expectNext(0)
                .expectNext(1)
                .expectNext(2)
                .verifyComplete();

    }

    @Test
    void cacheIfNecessaryWithError() {
        defaultReactiveFluxCache.cacheIfNecessary(cacheKey,
                        Duration.ofSeconds(3),
                        Flux.error(new IllegalStateException())
                )
                .as(StepVerifier::create)
                .expectError(IllegalStateException.class)
                .verify();
        defaultReactiveFluxCache.cacheIfNecessary(cacheKey, Duration.ofSeconds(3), Flux.range(0, 3)
                        .doOnNext(value -> {
                            if (value == 2) {
                                throw new IllegalStateException();
                            }
                        })
                )
                .as(StepVerifier::create)
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    void evictCache() {
        defaultReactiveFluxCache
                .evictCache(cacheKey)
                .as(StepVerifier::create)
                .verifyComplete();
    }
}