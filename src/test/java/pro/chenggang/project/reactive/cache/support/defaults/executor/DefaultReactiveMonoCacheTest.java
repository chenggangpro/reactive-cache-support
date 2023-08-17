package pro.chenggang.project.reactive.cache.support.defaults.executor;

import org.junit.jupiter.api.Test;
import pro.chenggang.project.reactive.cache.support.BaseTest;
import pro.chenggang.project.reactive.cache.support.defaults.inmemory.InmemoryReactiveCacheLock;
import pro.chenggang.project.reactive.cache.support.defaults.inmemory.InmemoryReactiveCacheMonoAdapter;
import pro.chenggang.project.reactive.cache.support.exception.NoSuchCachedReactiveDataException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
class DefaultReactiveMonoCacheTest extends BaseTest {

    DefaultReactiveMonoCache defaultReactiveMonoCache = new DefaultReactiveMonoCache(cacheName,
            maxWaitingDuration,
            new InmemoryReactiveCacheLock(),
            new InmemoryReactiveCacheMonoAdapter()
    );

    @Test
    void get() {
        defaultReactiveMonoCache.get(cacheKey)
                .as(StepVerifier::create)
                .expectError(NoSuchCachedReactiveDataException.class)
                .verify();
    }

    @Test
    void cacheIfNecessary() {
        defaultReactiveMonoCache.cacheIfNecessary(cacheKey, Duration.ofSeconds(3), Mono.just(true))
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void cacheIfNecessaryWithError() {
        defaultReactiveMonoCache.cacheIfNecessary(cacheKey, Duration.ofSeconds(3), Mono.error(new IllegalStateException()))
                .as(StepVerifier::create)
                .expectError(IllegalStateException.class)
                .verify();
        defaultReactiveMonoCache.cacheIfNecessary(cacheKey, Duration.ofSeconds(3), Mono.just(true)
                        .flatMap(value -> Mono.error(new IllegalStateException()).delayElement(Duration.ofSeconds(1)))
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