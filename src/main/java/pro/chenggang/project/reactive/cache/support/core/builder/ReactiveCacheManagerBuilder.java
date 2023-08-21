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
     * New inmemory reactive manager builder.
     *
     * @return the inmemory reactive manager builder
     */
    public static InmemoryReactiveManagerBuilder newInmemoryReactiveManagerBuilder() {
        return new InmemoryReactiveManagerBuilder();
    }

    /**
     * New caffeine reactive manager builder.
     *
     * @return the caffeine reactive manager builder
     */
    public static CaffeineReactiveManagerBuilder newCaffeineReactiveManagerBuilder() {
        return new CaffeineReactiveManagerBuilder();
    }

    /**
     * New redis reactive manager builder.
     *
     * @param reactiveRedisTemplate the reactive redis template
     * @return the redis reactive manager builder
     */
    public static RedisReactiveManagerBuilder newRedisReactiveManagerBuilder(@NonNull ReactiveRedisTemplate<String, Object> reactiveRedisTemplate) {
        return new RedisReactiveManagerBuilder(reactiveRedisTemplate);
    }

    /**
     * New custom reactive manager builder.
     *
     * @return the custom reactive manager builder
     */
    public static CustomReactiveManagerBuilder newCustomReactiveManagerBuilder() {
        return new CustomReactiveManagerBuilder();
    }

    /**
     * The base reactive manager builder.
     *
     * @param <B> the BaseReactiveManagerBuilder type
     */
    protected abstract static class BaseReactiveManagerBuilder<B extends BaseReactiveManagerBuilder<B>> {

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
     * The custom reactive manager builder.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CustomReactiveManagerBuilder extends BaseReactiveManagerBuilder<CustomReactiveManagerBuilder> {

        private ReactiveCacheLock reactiveCacheLock;
        private ReactiveCacheMonoAdapter reactiveCacheMonoAdapter;
        private ReactiveCacheFluxAdapter reactiveCacheFluxAdapter;

        /**
         * With reactive cache lock.
         *
         * @param reactiveCacheLock the reactive cache lock
         * @return the custom reactive manager builder
         */
        public CustomReactiveManagerBuilder withReactiveCacheLock(@NonNull ReactiveCacheLock reactiveCacheLock) {
            this.reactiveCacheLock = reactiveCacheLock;
            return this;
        }

        /**
         * With reactive cache mono adapter.
         *
         * @param reactiveCacheMonoAdapter the reactive cache mono adapter
         * @return the custom reactive manager builder
         */
        public CustomReactiveManagerBuilder withReactiveCacheMonoAdapter(@NonNull ReactiveCacheMonoAdapter reactiveCacheMonoAdapter) {
            this.reactiveCacheMonoAdapter = reactiveCacheMonoAdapter;
            return this;
        }

        /**
         * With reactive cache flux adapter.
         *
         * @param reactiveCacheFluxAdapter the reactive cache flux adapter
         * @return the custom reactive manager builder
         */
        public CustomReactiveManagerBuilder withReactiveCacheFluxAdapter(@NonNull ReactiveCacheFluxAdapter reactiveCacheFluxAdapter) {
            this.reactiveCacheFluxAdapter = reactiveCacheFluxAdapter;
            return this;
        }

        @Override
        public CustomReactiveManagerBuilder self() {
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
     * The inmemory reactive manager builder.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class InmemoryReactiveManagerBuilder extends BaseReactiveManagerBuilder<InmemoryReactiveManagerBuilder> {

        @Override
        public InmemoryReactiveManagerBuilder self() {
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
     * The caffeine reactive manager builder.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CaffeineReactiveManagerBuilder extends BaseReactiveManagerBuilder<CaffeineReactiveManagerBuilder> {

        @Override
        public CaffeineReactiveManagerBuilder self() {
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
     * The redis reactive manager builder.
     */
    public static class RedisReactiveManagerBuilder extends BaseReactiveManagerBuilder<RedisReactiveManagerBuilder> {

        @NonNull
        private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;
        @NonNull
        private final RedisReactiveCacheLock redisReactiveCacheLock;

        private RedisReactiveManagerBuilder(@NonNull ReactiveRedisTemplate<String, Object> reactiveRedisTemplate) {
            this.reactiveRedisTemplate = reactiveRedisTemplate;
            this.redisReactiveCacheLock = new RedisReactiveCacheLock(reactiveRedisTemplate);
        }

        @Override
        public RedisReactiveManagerBuilder self() {
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
