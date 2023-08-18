package pro.chenggang.project.reactive.cache.support.defaults.redis;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import pro.chenggang.project.reactive.cache.support.core.ReactiveCacheLock;
import pro.chenggang.project.reactive.cache.support.exception.ReactiveCacheLoadExhaustedException;
import reactor.core.publisher.Mono;
import reactor.retry.Backoff;
import reactor.retry.Repeat;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * The redis reactive cache lock
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class RedisReactiveCacheLock implements ReactiveCacheLock {

    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    @Override
    public Mono<Void> checkInitializeLock(@NonNull String cacheName,
                                          @NonNull String cacheKey,
                                          @NonNull Duration maxWaitingDuration) {
        final String decoratedCacheInitializeLockKey = decorateCacheInitializeLockKey(cacheName, cacheKey);
        return reactiveRedisTemplate.hasKey(decoratedCacheInitializeLockKey)
                .defaultIfEmpty(false)
                .filter(Predicate.not(Boolean::booleanValue))
                .repeatWhenEmpty(Repeat.onlyIf(repeatContext -> true)
                        .timeout(maxWaitingDuration)
                        .backoff(Backoff.fixed(Duration.ofMillis(300)))
                )
                .switchIfEmpty(Mono.defer(() -> {
                    log.error(
                            "[Redis reactive cache initialize lock](Check whether any cache initialization running): " +
                                    "Initialization is running and reach the max waiting duration:{}, CacheName:{},CacheKey:{}",
                            maxWaitingDuration,
                            cacheName,
                            cacheKey
                    );
                    return Mono.error(new ReactiveCacheLoadExhaustedException(cacheName, cacheKey));
                }))
                .doOnNext(lockNotExist -> log.debug(
                        "[Redis reactive cache initialize lock](Check whether any cache initialization running): " +
                                "None of initialization is running, CacheName:{},CacheKey:{}",
                        cacheName,
                        cacheKey
                ))
                .then();
    }

    @Override
    public Mono<String> tryLockInitializeLock(@NonNull String cacheName,
                                              @NonNull String cacheKey,
                                              @NonNull Duration maxWaitingDuration) {
        final String cacheInitializeLockKey = decorateCacheInitializeLockKey(cacheName, cacheKey);
        final String currentOperationId = UUID.randomUUID()
                .toString();
        return reactiveRedisTemplate.opsForList()
                .leftPush(cacheInitializeLockKey, currentOperationId)
                .flatMap(__ -> reactiveRedisTemplate.opsForList()
                        .range(cacheInitializeLockKey, -1, -1)
                        .next()
                        .filter(value -> Objects.equals(currentOperationId, value))
                        .repeatWhenEmpty(Repeat.onlyIf(repeatContext -> true)
                                .timeout(maxWaitingDuration)
                                .backoff(Backoff.fixed(Duration.ofMillis(300)))
                        )
                        .switchIfEmpty(Mono.defer(() -> {
                            log.error(
                                    "[Redis reactive cache initialize lock](Check whether any cache initialization running): " +
                                            "Current operation is not the head of lock queue and reach the max waiting duration: {}, " +
                                            "CacheName: {},CacheKey: {}, CurrentOperationId: {}",
                                    maxWaitingDuration,
                                    cacheName,
                                    cacheKey,
                                    currentOperationId
                            );
                            return reactiveRedisTemplate.opsForList()
                                    .remove(cacheInitializeLockKey, 1, currentOperationId)
                                    .then(Mono.error(new ReactiveCacheLoadExhaustedException(cacheName,
                                            cacheKey
                                    )));
                        })))
                .doOnNext(operationId -> log.debug(
                        "[Redis reactive cache initialize lock](Lock initialization success): " +
                                "CacheName: {},CacheKey: {},LockedOperationId: {},CurrentOperationId: {}",
                        cacheName,
                        cacheKey,
                        operationId,
                        currentOperationId
                ))
                .thenReturn(currentOperationId);
    }

    @Override
    public Mono<String> releaseInitializeLock(@NonNull String cacheName, @NonNull String cacheKey) {
        final String cacheInitializeLockKey = decorateCacheInitializeLockKey(cacheName, cacheKey);
        return reactiveRedisTemplate.opsForList()
                .rightPop(cacheInitializeLockKey)
                .cast(String.class)
                .doOnNext(operationId -> log.debug(
                        "[Redis reactive cache initialize lock](Release initialization lock): " +
                                "CacheName: {}, CacheKey: {},LockedOperationId: {}",
                        cacheName,
                        cacheKey,
                        operationId
                ));
    }
}
