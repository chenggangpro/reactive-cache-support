# Reactive Cache Support

[![Java CI with Maven](https://github.com/chenggangpro/reactive-cache-support/actions/workflows/maven-ci.yml/badge.svg?branch=develop)](https://github.com/chenggangpro/reactive-cache-support/actions/workflows/maven-ci.yml)
[![Coverage](.github/badges/jacoco.svg)](https://github.com/chenggangpro/reactive-cache-support/actions/workflows/maven-ci.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/pro.chenggang/reactive-cache-support/badge.svg)](https://maven-badges.herokuapp.com/maven-central/pro.chenggang/reactive-cache-support)

#### The aim of this project is to implement a general approach to reactive cache operations.

#### Introduction

* This project is compatible with `reactivestream` aka [`project-reactor`](https://projectreactor.io/)
* This project could integrate with `Spring Framework` (version>= 2.x)
* This project implement reactive cache for business application scenarios. The default implementation includes the following:
  * `InmemeoryReactiveCache` uses `java.util.concurrent.DelayQueue` to implement cache behavior
  * `CaffeineReactiveCache` uses `com.github.benmanes.caffeine.cache.Cache` to implement cache behavior
  * `RedisReactiveCache` uses `redis` and `spring-boot-data-redis` to implement cache behavior
  * `DefaultReactiveCache` uses customized configuration which specific configured by uses to implement cache behavior

#### Usage

* Maven Central

```xml
<dependency>
  <groupId>pro.chenggang</groupId>
  <artifactId>reactive-cache-support</artifactId>
  <version>${latest.version}</version>
</dependency>
```

* Standalone usages

> InmemoryReactiveCache

```java
ReactiveCacheManager reactiveCacheManager = ReactiveCacheManagerBuilder.newInmemoryReactiveCacheManagerBuilder()
                .withMaxWaitingDuration(Duration.ofSeconds(5))
                .build();
```

> CaffeineReactiveCache

```java
ReactiveCacheManager reactiveCacheManager = ReactiveCacheManagerBuilder.newCaffeineReactiveCacheManagerBuilder()
                .withMaxWaitingDuration(Duration.ofSeconds(5))
                .build();
```


> RedisReactiveCache

```java
ReactiveCacheManager reactiveCacheManager = ReactiveCacheManagerBuilder.newRedisReactiveCacheManagerBuilder(
                        reactiveRedisTemplate)
                .withMaxWaitingDuration(Duration.ofSeconds(5))
                .build();
```

> Customized ReactiveCache

```java
ReactiveCacheManager reactiveCacheManager = ReactiveCacheManagerBuilder.newCustomReactiveCacheManagerBuilder()
                .withMaxWaitingDuration(Duration.ofSeconds(5))
                // Customized ReactiveCacheLock instance implemented interface ReactiveCacheLock
                .withReactiveCacheLock()
                // Customized ReactiveCacheMonoAdapter instance implemented interface ReactiveCacheMonoAdapter
                .withReactiveCacheMonoAdapter()
                // Customized ReactiveCacheFluxAdapter instance implemented interface ReactiveCacheFluxAdapter
                .withReactiveCacheFluxAdapter()
                .build();
```

* SpringBoot usages

  * Configuration properties as: 
  
  ```yaml
  reactive:
    cache:
      enabled: true
      type: inmemory
      maxWaitingDuration: PT5S
  ```
  
  * Using with spring auto-injection
  
  ```java
  @Autowired
  ReactiveCacheManager reactiveCacheManager;
  ```
  
  * Caching operation
  
  ```java
    // Initialize ReactiveCacheManager instance manually with ReactiveCacheManagerBuilder
    // Or auto-injected in spring boot context 
    ReactiveCacheManager reactiveCacheManager;
  
    @Test
    void get() {
        reactiveCache.monoCache()
                .get(cacheKey)
                .as(StepVerifier::create)
                .expectError(NoSuchCachedReactiveDataException.class)
                .verify();
    }

    @Test
    void cacheIfNecessary() {
        reactiveCache.monoCache()
                .cacheIfNecessary(cacheKey, Duration.ofSeconds(3), Mono.just(true))
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();
        reactiveCache.monoCache()
                .get(cacheKey)
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void getMany() {
        reactiveCache.fluxCache()
                .get(cacheKey)
                .as(StepVerifier::create)
                .expectError(NoSuchCachedReactiveDataException.class)
                .verify();
    }

    @Test
    void cacheManyIfNecessary() {
        reactiveCache.fluxCache()
                .cacheIfNecessary(cacheKey, Duration.ofSeconds(3), Flux.range(0,3))
                .as(StepVerifier::create)
                .expectNext(0)
                .expectNext(1)
                .expectNext(2)
                .verifyComplete();
        reactiveCache.fluxCache()
                .get(cacheKey)
                .as(StepVerifier::create)
                .expectNext(0)
                .expectNext(1)
                .expectNext(2)
                .verifyComplete();
    }

    @Test
    void evictCache() {
        reactiveCache.monoCache()
                .evictCache(cacheKey)
                .as(StepVerifier::create)
                .verifyComplete();
        reactiveCache.fluxCache()
                .evictCache(cacheKey)
                .as(StepVerifier::create)
                .verifyComplete();
        reactiveCache.monoCache()
                .cacheIfNecessary(cacheKey, Duration.ofSeconds(3), Mono.just(true))
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();
        reactiveCache.monoCache()
                .evictCache(cacheKey)
                .as(StepVerifier::create)
                .verifyComplete();
        reactiveCache.fluxCache()
                .cacheIfNecessary(cacheKey, Duration.ofSeconds(3), Flux.range(0,3))
                .as(StepVerifier::create)
                .expectNext(0)
                .expectNext(1)
                .expectNext(2)
                .verifyComplete();
        reactiveCache.fluxCache()
                .evictCache(cacheKey)
                .as(StepVerifier::create)
                .verifyComplete();
    }
  ```
  
* For more details, please refer to the source code and the test cases.

#### Note:

* Test cases run with `testcontainers`, so please ensure that you have access to `Docker` in your local environment.







