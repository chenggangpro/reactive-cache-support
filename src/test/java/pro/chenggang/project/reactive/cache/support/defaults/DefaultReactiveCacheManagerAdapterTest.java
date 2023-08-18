package pro.chenggang.project.reactive.cache.support.defaults;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pro.chenggang.project.reactive.cache.support.BaseTest;
import pro.chenggang.project.reactive.cache.support.core.ReactiveCache;
import pro.chenggang.project.reactive.cache.support.defaults.inmemory.InmemoryReactiveCacheFluxAdapter;
import pro.chenggang.project.reactive.cache.support.defaults.inmemory.InmemoryReactiveCacheLock;
import pro.chenggang.project.reactive.cache.support.defaults.inmemory.InmemoryReactiveCacheMonoAdapter;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
class DefaultReactiveCacheManagerAdapterTest extends BaseTest {

    DefaultReactiveCacheManagerAdapter defaultReactiveCacheManagerAdapter = new DefaultReactiveCacheManagerAdapter(
            maxWaitingDuration,
            new InmemoryReactiveCacheLock(),
            new InmemoryReactiveCacheMonoAdapter(),
            new InmemoryReactiveCacheFluxAdapter()
    );

    @Test
    void initializeReactiveCache() {
        ReactiveCache reactiveCache = defaultReactiveCacheManagerAdapter.initializeReactiveCache(cacheName);
        Assertions.assertNotNull(reactiveCache);
        Assertions.assertEquals(reactiveCache.getClass(), DefaultReactiveCache.class);
    }
}