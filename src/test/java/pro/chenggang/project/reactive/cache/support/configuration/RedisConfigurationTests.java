package pro.chenggang.project.reactive.cache.support.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pro.chenggang.project.reactive.cache.support.configuration.properties.ReactiveCacheSupportProperties;
import pro.chenggang.project.reactive.cache.support.core.ReactiveCacheManager;

import java.time.Duration;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
@Testcontainers
@SpringBootTest
@ActiveProfiles("redis")
@TestPropertySource(locations = "classpath:application-redis.yml")
@ContextConfiguration(classes = {RedisConfigurationTests.RedisTestConfiguration.class, ReactiveCacheAutoConfiguration.class, RedisReactiveAutoConfiguration.class})
public class RedisConfigurationTests {

    @Configuration
    @EnableConfigurationProperties(RedisProperties.class)
    public static class RedisTestConfiguration {

        @Bean
        public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(RedisProperties redisProperties) {
            RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(redisProperties.getHost(),
                    redisProperties.getPort()
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
            return new ReactiveRedisTemplate<>(lettuceConnectionFactory, serializationContext);
        }
    }

    @Container
    private static final RedisContainer REDIS_CONTAINER =
            new RedisContainer(RedisContainer.DEFAULT_IMAGE_NAME
                    .withTag(RedisContainer.DEFAULT_TAG))
                    .withExposedPorts(6379);

    @DynamicPropertySource
    private static void registerRedisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.redis.port",
                () -> REDIS_CONTAINER.getMappedPort(6379)
                        .toString()
        );
    }

    @Autowired
    ReactiveCacheSupportProperties reactiveCacheSupportProperties;

    @Autowired
    ReactiveCacheManager reactiveCacheManager;

    @Test
    public void testProperties() {
        Assertions.assertEquals(reactiveCacheSupportProperties.getType(),
                ReactiveCacheSupportProperties.ReactiveCacheType.redis
        );
        Assertions.assertEquals(reactiveCacheSupportProperties.getMaxWaitingDuration(),
                Duration.ofSeconds(3)
        );
    }

    @Test
    public void testRedisReactiveCacheManagerConfiguration() {
        Assertions.assertNotNull(reactiveCacheManager);
    }

}
