package pro.chenggang.project.reactive.cache.support.core.executor;

import lombok.NonNull;
import pro.chenggang.project.reactive.cache.support.exception.NoSuchCachedReactiveDataException;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * The reactive mono cache.
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
public interface ReactiveMonoCache {

    /**
     * Gets cached mono or {@code Mono.empty()} if cached data does not found.
     *
     * @param <T>          the cached mono's data type
     * @param cacheKey     the cache key
     * @return the cached mono
     * @throws NoSuchCachedReactiveDataException if there is no cached data found and not be initialized,
     *                                           this exception is intended to distinguish from the empty single
     */
    <T> Mono<T> get(@NonNull String cacheKey);

    /**
     * Cache from source mono if cache didn't exist.
     * Gets cached mono or {@code Mono.empty()} if cached data does not found.
     *
     * @param <T>           the cached mono's data type
     * @param cacheKey      the cache key
     * @param cacheDuration the cache duration
     * @param sourceMono    the source mono
     * @return the cached mono
     */
    <T> Mono<T> cacheIfNecessary(@NonNull String cacheKey,
                                 @NonNull Duration cacheDuration,
                                 @NonNull Mono<T> sourceMono);

    /**
     * Evict cache.
     *
     * @param cacheKey the cache key
     * @return Void mono
     */
    Mono<Void> evictCache(@NonNull String cacheKey);
}
