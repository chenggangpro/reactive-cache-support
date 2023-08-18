package pro.chenggang.project.reactive.cache.support.configuration.properties;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.time.Duration;

/**
 * The reactive cache support configure properties
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
@Getter
@Setter
public class ReactiveCacheSupportProperties {

    public static final String PREFIX = "reactive.cache";

    private boolean enabled = false;

    /**
     * The max waiting duration
     */
    @NonNull
    private Duration maxWaitingDuration = Duration.ofSeconds(3);

    /**
     * The reactive cache type
     */
    @NonNull
    private ReactiveCacheType type = ReactiveCacheType.inmemory;

    /**
     * The reactive cache type
     */
    public enum ReactiveCacheType {

        /**
         * The inmemory reactive cache type
         */
        inmemory,

        /**
         * The redis reactive cache type
         */
        redis,

        ;
    }
}
