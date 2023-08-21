package pro.chenggang.project.reactive.cache.support.configuration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import pro.chenggang.project.reactive.cache.support.configuration.properties.ReactiveCacheSupportProperties;
import pro.chenggang.project.reactive.cache.support.core.ReactiveCacheManager;

import java.time.Duration;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
@SpringBootTest
@ActiveProfiles("caffeine")
@TestPropertySource(locations = "classpath:application-caffeine.yml")
@ContextConfiguration(classes = ReactiveCacheAutoConfiguration.class)
public class CaffeineConfigurationTests {

    @Autowired
    ReactiveCacheSupportProperties reactiveCacheSupportProperties;

    @Autowired
    ReactiveCacheManager reactiveCacheManager;

    @Autowired
    Environment environment;

    @Test
    public void testProperties() {
        Assertions.assertEquals(reactiveCacheSupportProperties.getType(),
                ReactiveCacheSupportProperties.ReactiveCacheType.caffeine
        );
        Assertions.assertEquals(reactiveCacheSupportProperties.getMaxWaitingDuration(),
                Duration.ofSeconds(5)
        );
    }

    @Test
    public void testCaffeineReactiveCacheManagerConfiguration() {
        Assertions.assertNotNull(reactiveCacheManager);
    }
}
