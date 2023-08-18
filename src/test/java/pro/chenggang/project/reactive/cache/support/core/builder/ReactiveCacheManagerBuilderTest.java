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
import pro.chenggang.project.reactive.cache.support.core.builder.ReactiveCacheManagerBuilder.CustomReactiveManagerBuilder;
import pro.chenggang.project.reactive.cache.support.core.builder.ReactiveCacheManagerBuilder.InmemoryReactiveManagerBuilder;
import pro.chenggang.project.reactive.cache.support.core.builder.ReactiveCacheManagerBuilder.RedisReactiveManagerBuilder;
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
    void newInmemoryReactiveManagerBuilder() {
        InmemoryReactiveManagerBuilder inmemoryReactiveManagerBuilder = ReactiveCacheManagerBuilder.newInmemoryReactiveManagerBuilder();
        Assertions.assertNotNull(inmemoryReactiveManagerBuilder);
    }

    @Test
    void newRedisReactiveManagerBuilder() {
        RedisReactiveManagerBuilder redisReactiveManagerBuilder = ReactiveCacheManagerBuilder.newRedisReactiveManagerBuilder(
                reactiveRedisTemplate
        );
        Assertions.assertNotNull(redisReactiveManagerBuilder);
    }

    @Test
    void newCustomReactiveManagerBuilder() {
        CustomReactiveManagerBuilder customReactiveManagerBuilder = ReactiveCacheManagerBuilder.newCustomReactiveManagerBuilder();
        Assertions.assertNotNull(customReactiveManagerBuilder);
    }

    @Test
    void testNewInmemoryReactiveManagerBuilder() {
        ReactiveCacheManager reactiveCacheManager = ReactiveCacheManagerBuilder.newInmemoryReactiveManagerBuilder()
                .withMaxWaitingDuration(Duration.ofSeconds(5))
                .withInmemoryReactiveCacheLock()
                .build();
        Assertions.assertNotNull(reactiveCacheManager);
    }

    @Test
    void testNewRedisReactiveManagerBuilder() {
        ReactiveCacheManager reactiveCacheManager = ReactiveCacheManagerBuilder.newRedisReactiveManagerBuilder(
                        reactiveRedisTemplate)
                .withMaxWaitingDuration(Duration.ofSeconds(5))
                .withRedisReactiveCacheLock()
                .build();
        Assertions.assertNotNull(reactiveCacheManager);
    }

    @Test
    void testNewCustomReactiveManagerBuilder() {
        ReactiveCacheManager reactiveCacheManager = ReactiveCacheManagerBuilder.newCustomReactiveManagerBuilder()
                .withMaxWaitingDuration(Duration.ofSeconds(5))
                .withReactiveCacheLock(new InmemoryReactiveCacheLock())
                .withReactiveCacheMonoAdapter(new InmemoryReactiveCacheMonoAdapter())
                .withReactiveCacheFluxAdapter(new InmemoryReactiveCacheFluxAdapter())
                .build();
        Assertions.assertNotNull(reactiveCacheManager);
    }

}