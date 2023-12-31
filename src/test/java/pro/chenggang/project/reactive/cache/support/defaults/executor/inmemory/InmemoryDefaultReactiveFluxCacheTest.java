package pro.chenggang.project.reactive.cache.support.defaults.executor.inmemory;

import lombok.NonNull;
import org.junit.jupiter.api.Test;
import pro.chenggang.project.reactive.cache.support.BaseTest;
import pro.chenggang.project.reactive.cache.support.core.adapter.ReactiveCacheFluxAdapter;
import pro.chenggang.project.reactive.cache.support.defaults.executor.DefaultReactiveFluxCache;
import pro.chenggang.project.reactive.cache.support.defaults.inmemory.InmemoryReactiveCacheFluxAdapter;
import pro.chenggang.project.reactive.cache.support.defaults.inmemory.InmemoryReactiveCacheLock;
import pro.chenggang.project.reactive.cache.support.exception.NoSuchCachedReactiveDataException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
class InmemoryDefaultReactiveFluxCacheTest extends BaseTest {

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
    void cacheIfNecessaryWithCancel() {
        defaultReactiveFluxCache.cacheIfNecessary(cacheKey,
                        Duration.ofSeconds(3),
                        Flux.range(0, 3)
                                .delaySequence(Duration.ofSeconds(200))
                )
                .as(StepVerifier::create)
                .thenAwait(Duration.ofMillis(300))
                .thenCancel()
                .verify();
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
        defaultReactiveFluxCache.evictCache(cacheKey)
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void evictCacheWithError(){
        DefaultReactiveFluxCache defaultReactiveFluxCache = new DefaultReactiveFluxCache(cacheName,
                maxWaitingDuration,
                new InmemoryReactiveCacheLock(),
                new ErrorReactiveCacheFluxAdapter()
        );
        defaultReactiveFluxCache.evictCache(cacheKey)
                .as(StepVerifier::create)
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    void evictCacheWithCancel(){
        DefaultReactiveFluxCache defaultReactiveFluxCache = new DefaultReactiveFluxCache(cacheName,
                maxWaitingDuration,
                new InmemoryReactiveCacheLock(),
                new CancelReactiveCacheFluxAdapter()
        );
        defaultReactiveFluxCache.evictCache(cacheKey)
                .delayElement(Duration.ofMillis(500))
                .as(StepVerifier::create)
                .thenAwait(Duration.ofMillis(500))
                .thenCancel()
                .verify();
    }

    private static class ErrorReactiveCacheFluxAdapter implements ReactiveCacheFluxAdapter {

        @Override
        public Mono<Boolean> hasData(@NonNull String cacheKey) {
            return Mono.just(true);
        }

        @Override
        public <T> Flux<T> loadData(@NonNull String cacheKey) {
            return Flux.empty();
        }

        @Override
        public <T> Flux<T> cacheData(@NonNull String cacheKey,
                                     @NonNull Duration cacheDuration,
                                     @NonNull Flux<T> sourcePublisher) {
            return sourcePublisher;
        }

        @Override
        public Mono<Void> cleanupData(@NonNull String cacheKey) {
            return Mono.error(new IllegalStateException())
                    .then();
        }
    }

    private static class CancelReactiveCacheFluxAdapter implements ReactiveCacheFluxAdapter {

        @Override
        public Mono<Boolean> hasData(@NonNull String cacheKey) {
            return Mono.just(true);
        }

        @Override
        public <T> Flux<T> loadData(@NonNull String cacheKey) {
            return Flux.empty();
        }

        @Override
        public <T> Flux<T> cacheData(@NonNull String cacheKey,
                                     @NonNull Duration cacheDuration,
                                     @NonNull Flux<T> sourcePublisher) {
            return sourcePublisher;
        }

        @Override
        public Mono<Void> cleanupData(@NonNull String cacheKey) {
            return Mono.defer(() -> Mono.just(true)
                    .delayElement(Duration.ofSeconds(1)))
                    .then();
        }
    }
}