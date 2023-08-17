package pro.chenggang.project.reactive.cache.support.defaults.inmemory;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pro.chenggang.project.reactive.cache.support.BaseTest;
import pro.chenggang.project.reactive.cache.support.core.ReactiveCache;
import pro.chenggang.project.reactive.cache.support.defaults.DefaultReactiveCache;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
class InmemoryReactiveCacheManagerAdapterTest extends BaseTest {

    InmemoryReactiveCacheManagerAdapter inmemoryReactiveCacheManagerAdapter = new InmemoryReactiveCacheManagerAdapter(
            maxWaitingDuration, new InmemoryReactiveCacheLock()
    );

    @Test
    void initializeReactiveCache() {
        ReactiveCache reactiveCache = inmemoryReactiveCacheManagerAdapter.initializeReactiveCache(cacheName);
        Assertions.assertNotNull(reactiveCache);
        Assertions.assertEquals(reactiveCache.getClass(), DefaultReactiveCache.class);
    }
}