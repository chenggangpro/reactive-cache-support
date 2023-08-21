package pro.chenggang.project.reactive.cache.support.defaults.caffeine;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pro.chenggang.project.reactive.cache.support.core.ReactiveCache;
import pro.chenggang.project.reactive.cache.support.core.adapter.ReactiveCacheManagerAdapter;
import pro.chenggang.project.reactive.cache.support.defaults.DefaultReactiveCache;
import pro.chenggang.project.reactive.cache.support.defaults.inmemory.InmemoryReactiveCacheLock;

import java.time.Duration;

/**
 * The inmemory reactive cache manager adapter by using caffeine.
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class CaffeineReactiveCacheManagerAdapter implements ReactiveCacheManagerAdapter {

    @NonNull
    private final Duration maxWaitingDuration;
    @NonNull
    private final InmemoryReactiveCacheLock inmemoryReactiveCacheLock;

    @Override
    public ReactiveCache initializeReactiveCache(@NonNull String name) {
        return new DefaultReactiveCache(name,
                maxWaitingDuration,
                inmemoryReactiveCacheLock,
                new CaffeineReactiveCacheMonoAdapter(),
                new CaffeineReactiveCacheFluxAdapter()
        );
    }
}
