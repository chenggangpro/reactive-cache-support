package pro.chenggang.project.reactive.cache.support.defaults.inmemory;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pro.chenggang.project.reactive.cache.support.core.adapter.ReactiveCacheFluxAdapter;
import pro.chenggang.project.reactive.cache.support.toolkit.AutoExpiredDataCache;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
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
public class InmemoryReactiveCacheFluxAdapter implements ReactiveCacheFluxAdapter {

    private final AutoExpiredDataCache<ConcurrentLinkedDeque<Object>> fluxDataCache = AutoExpiredDataCache.newInstance();

    @Override
    public Mono<Boolean> hasData(@NonNull String cacheKey) {
        return Mono.defer(() -> Mono.fromFuture(CompletableFuture.supplyAsync(() -> fluxDataCache.hasData(cacheKey))));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Flux<T> loadData(@NonNull String cacheKey) {
        return Mono.defer(() -> Mono.fromFuture(CompletableFuture.supplyAsync(() -> fluxDataCache.getData(cacheKey))))
                .flatMap(Mono::justOrEmpty)
                .flatMapMany(cachedData -> (Flux<T>) Flux.fromIterable(cachedData));
    }

    @Override
    public <T> Flux<T> cacheData(@NonNull String cacheKey,
                                 @NonNull Duration cacheDuration,
                                 @NonNull Flux<T> sourcePublisher) {
        final AtomicBoolean initFlag = new AtomicBoolean(false);
        return Flux.zip(sourcePublisher,
                        sourcePublisher.share()
                                .concatMap(value -> {
                                    if (initFlag.compareAndSet(false, true)) {
                                        return Mono.fromFuture(CompletableFuture.supplyAsync(() -> {
                                            ConcurrentLinkedDeque<Object> data = new ConcurrentLinkedDeque<>();
                                            data.add(value);
                                            fluxDataCache.putData(cacheKey, data, cacheDuration);
                                            return true;
                                        }));
                                    }
                                    return Mono.fromFuture(CompletableFuture.supplyAsync(() -> {
                                        Optional<ConcurrentLinkedDeque<Object>> optionalDeque = fluxDataCache.getData(cacheKey);
                                        optionalDeque.ifPresent(deque -> deque.add(value));
                                        return true;
                                    }));
                                })
                )
                .map(Tuple2::getT1);
    }

    @Override
    public Mono<Void> cleanupData(@NonNull String cacheKey) {
        return Mono.fromFuture(CompletableFuture.runAsync(() -> fluxDataCache.removeData(cacheKey)));
    }

}
