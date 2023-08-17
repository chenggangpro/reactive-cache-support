package pro.chenggang.project.reactive.cache.support.core;

import pro.chenggang.project.reactive.cache.support.core.executor.ReactiveFluxCache;
import pro.chenggang.project.reactive.cache.support.core.executor.ReactiveMonoCache;

/**
 * The reactive cache.
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
public interface ReactiveCache {

    /**
     * The reactive mono cache.
     *
     * @return the reactive mono cache
     */
    ReactiveMonoCache monoCache();

    /**
     * The reactive flux cache.
     *
     * @return the reactive flux cache
     */
    ReactiveFluxCache fluxCache();
}
