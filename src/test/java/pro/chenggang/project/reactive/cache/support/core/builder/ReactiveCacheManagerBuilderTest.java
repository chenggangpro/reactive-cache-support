package pro.chenggang.project.reactive.cache.support.core.builder;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import pro.chenggang.project.reactive.cache.support.core.ReactiveCacheManager;
import pro.chenggang.project.reactive.cache.support.core.builder.ReactiveCacheManagerBuilder.CaffeineReactiveCacheManagerBuilder;
import pro.chenggang.project.reactive.cache.support.core.builder.ReactiveCacheManagerBuilder.CustomReactiveCacheManagerBuilder;
import pro.chenggang.project.reactive.cache.support.core.builder.ReactiveCacheManagerBuilder.InmemoryReactiveCacheManagerBuilder;
import pro.chenggang.project.reactive.cache.support.core.builder.ReactiveCacheManagerBuilder.RedisReactiveCacheManagerBuilder;
import pro.chenggang.project.reactive.cache.support.defaults.inmemory.InmemoryReactiveCacheFluxAdapter;
import pro.chenggang.project.reactive.cache.support.defaults.inmemory.InmemoryReactiveCacheLock;
import pro.chenggang.project.reactive.cache.support.defaults.inmemory.InmemoryReactiveCacheMonoAdapter;

import java.time.Duration;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ReactiveCacheManagerBuilderTest {

    ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    @BeforeAll
    public void beforeAll() {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration("127.0.0.1", 6379);
        LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(redisConfig);
        lettuceConnectionFactory.afterPropertiesSet();
        GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer = new GenericJackson2JsonRedisSerializer(
                new ObjectMapper());
        StringRedisSerializer stringRedisSerializer = StringRedisSerializer.UTF_8;
        RedisSerializationContext<String, Object> serializationContext = RedisSerializationContext
                .<String, Object>newSerializationContext()
                .key(stringRedisSerializer)
                .value(genericJackson2JsonRedisSerializer)
                .hashKey(stringRedisSerializer)
                .hashValue(genericJackson2JsonRedisSerializer)
                .build();
        reactiveRedisTemplate = new ReactiveRedisTemplate<>(lettuceConnectionFactory, serializationContext);
    }

    @Test
    void newInmemoryReactiveCacheManagerBuilder() {
        InmemoryReactiveCacheManagerBuilder inmemoryReactiveManagerBuilder = ReactiveCacheManagerBuilder.newInmemoryReactiveCacheManagerBuilder();
        Assertions.assertNotNull(inmemoryReactiveManagerBuilder);
    }

    @Test
    void newCaffeineReactiveCacheManagerBuilder() {
        CaffeineReactiveCacheManagerBuilder caffeineReactiveManagerBuilder = ReactiveCacheManagerBuilder.newCaffeineReactiveCacheManagerBuilder();
        Assertions.assertNotNull(caffeineReactiveManagerBuilder);
    }

    @Test
    void newRedisReactiveCacheManagerBuilder() {
        RedisReactiveCacheManagerBuilder redisReactiveManagerBuilder = ReactiveCacheManagerBuilder.newRedisReactiveCacheManagerBuilder(
                reactiveRedisTemplate
        );
        Assertions.assertNotNull(redisReactiveManagerBuilder);
    }

    @Test
    void newCustomReactiveCacheManagerBuilder() {
        CustomReactiveCacheManagerBuilder customReactiveManagerBuilder = ReactiveCacheManagerBuilder.newCustomReactiveCacheManagerBuilder();
        Assertions.assertNotNull(customReactiveManagerBuilder);
    }

    @Test
    void testNewInmemoryReactiveCacheManagerBuilder() {
        ReactiveCacheManager reactiveCacheManager = ReactiveCacheManagerBuilder.newInmemoryReactiveCacheManagerBuilder()
                .withMaxWaitingDuration(Duration.ofSeconds(5))
                .build();
        Assertions.assertNotNull(reactiveCacheManager);
    }

    @Test
    void testNewRedisReactiveCacheManagerBuilder() {
        ReactiveCacheManager reactiveCacheManager = ReactiveCacheManagerBuilder.newRedisReactiveCacheManagerBuilder(
                        reactiveRedisTemplate)
                .withMaxWaitingDuration(Duration.ofSeconds(5))
                .build();
        Assertions.assertNotNull(reactiveCacheManager);
    }

    @Test
    void testNewCustomReactiveCacheManagerBuilder() {
        ReactiveCacheManager reactiveCacheManager = ReactiveCacheManagerBuilder.newCustomReactiveCacheManagerBuilder()
                .withMaxWaitingDuration(Duration.ofSeconds(5))
                .withReactiveCacheLock(new InmemoryReactiveCacheLock())
                .withReactiveCacheMonoAdapter(new InmemoryReactiveCacheMonoAdapter())
                .withReactiveCacheFluxAdapter(new InmemoryReactiveCacheFluxAdapter())
                .build();
        Assertions.assertNotNull(reactiveCacheManager);
    }

}