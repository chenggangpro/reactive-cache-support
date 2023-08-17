package pro.chenggang.project.reactive.cache.support.core;

import lombok.NonNull;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Reactive cache lock
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
public interface ReactiveCacheLock {

    /**
     * Decorate cache initialize lock key.
     *
     * @param cacheName the cache name
     * @param cacheKey  the cache key
     * @return the decorated cache initialize lock key
     */
    default String decorateCacheInitializeLockKey(@NonNull String cacheName, @NonNull String cacheKey) {
        return cacheName + ":INITIALIZE_LOCK:" + cacheKey;
    }

    /**
     * Check initialize lock.
     *
     * @param cacheName          the cache name
     * @param cacheKey           the cache key
     * @param maxWaitingDuration the max waiting duration
     * @return the Void
     */
    Mono<Void> checkInitializeLock(@NonNull String cacheName,
                                   @NonNull String cacheKey,
                                   @NonNull Duration maxWaitingDuration);

    /**
     * Try lock initialize lock.
     *
     * @param cacheName          the cache name
     * @param cacheKey           the cache key
     * @param maxWaitingDuration the max waiting duration
     * @return the current operation id
     */
    Mono<String> tryLockInitializeLock(@NonNull String cacheName,
                                       @NonNull String cacheKey,
                                       @NonNull Duration maxWaitingDuration);

    /**
     * Release initialize lock.
     *
     * @param cacheName the cache name
     * @param cacheKey  the cache key
     * @return the released operation id
     */
    Mono<String> releaseInitializeLock(@NonNull String cacheName, @NonNull String cacheKey);

}
