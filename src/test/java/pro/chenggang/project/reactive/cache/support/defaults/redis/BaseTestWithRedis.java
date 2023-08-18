package pro.chenggang.project.reactive.cache.support.defaults.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import pro.chenggang.project.reactive.cache.support.BaseTest;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
public class BaseTestWithRedis extends BaseTest {

    protected GenericContainer redisContainer;
    protected ReactiveRedisTemplate reactiveRedisTemplate;

    @BeforeEach
    public void setupRedisClient() {
        redisContainer = new GenericContainer(RedisContainer.DEFAULT_IMAGE_NAME
                .withTag(RedisContainer.DEFAULT_TAG))
                .withExposedPorts(6379);
        redisContainer.start();
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(redisContainer.getHost(),
                redisContainer.getMappedPort(6379)
        );
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
    public void loadRedis() {
        Assertions.assertNotNull(redisContainer);
        Assertions.assertNotNull(reactiveRedisTemplate);
    }

}
