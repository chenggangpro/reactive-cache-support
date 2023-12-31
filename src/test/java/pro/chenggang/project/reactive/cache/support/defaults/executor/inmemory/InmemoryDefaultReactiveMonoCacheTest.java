package pro.chenggang.project.reactive.cache.support.defaults.executor.inmemory;

import lombok.NonNull;
import org.junit.jupiter.api.Test;
import pro.chenggang.project.reactive.cache.support.BaseTest;
import pro.chenggang.project.reactive.cache.support.core.adapter.ReactiveCacheMonoAdapter;
import pro.chenggang.project.reactive.cache.support.defaults.executor.DefaultReactiveMonoCache;
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
class InmemoryDefaultReactiveMonoCacheTest extends BaseTest {

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

    @Test
    void evictCacheWithError(){
        DefaultReactiveMonoCache defaultReactiveMonoCache = new DefaultReactiveMonoCache(cacheName,
                maxWaitingDuration,
                new InmemoryReactiveCacheLock(),
                new ErrorReactiveCacheMonoAdapter()
        );
        defaultReactiveMonoCache.evictCache(cacheKey)
                .as(StepVerifier::create)
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    void evictCacheWithCancel(){
        DefaultReactiveMonoCache defaultReactiveMonoCache = new DefaultReactiveMonoCache(cacheName,
                maxWaitingDuration,
                new InmemoryReactiveCacheLock(),
                new CancelReactiveCacheMonoAdapter()
        );
        defaultReactiveMonoCache.evictCache(cacheKey)
                .delayElement(Duration.ofMillis(500))
                .as(StepVerifier::create)
                .thenAwait(Duration.ofMillis(500))
                .thenCancel()
                .verify();
    }

    private static class ErrorReactiveCacheMonoAdapter implements ReactiveCacheMonoAdapter {

        @Override
        public Mono<Boolean> hasData(@NonNull String cacheKey) {
            return Mono.just(true);
        }

        @Override
        public <T> Mono<T> loadData(@NonNull String cacheKey) {
            return Mono.empty();
        }

        @Override
        public <T> Mono<T> cacheData(@NonNull String cacheKey,
                                     @NonNull Duration cacheDuration,
                                     @NonNull Mono<T> sourcePublisher) {
            return sourcePublisher;
        }

        @Override
        public Mono<Void> cleanupData(@NonNull String cacheKey) {
            return Mono.error(new IllegalStateException())
                    .then();
        }
    }

    private static class CancelReactiveCacheMonoAdapter implements ReactiveCacheMonoAdapter {

        @Override
        public Mono<Boolean> hasData(@NonNull String cacheKey) {
            return Mono.just(true);
        }

        @Override
        public <T> Mono<T> loadData(@NonNull String cacheKey) {
            return Mono.empty();
        }

        @Override
        public <T> Mono<T> cacheData(@NonNull String cacheKey,
                                     @NonNull Duration cacheDuration,
                                     @NonNull Mono<T> sourcePublisher) {
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