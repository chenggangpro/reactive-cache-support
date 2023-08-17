package pro.chenggang.project.reactive.cache.support.exception;

import lombok.Getter;

/**
 * No such cached reactive data exception
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
@Getter
public class NoSuchCachedReactiveDataException extends RuntimeException {

    private static final long serialVersionUID = -970142528523070375L;

    private final String cacheName;
    private final String cacheKey;

    public NoSuchCachedReactiveDataException(String cacheName, String cacheKey) {
        super(String.format("There is no cached reactive data for key '%s' of Cache '%s' exhausted", cacheKey, cacheName));
        this.cacheName = cacheName;
        this.cacheKey = cacheKey;
    }

}