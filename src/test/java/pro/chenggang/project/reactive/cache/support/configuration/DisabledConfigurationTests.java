package pro.chenggang.project.reactive.cache.support.configuration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import pro.chenggang.project.reactive.cache.support.core.ReactiveCacheManager;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application.yml")
@ContextConfiguration(classes = ReactiveCacheAutoConfiguration.class)
public class DisabledConfigurationTests {

    @Autowired(required = false)
    ReactiveCacheManager reactiveCacheManager;

    @Test
    public void testDisabledReactiveCacheManagerConfiguration() {
        Assertions.assertNull(reactiveCacheManager);
    }
}
