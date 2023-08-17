package pro.chenggang.project.reactive.cache.support;

import java.time.Duration;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class BaseTest {

    protected final String cacheName = "CACHE_NAME";
    protected final String cacheKey = "CACHE_KEY";
    protected final Duration maxWaitingDuration = Duration.ofSeconds(5);
}
