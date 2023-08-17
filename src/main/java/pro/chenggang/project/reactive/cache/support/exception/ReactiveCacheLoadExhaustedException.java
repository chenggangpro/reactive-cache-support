package pro.chenggang.project.reactive.cache.support.exception;

import lombok.Getter;

/**
 * Waiting load cache exhausted exception
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
@Getter
public class ReactiveCacheLoadExhaustedException extends RuntimeException {

    private static final long serialVersionUID = 5589465903249165312L;

    private final String cacheName;
    private final String cacheKey;

    public ReactiveCacheLoadExhaustedException(String cacheName, String cacheKey) {
        super(String.format("Waiting load cache for key '%s' of Cache '%s' exhausted", cacheKey, cacheName));
        this.cacheName = cacheName;
        this.cacheKey = cacheKey;
    }

}