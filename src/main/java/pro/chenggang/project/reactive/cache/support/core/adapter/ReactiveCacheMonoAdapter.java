package pro.chenggang.project.reactive.cache.support.core.adapter;

import lombok.NonNull;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * The Reactive cache executor Mono adapter.
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
public interface ReactiveCacheMonoAdapter {

    /**
     * Whether cache has data .
     *
     * @param cacheKey the cache key
     * @return true if cache exists
     */
    Mono<Boolean> hasData(@NonNull String cacheKey);

    /**
     * Load data .
     *
     * @param cacheKey the cache key
     * @return the cached Mono
     */
    <T> Mono<T> loadData(@NonNull String cacheKey);

    /**
     * Cache data (Mono).
     *
     * @param cacheKey        the cache key
     * @param cacheDuration   the cache expired duration
     * @param sourcePublisher the source mono
     * @return the cached Mono
     */
    <T> Mono<T> cacheData(@NonNull String cacheKey, @NonNull Duration cacheDuration, @NonNull Mono<T> sourcePublisher);

    /**
     * Cleanup cache data.
     *
     * @param cacheKey the cache key
     * @return the Void
     */
    Mono<Void> cleanupData(@NonNull String cacheKey);

}
