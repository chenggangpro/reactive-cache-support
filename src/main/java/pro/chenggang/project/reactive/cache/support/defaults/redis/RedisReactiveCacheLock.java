package pro.chenggang.project.reactive.cache.support.defaults.redis;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import pro.chenggang.project.reactive.cache.support.core.ReactiveCacheLock;
import pro.chenggang.project.reactive.cache.support.exception.ReactiveCacheLoadExhaustedException;
import reactor.core.publisher.Mono;
import reactor.retry.Backoff;
import reactor.retry.Repeat;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

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

    private static final String TRY_LOCK_LUA =
            "redis.call('ZREMRANGEBYSCORE', KEYS[1], 0, tonumber(ARGV[3])); " +
                    "redis.call('ZADD', KEYS[1], tonumber(ARGV[2]), ARGV[1]); " +
                    "local head = redis.call('ZRANGE', KEYS[1], 0, 0); " +
                    "if head[1] == ARGV[1] then return 1 else return 0 end";

    private static final String RELEASE_LOCK_LUA =
            "local head = redis.call('ZRANGE', KEYS[1], 0, 0); " +
                    "if head[1] == ARGV[1] then return redis.call('ZREM', KEYS[1], ARGV[1]) else return 0 end";

    private static final String CHECK_LOCK_LUA =
            "redis.call('ZREMRANGEBYSCORE', KEYS[1], 0, tonumber(ARGV[1])); " +
                    "return redis.call('ZCARD', KEYS[1])";

    private final RedisScript<Long> tryLockScript = RedisScript.of(TRY_LOCK_LUA, Long.class);
    private final RedisScript<Long> releaseLockScript = RedisScript.of(RELEASE_LOCK_LUA, Long.class);
    private final RedisScript<Long> checkLockScript = RedisScript.of(CHECK_LOCK_LUA, Long.class);

    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    @Override
    public Mono<Void> checkInitializeLock(@NonNull String cacheName,
                                          @NonNull String cacheKey,
                                          @NonNull Duration maxWaitingDuration) {
        final String decoratedCacheInitializeLockKey = decorateCacheInitializeLockKey(cacheName, cacheKey);
        return Mono.defer(() -> {
                    long staleThreshold = System.currentTimeMillis() - maxWaitingDuration.toMillis();
                    return reactiveRedisTemplate.execute(
                                    checkLockScript,
                                    Collections.singletonList(decoratedCacheInitializeLockKey),
                                    Collections.singletonList(staleThreshold)
                            )
                            .next();
                })
                .defaultIfEmpty(0L)
                .filter(lockedSize -> lockedSize == 0L)
                .repeatWhenEmpty(Repeat.onlyIf(repeatContext -> true)
                        .timeout(maxWaitingDuration)
                        .backoff(Backoff.fixed(Duration.ofMillis(300)))
                )
                .switchIfEmpty(Mono.defer(() -> {
                    log.error(
                            "(Check whether any cache initialization running): " +
                                    "Initialization is running and reach the max waiting duration:{}, CacheName:{},CacheKey:{}",
                            maxWaitingDuration,
                            cacheName,
                            cacheKey
                    );
                    return Mono.error(new ReactiveCacheLoadExhaustedException(cacheName, cacheKey));
                }))
                .doOnNext(lockNotExist -> log.debug(
                        "(Check whether any cache initialization running): " +
                                "None of initialization is running, CacheName:{},CacheKey:{}",
                        cacheName,
                        cacheKey
                ))
                .then();
    }

    @Override
    public Mono<String> tryLockInitializeLock(@NonNull String cacheName, @NonNull String cacheKey, @NonNull Duration maxWaitingDuration) {
        final String cacheInitializeLockKey = decorateCacheInitializeLockKey(cacheName, cacheKey);
        final String currentOperationId = UUID.randomUUID().toString();
        return Mono.defer(() -> {
                    long now = System.currentTimeMillis();
                    long staleThreshold = now - maxWaitingDuration.toMillis();
                    return reactiveRedisTemplate.execute(
                                    tryLockScript,
                                    Collections.singletonList(cacheInitializeLockKey),
                                    Arrays.asList(currentOperationId, now, staleThreshold)
                            )
                            .next()
                            .filter(result -> result == 1L)
                            .repeatWhenEmpty(Repeat.onlyIf(repeatContext -> true)
                                    .timeout(maxWaitingDuration)
                                    .backoff(Backoff.fixed(Duration.ofMillis(300)))
                            )
                            .switchIfEmpty(Mono.defer(() -> {
                                log.error(
                                        "(Check whether any cache initialization running): " +
                                                "Current operation is not the head of lock queue and reach the max waiting duration: {}, " +
                                                "CacheName: {},CacheKey: {}, CurrentOperationId: {}",
                                        maxWaitingDuration,
                                        cacheName,
                                        cacheKey,
                                        currentOperationId
                                );
                                return reactiveRedisTemplate.opsForZSet()
                                        .remove(cacheInitializeLockKey, currentOperationId)
                                        .then(Mono.error(new ReactiveCacheLoadExhaustedException(cacheName, cacheKey)));
                            }));
                })
                .doOnNext(__ -> log.debug(
                        "(Lock initialization success): CacheName: {},CacheKey: {},CurrentOperationId: {}",
                        cacheName,
                        cacheKey,
                        currentOperationId
                ))
                .thenReturn(currentOperationId);
    }

    @Override
    public Mono<String> releaseInitializeLock(@NonNull String cacheName, @NonNull String cacheKey, @NonNull String operationId) {
        final String cacheInitializeLockKey = decorateCacheInitializeLockKey(cacheName, cacheKey);
        return reactiveRedisTemplate.execute(
                        releaseLockScript,
                        Collections.singletonList(cacheInitializeLockKey),
                        Collections.singletonList(operationId)
                )
                .next()
                .filter(result -> Objects.equals(result, 1L))
                .doOnNext(result -> log.debug(
                        "(Release initialization lock): CacheName: {}, CacheKey: {}, OperationId: {}",
                        cacheName,
                        cacheKey,
                        operationId
                ))
                .map(result -> operationId);
    }
}
