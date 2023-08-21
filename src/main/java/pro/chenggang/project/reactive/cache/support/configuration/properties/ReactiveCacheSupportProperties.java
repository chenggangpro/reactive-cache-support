package pro.chenggang.project.reactive.cache.support.configuration.properties;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
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
    @NotNull(message = "Max waiting duration cloud not be null")
    private Duration maxWaitingDuration = Duration.ofSeconds(3);

    /**
     * The reactive cache type
     */
    @NotNull(message = "Reactive cache type could not be null")
    private ReactiveCacheType type;

    /**
     * The reactive cache type enum
     */
    public enum ReactiveCacheType {

        /**
         * The inmemory reactive cache type
         */
        inmemory,

        /**
         * The caffeine reactive cache type
         */
        caffeine,

        /**
         * The redis reactive cache type
         */
        redis,

        ;
    }
}
