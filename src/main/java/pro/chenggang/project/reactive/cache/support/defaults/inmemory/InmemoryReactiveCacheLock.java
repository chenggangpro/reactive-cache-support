package pro.chenggang.project.reactive.cache.support.defaults.inmemory;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import pro.chenggang.project.reactive.cache.support.core.ReactiveCacheLock;
import pro.chenggang.project.reactive.cache.support.exception.ReactiveCacheLoadExhaustedException;
import reactor.core.publisher.Mono;
import reactor.retry.Backoff;
import reactor.retry.Repeat;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * The inmemory reactive cache initialize lock
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public class InmemoryReactiveCacheLock implements ReactiveCacheLock {

    private final ConcurrentHashMap<String, ConcurrentLinkedDeque<String>> lockContainer = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> checkInitializeLock(@NonNull String cacheName,
                                          @NonNull String cacheKey,
                                          @NonNull Duration maxWaitingDuration) {
        final String decoratedCacheInitializeLockKey = decorateCacheInitializeLockKey(cacheName, cacheKey);
        return Mono.defer(() -> Mono.fromFuture(CompletableFuture.supplyAsync(() -> lockContainer.get(
                        decoratedCacheInitializeLockKey)))
                )
                .map(ConcurrentLinkedDeque::size)
                .defaultIfEmpty(0)
                .filter(lockedSize -> lockedSize == 0)
                .repeatWhenEmpty(Repeat.onlyIf(repeatContext -> true)
                        .timeout(maxWaitingDuration)
                        .backoff(Backoff.fixed(Duration.ofMillis(300)))
                )
                .switchIfEmpty(Mono.defer(() -> {
                    log.error(
                            "[Inmemory reactive cache initialize lock](Check whether any cache initialization running): " +
                                    "Initialization is running and reach the max waiting duration:{}, CacheName:{},CacheKey:{}",
                            maxWaitingDuration,
                            cacheName,
                            cacheKey
                    );
                    return Mono.error(new ReactiveCacheLoadExhaustedException(cacheName, cacheKey));
                }))
                .doOnNext(lockNotExist -> log.debug(
                        "[Inmemory reactive cache initialize lock](Check whether any cache initialization running): " +
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
        return Mono.fromFuture(CompletableFuture.supplyAsync(() -> lockContainer.computeIfAbsent(cacheInitializeLockKey,
                        key -> new ConcurrentLinkedDeque<>()
                )))
                .map(deque -> deque.add(currentOperationId))
                .flatMap(__ -> Mono.defer(() -> Mono.just(lockContainer.get(cacheInitializeLockKey))
                                        .map(ConcurrentLinkedDeque::peekFirst)
                                )
                                .filter(value -> Objects.equals(value, currentOperationId))
                                .repeatWhenEmpty(Repeat.onlyIf(repeatContext -> true)
                                        .timeout(maxWaitingDuration)
                                        .backoff(Backoff.fixed(Duration.ofMillis(300)))
                                )
                                .switchIfEmpty(Mono.defer(() -> {
                                    log.error(
                                            "[Inmemory reactive cache initialize lock](Check whether any cache initialization running): " +
                                                    "Current operation is not the head of lock queue and reach the max waiting duration: {}, " +
                                                    "CacheName: {},CacheKey: {}, CurrentOperationId: {}",
                                            maxWaitingDuration,
                                            cacheName,
                                            cacheKey,
                                            currentOperationId
                                    );
                                    return Mono.just(lockContainer.get(cacheInitializeLockKey))
                                            .flatMap(deque -> Mono.fromFuture(CompletableFuture.runAsync(() ->
                                                                    deque.removeIf(value -> Objects.equals(value,
                                                                                    currentOperationId
                                                                            )
                                                                    ))
                                                            )
                                                            .then(Mono.error(new ReactiveCacheLoadExhaustedException(cacheName,
                                                                    cacheKey
                                                            )))
                                            );
                                }))
                )
                .doOnNext(operationId -> log.debug(
                        "[Inmemory reactive cache initialize lock](Lock initialization success): " +
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
        return Mono.fromFuture(CompletableFuture.supplyAsync(() -> lockContainer.get(cacheInitializeLockKey)))
                .map(ConcurrentLinkedDeque::pollFirst)
                .doOnNext(operationId -> log.debug(
                        "[Inmemory reactive cache initialize lock](Release initialization lock): " +
                                "CacheName: {}, CacheKey: {},LockedOperationId: {}",
                        cacheName,
                        cacheKey,
                        operationId
                ));
    }

}
