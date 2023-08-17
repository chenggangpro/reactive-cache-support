package pro.chenggang.project.reactive.cache.support.toolkit;

import lombok.Getter;
import lombok.NonNull;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The auto expired data cache.
 *
 * @param <T> the cached data type
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
public class AutoExpiredDataCache<T> {

    private final AtomicBoolean startFlag = new AtomicBoolean(false);
    private final Map<String, AutoExpiredDataWrapper<T>> cachedDataContainer = new ConcurrentHashMap<>();
    private final DelayQueue<AutoExpiredDataWrapper<T>> delayQueue = new DelayQueue<>();
    private final Thread daemonThread = new Thread(() -> {
        while (startFlag.get()) {
            AutoExpiredDataWrapper<T> data = delayQueue.poll();
            if (Objects.nonNull(data) && cachedDataContainer.containsKey(data.getDataKey())) {
                cachedDataContainer.remove(data.getDataKey(), data);
            }
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                //ignore
            }
        }
    });

    /**
     * New instance of auto expired data cache.
     *
     * @param <T> the cached data type
     * @return the auto expired data cache
     */
    public static <T> AutoExpiredDataCache<T> newInstance() {
        return new AutoExpiredDataCache<>();
    }

    /**
     * Instantiates a new Auto expired data cache.
     */
    protected AutoExpiredDataCache() {
        startup();
    }

    /**
     * Has data or not.
     *
     * @param dataKey the data key
     * @return true if associated data exists
     */
    public boolean hasData(@NonNull String dataKey) {
        return this.cachedDataContainer.containsKey(dataKey);
    }

    /**
     * Put the auto expired data if it is absent, otherwise replace exists data.
     *
     * @param dataKey         the data key
     * @param data            the data
     * @param expiredDuration the expired duration
     */
    public T putData(@NonNull String dataKey, @NonNull T data, @NonNull Duration expiredDuration) {
        if (expiredDuration.isNegative() || expiredDuration.isZero()) {
            throw new IllegalArgumentException(
                    "Expired duration could not be negative or zero, current value is : " + expiredDuration);
        }
        return cachedDataContainer.compute(dataKey, (key, value) -> {
                    if (Objects.isNull(value)) {
                        AutoExpiredDataWrapper<T> autoExpiredDataWrapper = new AutoExpiredDataWrapper<>(dataKey,
                                data,
                                expiredDuration
                        );
                        delayQueue.add(autoExpiredDataWrapper);
                        return autoExpiredDataWrapper;
                    }
                    value.terminate();
                    AutoExpiredDataWrapper<T> autoExpiredDataWrapper = new AutoExpiredDataWrapper<>(dataKey,
                            data,
                            expiredDuration
                    );
                    delayQueue.add(autoExpiredDataWrapper);
                    return autoExpiredDataWrapper;
                })
                .getData();
    }

    /**
     * Gets the auto expired data.
     *
     * @param dataKey the data key
     * @return the optional data
     */
    public Optional<T> getData(@NonNull String dataKey) {
        return Optional.ofNullable(this.cachedDataContainer.get(dataKey))
                .map(AutoExpiredDataWrapper::getData);
    }

    /**
     * Remove the expired data.
     *
     * @param dataKey the data key
     */
    public void removeData(@NonNull String dataKey) {
        if (!this.cachedDataContainer.containsKey(dataKey)) {
            return;
        }
        AutoExpiredDataWrapper<T> autoExpiredDataWrapper = this.cachedDataContainer.get(dataKey);
        autoExpiredDataWrapper.terminate();
        cachedDataContainer.remove(dataKey);
    }

    protected void startup() {
        if (startFlag.compareAndSet(false, true)) {
            daemonThread.setDaemon(true);
            daemonThread.start();
        }
    }

    /**
     * The auto expired data
     *
     * @param <DATA> the actual data type
     * @author Gang Cheng
     * @version 1.0.0
     * @since 1.0.0
     */
    private static class AutoExpiredDataWrapper<DATA> implements Delayed {

        @Getter
        @NonNull
        private final String dataKey;
        @Getter
        @NonNull
        private final DATA data;
        @Getter
        @NonNull
        private final Duration expiredDuration;
        private final long targetExpireTime;
        private final AtomicBoolean terminated = new AtomicBoolean(false);

        private AutoExpiredDataWrapper(@NonNull String dataKey,
                                       @NonNull DATA data,
                                       @NonNull Duration expiredDuration) {
            this.dataKey = dataKey;
            if (expiredDuration.isNegative() || expiredDuration.isZero()) {
                throw new IllegalArgumentException(
                        "Auto expired duration could not be negative or zero, current value is : " + expiredDuration);
            }
            this.data = data;
            this.expiredDuration = expiredDuration;
            this.targetExpireTime = System.currentTimeMillis() + this.expiredDuration.toMillis();
        }

        @Override
        public long getDelay(TimeUnit unit) {
            if (terminated.get()) {
                return 0;
            }
            return unit.convert(this.targetExpireTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }

        private void terminate() {
            this.terminated.getAndSet(true);
        }

        @Override
        public int compareTo(Delayed o) {
            if (!(o instanceof AutoExpiredDataWrapper)) {
                return 1;
            }
            if (o == this) {
                return 0;
            }
            AutoExpiredDataWrapper<?> s = (AutoExpiredDataWrapper<?>) o;
            return Long.compare(this.getDelay(TimeUnit.MILLISECONDS), s.getDelay(TimeUnit.MILLISECONDS));
        }

    }

}
