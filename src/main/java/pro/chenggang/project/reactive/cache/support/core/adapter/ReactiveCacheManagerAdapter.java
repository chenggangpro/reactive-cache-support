package pro.chenggang.project.reactive.cache.support.core.adapter;

import lombok.NonNull;
import pro.chenggang.project.reactive.cache.support.core.ReactiveCache;

/**
 * The Reactive cache manager adapter.
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
public interface ReactiveCacheManagerAdapter {

    /**
     * Initialize a new reactive cache with given name.
     *
     * @param name the cache name
     * @return the reactive cache
     */
    ReactiveCache initializeReactiveCache(@NonNull String name);
}
