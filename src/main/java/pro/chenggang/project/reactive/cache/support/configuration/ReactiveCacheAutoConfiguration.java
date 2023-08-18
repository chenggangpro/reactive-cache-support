package pro.chenggang.project.reactive.cache.support.configuration;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import pro.chenggang.project.reactive.cache.support.configuration.properties.ReactiveCacheSupportProperties;
import pro.chenggang.project.reactive.cache.support.core.ReactiveCacheManager;
import pro.chenggang.project.reactive.cache.support.core.builder.ReactiveCacheManagerBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static pro.chenggang.project.reactive.cache.support.configuration.properties.ReactiveCacheSupportProperties.PREFIX;

/**
 * The reactive cache autoconfiguration
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
@ConditionalOnProperty(prefix = PREFIX, value = "enabled", havingValue = "true")
@ConditionalOnClass({Flux.class, Mono.class})
@AutoConfiguration
@Configuration
public class ReactiveCacheAutoConfiguration {

    @ConfigurationProperties(prefix = PREFIX)
    @Bean
    public ReactiveCacheSupportProperties reactiveCacheSupportProperties() {
        return new ReactiveCacheSupportProperties();
    }

    @ConditionalOnProperty(prefix = PREFIX, value = "type", havingValue = "inmemory")
    @ConditionalOnMissingBean(ReactiveCacheManager.class)
    @Bean
    public ReactiveCacheManager inmemoryReactiveCacheManager(ReactiveCacheSupportProperties reactiveCacheSupportProperties) {
        return ReactiveCacheManagerBuilder.newInmemoryReactiveManagerBuilder()
                .withMaxWaitingDuration(reactiveCacheSupportProperties.getMaxWaitingDuration())
                .build();
    }

    @SuppressWarnings("all")
    @ConditionalOnProperty(prefix = PREFIX, value = "type", havingValue = "redis")
    @ConditionalOnBean(ReactiveRedisTemplate.class)
    @ConditionalOnMissingBean(ReactiveCacheManager.class)
    @Bean
    public ReactiveCacheManager redisReactiveCacheManager(ReactiveCacheSupportProperties reactiveCacheSupportProperties,
                                                          ReactiveRedisTemplate reactiveRedisTemplate) {
        return ReactiveCacheManagerBuilder.newRedisReactiveManagerBuilder(reactiveRedisTemplate)
                .withMaxWaitingDuration(reactiveCacheSupportProperties.getMaxWaitingDuration())
                .build();
    }
}
