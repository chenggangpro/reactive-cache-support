package pro.chenggang.project.reactive.cache.support.defaults;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pro.chenggang.project.reactive.cache.support.core.ReactiveCache;
import pro.chenggang.project.reactive.cache.support.core.ReactiveCacheManager;
import pro.chenggang.project.reactive.cache.support.core.adapter.ReactiveCacheManagerAdapter;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

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

    private final AsyncCache<String, ReactiveCache> cacheContainer = Caffeine.newBuilder()
            .buildAsync();

    /**
     * The reactive cache manager adapter
     */
    @NonNull
    private final ReactiveCacheManagerAdapter reactiveCacheManagerAdapter;

    @NonNull
    @Override
    public Mono<ReactiveCache> getCache(@NonNull String name) {
        CompletableFuture<ReactiveCache> cacheCompletableFuture = cacheContainer.get(name,
                (key, executor) -> Mono.defer(
                                () -> Mono.just(key)
                                        .map(reactiveCacheManagerAdapter::initializeReactiveCache)
                        )
                        .toFuture()
        );
        return Mono.fromFuture(cacheCompletableFuture);
    }

    @Override
    public Collection<String> getCacheNames() {
        return cacheContainer.asMap()
                .keySet();
    }
}
