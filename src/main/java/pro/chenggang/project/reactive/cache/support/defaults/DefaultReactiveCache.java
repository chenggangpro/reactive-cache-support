package pro.chenggang.project.reactive.cache.support.defaults;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import pro.chenggang.project.reactive.cache.support.core.ReactiveCache;
import pro.chenggang.project.reactive.cache.support.core.ReactiveCacheLock;
import pro.chenggang.project.reactive.cache.support.core.adapter.ReactiveCacheFluxAdapter;
import pro.chenggang.project.reactive.cache.support.core.adapter.ReactiveCacheMonoAdapter;
import pro.chenggang.project.reactive.cache.support.core.executor.ReactiveFluxCache;
import pro.chenggang.project.reactive.cache.support.core.executor.ReactiveMonoCache;
import pro.chenggang.project.reactive.cache.support.defaults.executor.DefaultReactiveFluxCache;
import pro.chenggang.project.reactive.cache.support.defaults.executor.DefaultReactiveMonoCache;

import java.time.Duration;

/**
 * The default implementation of reactive cache
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public class DefaultReactiveCache implements ReactiveCache {
    /**
     * The reactive mono cache
     */
    @NonNull
    private final ReactiveMonoCache reactiveMonoCache;
    /**
     * The reactive flux cache
     */
    @NonNull
    private final ReactiveFluxCache reactiveFluxCache;

    public DefaultReactiveCache(@NonNull String cacheName,
                                @NonNull Duration maxWaitingDuration,
                                @NonNull ReactiveCacheLock reactiveCacheLock,
                                @NonNull ReactiveCacheMonoAdapter reactiveCacheMonoAdapter,
                                @NonNull ReactiveCacheFluxAdapter reactiveCacheFluxAdapter) {
        this.reactiveMonoCache = new DefaultReactiveMonoCache(cacheName,
                maxWaitingDuration,
                reactiveCacheLock,
                reactiveCacheMonoAdapter
        );
        this.reactiveFluxCache = new DefaultReactiveFluxCache(cacheName,
                maxWaitingDuration,
                reactiveCacheLock,
                reactiveCacheFluxAdapter
        );
    }


    @Override
    public ReactiveMonoCache monoCache() {
        return this.reactiveMonoCache;
    }

    @Override
    public ReactiveFluxCache fluxCache() {
        return this.reactiveFluxCache;
    }
}
