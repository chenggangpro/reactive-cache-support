package pro.chenggang.project.reactive.cache.support.toolkit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pro.chenggang.project.reactive.cache.support.BaseTest;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
class AutoExpiredDataCacheTest extends BaseTest {

    AutoExpiredDataCache<Boolean> autoExpiredDataCache = AutoExpiredDataCache.newInstance();

    @Test
    void initialization(){
        Assertions.assertThrows(IllegalArgumentException.class, () -> autoExpiredDataCache.putData(cacheKey,true,Duration.ZERO));
        Assertions.assertThrows(IllegalArgumentException.class, () -> autoExpiredDataCache.putData(cacheKey,true,Duration.ofSeconds(-1)));
    }

    @Test
    void hasData() {
        boolean hasData = autoExpiredDataCache.hasData(cacheKey);
        assertFalse(hasData);
    }

    @Test
    void putDataIfAbsent() throws Exception{
        Boolean cachedData = autoExpiredDataCache.putData(cacheKey, true, Duration.ofSeconds(1));
        assertTrue(cachedData);
        boolean hasData = autoExpiredDataCache.hasData(cacheKey);
        assertTrue(hasData);
        TimeUnit.SECONDS.sleep(2);
        hasData = autoExpiredDataCache.hasData(cacheKey);
        assertFalse(hasData);
    }

    @Test
    void putDataIfPresent() throws Exception{
        Boolean cachedData = autoExpiredDataCache.putData(cacheKey, true, Duration.ofSeconds(1));
        assertTrue(cachedData);
        boolean hasData = autoExpiredDataCache.hasData(cacheKey);
        assertTrue(hasData);
        cachedData = autoExpiredDataCache.putData(cacheKey, false, Duration.ofSeconds(1));
        assertFalse(cachedData);
        hasData = autoExpiredDataCache.hasData(cacheKey);
        assertTrue(hasData);
    }

    @Test
    void getData() throws Exception {
        Boolean cachedData = autoExpiredDataCache.putData(cacheKey, true, Duration.ofSeconds(1));
        assertTrue(cachedData);
        boolean hasData = autoExpiredDataCache.hasData(cacheKey);
        assertTrue(hasData);
        Optional<Boolean> optionalCachedData = autoExpiredDataCache.getData(cacheKey);
        assertTrue(optionalCachedData.isPresent());
        assertTrue(optionalCachedData.get());
        TimeUnit.SECONDS.sleep(2);
        optionalCachedData = autoExpiredDataCache.getData(cacheKey);
        assertTrue(optionalCachedData.isEmpty());
    }

    @Test
    void removeData() {
        Boolean cachedData = autoExpiredDataCache.putData(cacheKey, true, Duration.ofSeconds(3));
        assertTrue(cachedData);
        boolean hasData = autoExpiredDataCache.hasData(cacheKey);
        assertTrue(hasData);
        autoExpiredDataCache.removeData(cacheKey);
        hasData = autoExpiredDataCache.hasData(cacheKey);
        assertFalse(hasData);
    }
}