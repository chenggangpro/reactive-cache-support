package pro.chenggang.project.reactive.cache.support.core.builder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import pro.chenggang.project.reactive.cache.support.core.ReactiveCacheLock;
import pro.chenggang.project.reactive.cache.support.core.ReactiveCacheManager;
import pro.chenggang.project.reactive.cache.support.core.adapter.ReactiveCacheFluxAdapter;
import pro.chenggang.project.reactive.cache.support.core.adapter.ReactiveCacheMonoAdapter;
import pro.chenggang.project.reactive.cache.support.defaults.DefaultReactiveCacheManager;
import pro.chenggang.project.reactive.cache.support.defaults.DefaultReactiveCacheManagerAdapter;
import pro.chenggang.project.reactive.cache.support.defaults.caffeine.CaffeineReactiveCacheManagerAdapter;
import pro.chenggang.project.reactive.cache.support.defaults.inmemory.InmemoryReactiveCacheLock;
import pro.chenggang.project.reactive.cache.support.defaults.inmemory.InmemoryReactiveCacheManagerAdapter;
import pro.chenggang.project.reactive.cache.support.defaults.redis.RedisReactiveCacheLock;
import pro.chenggang.project.reactive.cache.support.defaults.redis.RedisReactiveCacheManagerAdapter;

import java.time.Duration;

/**
 * The abstract reactive cache manger builder
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class ReactiveCacheManagerBuilder {

    /**
     * New inmemory reactive cache manager builder.
     *
     * @return the inmemory reactive cache manager builder
     */
    public static InmemoryReactiveCacheManagerBuilder newInmemoryReactiveCacheManagerBuilder() {
        return new InmemoryReactiveCacheManagerBuilder();
    }

    /**
     * New caffeine reactive cache manager builder.
     *
     * @return the caffeine reactive cache manager builder
     */
    public static CaffeineReactiveCacheManagerBuilder newCaffeineReactiveCacheManagerBuilder() {
        return new CaffeineReactiveCacheManagerBuilder();
    }

    /**
     * New redis reactive cache manager builder.
     *
     * @param reactiveRedisTemplate the reactive redis template
     * @return the redis reactive cache manager builder
     */
    public static RedisReactiveCacheManagerBuilder newRedisReactiveCacheManagerBuilder(@NonNull ReactiveRedisTemplate<String, Object> reactiveRedisTemplate) {
        return new RedisReactiveCacheManagerBuilder(reactiveRedisTemplate);
    }

    /**
     * New custom reactive cache manager builder.
     *
     * @return the custom reactive cache manager builder
     */
    public static CustomReactiveCacheManagerBuilder newCustomReactiveCacheManagerBuilder() {
        return new CustomReactiveCacheManagerBuilder();
    }

    /**
     * The base reactive cache manager builder.
     *
     * @param <B> the BaseReactiveManagerBuilder type
     */
    protected abstract static class BaseReactiveCacheManagerBuilder<B extends BaseReactiveCacheManagerBuilder<B>> {

        /**
         * The Max waiting duration.
         */
        protected Duration maxWaitingDuration = Duration.ofSeconds(3);

        /**
         * With max waiting duration.Default is {@code Duration.ofSeconds(3)}
         *
         * @param maxWaitingDuration the max waiting duration
         * @return the builder
         */
        public B withMaxWaitingDuration(@NonNull Duration maxWaitingDuration) {
            this.maxWaitingDuration = maxWaitingDuration;
            return self();
        }

        /**
         * Self builder.
         *
         * @return the builder
         */
        protected abstract B self();

        /**
         * Build reactive cache manager.
         *
         * @return the reactive cache manager
         */
        public abstract ReactiveCacheManager build();
    }

    /**
     * The custom reactive cache manager builder.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CustomReactiveCacheManagerBuilder extends BaseReactiveCacheManagerBuilder<CustomReactiveCacheManagerBuilder> {

        private ReactiveCacheLock reactiveCacheLock;
        private ReactiveCacheMonoAdapter reactiveCacheMonoAdapter;
        private ReactiveCacheFluxAdapter reactiveCacheFluxAdapter;

        /**
         * With reactive cache lock.
         *
         * @param reactiveCacheLock the reactive cache lock
         * @return the custom reactive manager builder
         */
        public CustomReactiveCacheManagerBuilder withReactiveCacheLock(@NonNull ReactiveCacheLock reactiveCacheLock) {
            this.reactiveCacheLock = reactiveCacheLock;
            return this;
        }

        /**
         * With reactive cache mono adapter.
         *
         * @param reactiveCacheMonoAdapter the reactive cache mono adapter
         * @return the custom reactive manager builder
         */
        public CustomReactiveCacheManagerBuilder withReactiveCacheMonoAdapter(@NonNull ReactiveCacheMonoAdapter reactiveCacheMonoAdapter) {
            this.reactiveCacheMonoAdapter = reactiveCacheMonoAdapter;
            return this;
        }

        /**
         * With reactive cache flux adapter.
         *
         * @param reactiveCacheFluxAdapter the reactive cache flux adapter
         * @return the custom reactive manager builder
         */
        public CustomReactiveCacheManagerBuilder withReactiveCacheFluxAdapter(@NonNull ReactiveCacheFluxAdapter reactiveCacheFluxAdapter) {
            this.reactiveCacheFluxAdapter = reactiveCacheFluxAdapter;
            return this;
        }

        @Override
        public CustomReactiveCacheManagerBuilder self() {
            return this;
        }

        @Override
        public ReactiveCacheManager build() {
            return new DefaultReactiveCacheManager(new DefaultReactiveCacheManagerAdapter(maxWaitingDuration,
                    reactiveCacheLock,
                    reactiveCacheMonoAdapter,
                    reactiveCacheFluxAdapter
            ));
        }
    }


    /**
     * The inmemory reactive cache manager builder.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class InmemoryReactiveCacheManagerBuilder extends BaseReactiveCacheManagerBuilder<InmemoryReactiveCacheManagerBuilder> {

        @Override
        public InmemoryReactiveCacheManagerBuilder self() {
            return this;
        }

        @Override
        public ReactiveCacheManager build() {
            return new DefaultReactiveCacheManager(new InmemoryReactiveCacheManagerAdapter(maxWaitingDuration,
                    new InmemoryReactiveCacheLock()
            ));
        }
    }

    /**
     * The caffeine reactive cache manager builder.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CaffeineReactiveCacheManagerBuilder extends BaseReactiveCacheManagerBuilder<CaffeineReactiveCacheManagerBuilder> {

        @Override
        public CaffeineReactiveCacheManagerBuilder self() {
            return this;
        }

        @Override
        public ReactiveCacheManager build() {
            return new DefaultReactiveCacheManager(new CaffeineReactiveCacheManagerAdapter(maxWaitingDuration,
                    new InmemoryReactiveCacheLock()
            ));
        }
    }

    /**
     * The redis reactive cache manager builder.
     */
    public static class RedisReactiveCacheManagerBuilder extends BaseReactiveCacheManagerBuilder<RedisReactiveCacheManagerBuilder> {

        @NonNull
        private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;
        @NonNull
        private final RedisReactiveCacheLock redisReactiveCacheLock;

        private RedisReactiveCacheManagerBuilder(@NonNull ReactiveRedisTemplate<String, Object> reactiveRedisTemplate) {
            this.reactiveRedisTemplate = reactiveRedisTemplate;
            this.redisReactiveCacheLock = new RedisReactiveCacheLock(reactiveRedisTemplate);
        }

        @Override
        public RedisReactiveCacheManagerBuilder self() {
            return this;
        }

        @Override
        public ReactiveCacheManager build() {
            return new DefaultReactiveCacheManager(new RedisReactiveCacheManagerAdapter(maxWaitingDuration,
                    redisReactiveCacheLock,
                    reactiveRedisTemplate
            ));
        }
    }
}
