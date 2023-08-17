package pro.chenggang.project.reactive.cache.support.defaults;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pro.chenggang.project.reactive.cache.support.BaseTest;
import pro.chenggang.project.reactive.cache.support.core.ReactiveCacheManager;
import pro.chenggang.project.reactive.cache.support.defaults.inmemory.InmemoryReactiveCacheLock;
import pro.chenggang.project.reactive.cache.support.defaults.inmemory.InmemoryReactiveCacheManagerAdapter;
import reactor.test.StepVerifier;

import java.util.Collection;
import java.util.Optional;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
class DefaultReactiveCacheManagerTest extends BaseTest {

    ReactiveCacheManager reactiveCacheManager = new DefaultReactiveCacheManager(new InmemoryReactiveCacheManagerAdapter(
            maxWaitingDuration, new InmemoryReactiveCacheLock())
    );

    @Test
    void getCache() {
        reactiveCacheManager.getCache(cacheName)
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void getCacheNames() {
        Collection<String> cacheNames = reactiveCacheManager.getCacheNames();
        Assertions.assertEquals(cacheNames.size(), 0);
        reactiveCacheManager.getCache(cacheName)
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();
        cacheNames = reactiveCacheManager.getCacheNames();
        Assertions.assertEquals(cacheNames.size(), 1);
        Optional<String> optionalFirst = cacheNames.stream()
                .findFirst();
        Assertions.assertTrue(optionalFirst.isPresent());
        Assertions.assertEquals(optionalFirst.get(), cacheName);
    }
}