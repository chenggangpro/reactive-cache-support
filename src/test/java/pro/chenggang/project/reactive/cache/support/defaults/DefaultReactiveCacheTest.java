package pro.chenggang.project.reactive.cache.support.defaults;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pro.chenggang.project.reactive.cache.support.BaseTest;
import pro.chenggang.project.reactive.cache.support.core.ReactiveCache;
import pro.chenggang.project.reactive.cache.support.core.executor.ReactiveFluxCache;
import pro.chenggang.project.reactive.cache.support.core.executor.ReactiveMonoCache;
import pro.chenggang.project.reactive.cache.support.defaults.executor.DefaultReactiveFluxCache;
import pro.chenggang.project.reactive.cache.support.defaults.executor.DefaultReactiveMonoCache;
import pro.chenggang.project.reactive.cache.support.defaults.inmemory.InmemoryReactiveCacheFluxAdapter;
import pro.chenggang.project.reactive.cache.support.defaults.inmemory.InmemoryReactiveCacheLock;
import pro.chenggang.project.reactive.cache.support.defaults.inmemory.InmemoryReactiveCacheMonoAdapter;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
class DefaultReactiveCacheTest extends BaseTest {

    ReactiveCache reactiveCache = new DefaultReactiveCache(cacheName,
            maxWaitingDuration,
            new InmemoryReactiveCacheLock(),
            new InmemoryReactiveCacheMonoAdapter(),
            new InmemoryReactiveCacheFluxAdapter()
    );

    @Test
    void monoCache() {
        ReactiveMonoCache reactiveMonoCache = reactiveCache.monoCache();
        Assertions.assertNotNull(reactiveMonoCache);
        Assertions.assertEquals(reactiveMonoCache.getClass(), DefaultReactiveMonoCache.class);
    }

    @Test
    void fluxCache() {
        ReactiveFluxCache reactiveFluxCache = reactiveCache.fluxCache();
        Assertions.assertNotNull(reactiveFluxCache);
        Assertions.assertEquals(reactiveFluxCache.getClass(), DefaultReactiveFluxCache.class);
    }
}