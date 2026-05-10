package pro.chenggang.project.reactive.cache.support.defaults.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;
import pro.chenggang.project.reactive.cache.support.exception.ReactiveCacheLoadExhaustedException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
class RedisReactiveCacheLockTest extends BaseTestWithRedis{

    RedisReactiveCacheLock redisReactiveCacheLock;

    @BeforeEach
    void beforeEach() {
        redisReactiveCacheLock = new RedisReactiveCacheLock(reactiveRedisTemplate);
    }

    @Test
    void checkInitializeLock() {
        redisReactiveCacheLock.checkInitializeLock(
                        cacheName,
                        cacheKey,
                        Duration.ofSeconds(1)
                )
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void checkInitializeLockWhenError() {
        Mono<String> lockMono = redisReactiveCacheLock.tryLockInitializeLock(cacheName,
                cacheKey,
                Duration.ofSeconds(3)
        );
        Mono<Void> checkMono = redisReactiveCacheLock.checkInitializeLock(
                cacheName,
                cacheKey,
                Duration.ofSeconds(1)
        );
        Flux.zip(lockMono, checkMono)
                .as(StepVerifier::create)
                .expectError(ReactiveCacheLoadExhaustedException.class)
                .verify();
    }


    @Test
    void tryLockInitializeLock() {
        redisReactiveCacheLock.tryLockInitializeLock(cacheName,
                        cacheKey,
                        Duration.ofSeconds(3)
                )
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void tryLockInitializeLockWhenFailed() {
        Mono<String> lockMono1 = redisReactiveCacheLock.tryLockInitializeLock(cacheName,
                cacheKey,
                maxWaitingDuration
        );
        Mono<String> lockMono2 = redisReactiveCacheLock.tryLockInitializeLock(cacheName,
                cacheKey,
                Duration.ofSeconds(3)
        );
        Flux.zip(lockMono1, lockMono2)
                .as(StepVerifier::create)
                .expectError(ReactiveCacheLoadExhaustedException.class)
                .verify();
    }


    @Test
    void releaseInitializeLock() {
        redisReactiveCacheLock.tryLockInitializeLock(cacheName,
                cacheKey,
                Duration.ofSeconds(3)
        ).flatMap(operationId -> redisReactiveCacheLock.releaseInitializeLock(cacheName,
                cacheKey,
                operationId
        ))
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void releaseInitializeLockWhenEmpty() {
        redisReactiveCacheLock.releaseInitializeLock(cacheName,
                        cacheKey,
                        java.util.UUID.randomUUID().toString()
                )
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void crashRecoveryTest() {
        final String cacheInitializeLockKey = redisReactiveCacheLock.decorateCacheInitializeLockKey(cacheName, cacheKey);
        final String staleOperationId = "stale-op-id";
        // Manual push a stale entry (score = 0)
        reactiveRedisTemplate.opsForZSet()
                .add(cacheInitializeLockKey, staleOperationId, 0.0)
                .block();

        // Should recover by pruning the stale entry
        redisReactiveCacheLock.tryLockInitializeLock(cacheName,
                        cacheKey,
                        Duration.ofSeconds(3)
                )
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();

        // Verify stale entry is gone
        Mono<Double> scoreMono = reactiveRedisTemplate.opsForZSet()
                .score(cacheInitializeLockKey, staleOperationId);
        StepVerifier.create(scoreMono)
                .verifyComplete();
    }


    @Test
    void tryLockAndReleaseConcurrency() {
        ExecutorService executorService = Executors.newScheduledThreadPool(8);
        int totalCount = 10;
        final Boolean[] resultContainer = new Boolean[totalCount];
        for (int i = 0; i < totalCount; i++) {
            int finalI = i;
            executorService.submit(() -> {
                singleLockOperation(finalI)
                        .as(StepVerifier::create)
                        .consumeNextWith(result -> {
                            resultContainer[finalI] = result;
                        })
                        .verifyComplete();
            });
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            // ignore
        }
        long successCount = Arrays.stream(resultContainer)
                .filter(value -> value)
                .count();
        System.out.println("Success Count : " + successCount);
    }

    static final AtomicBoolean competingResource = new AtomicBoolean(false);

    Mono<Boolean> singleLockOperation(Integer index) {
        return Mono.usingWhen(
                        redisReactiveCacheLock.tryLockInitializeLock(cacheName,
                                cacheKey,
                                Duration.ofSeconds(4)
                        ),
                        value -> {
                            return Mono.just(value)
                                    .flatMap(item -> {
                                        return Mono.defer(() -> {
                                            try {
                                                if (!competingResource.compareAndSet(false, true)) {
                                                    return Mono.error(new ConcurrentModificationException(
                                                            "!!! COMPETING RESOURCE UPDATE ERROR !!! [START]"));
                                                }
                                                TimeUnit.SECONDS.sleep(2);
                                            } catch (InterruptedException e) {
                                                return Mono.error(e);
                                            }
                                            if (!competingResource.compareAndSet(true, false)) {
                                                return Mono.error(new ConcurrentModificationException(
                                                        "!!! COMPETING RESOURCE UPDATE ERROR !!! [END]"));
                                            }
                                            return Mono.just(RandomStringUtils.randomAlphanumeric(index));
                                        });
                                    })
                                    .thenReturn(true);
                        },
                        value -> redisReactiveCacheLock.releaseInitializeLock(cacheName, cacheKey, value),
                        (value, throwable) -> redisReactiveCacheLock.releaseInitializeLock(cacheName,
                                        cacheKey,
                                        value
                                )
                                .then(Mono.error(throwable)),
                        value -> redisReactiveCacheLock.releaseInitializeLock(cacheName, cacheKey, value)
                )
                .onErrorResume(ReactiveCacheLoadExhaustedException.class, throwable -> Mono.just(false));
    }

}