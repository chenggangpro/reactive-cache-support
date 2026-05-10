package pro.chenggang.project.reactive.cache.support.defaults;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pro.chenggang.project.reactive.cache.support.core.ReactiveCache;
import pro.chenggang.project.reactive.cache.support.core.ReactiveCacheManager;
import pro.chenggang.project.reactive.cache.support.core.adapter.ReactiveCacheManagerAdapter;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The default reactive cache manager.
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultReactiveCacheManager implements ReactiveCacheManager {

    private final Map<String, ReactiveCache> cacheContainer = new ConcurrentHashMap<>();

    /**
     * The reactive cache manager adapter
     */
    @NonNull
    private final ReactiveCacheManagerAdapter reactiveCacheManagerAdapter;

    @NonNull
    @Override
    public Mono<ReactiveCache> getCache(@NonNull String name) {
        return Mono.fromCallable(() -> cacheContainer.computeIfAbsent(name, reactiveCacheManagerAdapter::initializeReactiveCache));
    }

    @Override
    public Collection<String> getCacheNames() {
        return cacheContainer.keySet();
    }
}
