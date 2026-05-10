package pro.chenggang.project.reactive.cache.support.defaults.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.Scheduler;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import pro.chenggang.project.reactive.cache.support.core.adapter.ReactiveCacheFluxAdapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The inmemory reactive cache flux adapter by using caffeine
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class CaffeineReactiveCacheFluxAdapter implements ReactiveCacheFluxAdapter {

    private final ConcurrentHashMap<String, Cache<String, ConcurrentLinkedDeque<Object>>> fluxDataCache = new ConcurrentHashMap<>();

    @Override
    public Mono<Boolean> hasData(@NonNull String cacheKey) {
        return Mono.defer(() -> Mono.fromCallable(() ->
                fluxDataCache.containsKey(cacheKey)
                        &&
                        fluxDataCache.get(cacheKey)
                                .asMap()
                                .containsKey(cacheKey))
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Flux<T> loadData(@NonNull String cacheKey) {
        return Mono.fromCallable(() -> fluxDataCache.get(cacheKey))
                .flatMapMany(cache -> Mono.fromCallable(() -> cache.getIfPresent(cacheKey))
                        .flatMapMany(cachedData -> (Flux<T>) Flux.fromIterable(cachedData))
                );
    }

    @Override
    public <T> Flux<T> cacheData(@NonNull String cacheKey, @NonNull Duration cacheDuration, @NonNull Flux<T> sourcePublisher) {
        final AtomicBoolean initFlag = new AtomicBoolean(false);
        return sourcePublisher.publish(sharedFlux -> sharedFlux.concatMap(item -> {
            if (initFlag.compareAndSet(false, true)) {
                return Mono.fromRunnable(() -> fluxDataCache.compute(
                                cacheKey,
                                (key, value) -> {
                                    ConcurrentLinkedDeque<Object> data = new ConcurrentLinkedDeque<>();
                                    data.add(item);
                                    if (Objects.nonNull(value)) {
                                        value.invalidateAll();
                                    }
                                    Cache<String, ConcurrentLinkedDeque<Object>> cache = Caffeine.newBuilder()
                                            .expireAfterWrite(cacheDuration)
                                            .scheduler(Scheduler.systemScheduler())
                                            .removalListener(this::removalListener)
                                            .build();
                                    cache.put(cacheKey, data);
                                    return cache;
                                }
                        ))
                        .thenReturn(item);
            }
            return Mono.fromCallable(() -> fluxDataCache.get(cacheKey))
                    .flatMap(cache -> Mono.fromRunnable(() -> {
                        ConcurrentLinkedDeque<Object> deque = cache.getIfPresent(cacheKey);
                        if (Objects.nonNull(deque)) {
                            deque.add(item);
                        }
                    }))
                    .thenReturn(item);
        }));
    }

    private void removalListener(@Nullable String key, @Nullable Object value, RemovalCause cause) {
        log.debug("Cache removed for key : {} cause : {}", key, cause);
    }

    @Override
    public Mono<Void> cleanupData(@NonNull String cacheKey) {
        return Mono.fromRunnable(() -> {
            Cache<String, ConcurrentLinkedDeque<Object>> cache = fluxDataCache.remove(cacheKey);
            if (Objects.nonNull(cache)) {
                cache.invalidateAll();
                cache.cleanUp();
            }
            log.debug("Cleanup cached data success, CacheKey: {}", cacheKey);
        });
    }

}
