package pro.chenggang.project.reactive.cache.support.core;

import lombok.NonNull;
import reactor.core.publisher.Mono;

import java.util.Collection;

/**
 * The reactive cache manager
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
public interface ReactiveCacheManager {

    /**
     * Get the cache associated with the given name.
     *
     * @param name the cache identifier (must not be {@code null})
     * @return the associated cache, or {@code Mono.empty()} if such a cache
     * does not exist or could not be created
     */
    Mono<ReactiveCache> getCache(@NonNull String name);

    /**
     * Get a collection of the cache names known by this manager.
     *
     * @return the names of all caches known by the cache manager
     */
    Collection<String> getCacheNames();

}
