package pro.chenggang.project.reactive.cache.support.defaults.redis;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveListOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import pro.chenggang.project.reactive.cache.support.core.adapter.ReactiveCacheFluxAdapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.concurrent.Queues;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The inmemory reactive cache flux adapter
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class RedisReactiveCacheFluxAdapter implements ReactiveCacheFluxAdapter {

    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    @Override
    public Mono<Boolean> hasData(@NonNull String cacheKey) {
        return reactiveRedisTemplate.hasKey(cacheKey);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Flux<T> loadData(@NonNull String cacheKey) {
        return reactiveRedisTemplate.opsForList()
                .size(cacheKey)
                .filter(size -> size > 0)
                .map(Long::intValue)
                .flatMapMany(size -> Flux.range(0, size)
                        .buffer(Queues.SMALL_BUFFER_SIZE)
                        .concatMap(bufferedIndex -> (Flux<T>) reactiveRedisTemplate.opsForList()
                                .range(cacheKey,
                                        bufferedIndex.get(0),
                                        bufferedIndex.get(bufferedIndex.size() - 1)
                                )
                        )
                );
    }

    @Override
    public <T> Flux<T> cacheData(@NonNull String cacheKey,
                                 @NonNull Duration cacheDuration,
                                 @NonNull Flux<T> sourcePublisher) {
        final AtomicBoolean initFlag = new AtomicBoolean(false);
        final ReactiveListOperations<String, Object> reactiveListOperations = reactiveRedisTemplate.opsForList();
        return Flux.zip(sourcePublisher,
                        sourcePublisher.share()
                                .concatMap(value -> reactiveListOperations.rightPush(cacheKey,
                                        value
                                ))
                                .concatMap(data -> Mono.just(initFlag)
                                        .map(atomicBoolean -> atomicBoolean.compareAndSet(false,
                                                true
                                        ))
                                        .filter(Boolean::booleanValue)
                                        .flatMap(firstTouch -> reactiveRedisTemplate.expire(
                                                cacheKey,
                                                cacheDuration
                                        ))
                                        .then(Mono.just(data))
                                )
                )
                .map(Tuple2::getT1);
    }

    @Override
    public Mono<Void> cleanupData(@NonNull String cacheKey) {
        return reactiveRedisTemplate.delete(cacheKey)
                .then(Mono.defer(() -> {
                    log.debug("[Redis reactive cache flux adapter]Cleanup cached data success, CacheKey: {}",cacheKey);
                    return Mono.empty();
                }));
    }

}
