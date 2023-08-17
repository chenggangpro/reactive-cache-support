package pro.chenggang.project.reactive.cache.support.core.executor;

import lombok.NonNull;
import pro.chenggang.project.reactive.cache.support.exception.NoSuchCachedReactiveDataException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * The reactive flux cache.
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
public interface ReactiveFluxCache {

    /**
     * Gets cached flux or {@code Flux.empty()}  if cached data does not found.
     *
     * @param <T>      the cached flux's data type
     * @param cacheKey the cache key
     * @return the cached flux
     * @throws NoSuchCachedReactiveDataException if there is no cached data found,
     *                                           this exception is supposed to distinguish from the empty single
     */
    <T> Flux<T> get(@NonNull String cacheKey);

    /**
     * Cache from source flux if cache didn't exist.
     * Gets cached flux or {@code Flux.empty()} if cached data does not found.
     *
     * @param <T>           the cached flux's data type
     * @param cacheKey      the cache key
     * @param cacheDuration the cache duration
     * @param sourceFlux    the source flux
     * @return the cached flux
     */
    <T> Flux<T> cacheIfNecessary(@NonNull String cacheKey,
                                 @NonNull Duration cacheDuration,
                                 @NonNull Flux<T> sourceFlux);

    /**
     * Evict cache.
     *
     * @param cacheKey the cache key
     * @return Void mono
     */
    Mono<Void> evictCache(@NonNull String cacheKey);
}
