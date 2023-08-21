package pro.chenggang.project.reactive.cache.support.defaults.caffeine;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pro.chenggang.project.reactive.cache.support.BaseTest;
import pro.chenggang.project.reactive.cache.support.core.ReactiveCache;
import pro.chenggang.project.reactive.cache.support.defaults.DefaultReactiveCache;
import pro.chenggang.project.reactive.cache.support.defaults.inmemory.InmemoryReactiveCacheLock;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
class CaffeineReactiveCacheManagerAdapterTest extends BaseTest {

    CaffeineReactiveCacheManagerAdapter caffeineReactiveCacheManagerAdapter = new CaffeineReactiveCacheManagerAdapter(
            maxWaitingDuration, new InmemoryReactiveCacheLock()
    );

    @Test
    void initializeReactiveCache() {
        ReactiveCache reactiveCache = caffeineReactiveCacheManagerAdapter.initializeReactiveCache(cacheName);
        Assertions.assertNotNull(reactiveCache);
        Assertions.assertEquals(reactiveCache.getClass(), DefaultReactiveCache.class);
    }
}