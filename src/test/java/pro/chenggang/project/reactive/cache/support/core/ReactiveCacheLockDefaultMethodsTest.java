package pro.chenggang.project.reactive.cache.support.core;

import org.junit.jupiter.api.Test;
import pro.chenggang.project.reactive.cache.support.BaseTest;
import pro.chenggang.project.reactive.cache.support.defaults.inmemory.InmemoryReactiveCacheLock;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
class ReactiveCacheLockDefaultMethodsTest extends BaseTest {

    ReactiveCacheLock reactiveCacheLock = new InmemoryReactiveCacheLock();

    @Test
    void testDecorateCacheInitializeLockKey() {
        String decoratedCacheInitializeLockKey = reactiveCacheLock.decorateCacheInitializeLockKey(cacheName,
                cacheKey
        );
        assertEquals(decoratedCacheInitializeLockKey, cacheName + ":INITIALIZE_LOCK:" + cacheKey);
    }

}