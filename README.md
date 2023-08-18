# Reactive Cache Support

[![Java CI with Maven](https://github.com/chenggangpro/reactive-cache-support/actions/workflows/maven-ci.yml/badge.svg?branch=develop)](https://github.com/chenggangpro/reactive-cache-support/actions/workflows/maven-ci.yml)
[![Coverage](.github/badges/jacoco.svg)](https://github.com/chenggangpro/reactive-cache-support/actions/workflows/maven-ci.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

#### The aim of this project is to implement a general approach to reactive cache operations.

#### Introduction

* This project is compatible with `reactivestream` aka [`project-reactor`](https://projectreactor.io/)
* This project could integrate with `Spring Framework` (version>= 2.x)
* This project implement reactive cache for business application scenarios. The default implementation includes the following:
  * `InmemeoryReactiveCache` uses `java.util.DelayQueue` to implement cache behavior 
  * `RedisReactiveCache` uses `redis` and `spring-boot-redis` to implement cache behavior
  * `DefaultReactiveCache` uses customized configuration to implement cache behavior

#### Usage

* Standalone usages

> InmemoryReactiveCache

```java
ReactiveCacheManager reactiveCacheManager = ReactiveCacheManagerBuilder.newInmemoryReactiveManagerBuilder()
                .withMaxWaitingDuration(Duration.ofSeconds(5))
                .build();
```

> RedisReactiveCache

```java
ReactiveCacheManager reactiveCacheManager = ReactiveCacheManagerBuilder.newRedisReactiveManagerBuilder(
                        reactiveRedisTemplate)
                .withMaxWaitingDuration(Duration.ofSeconds(5))
                .build();
```

> Customized ReactiveCache

```java
ReactiveCacheManager reactiveCacheManager = ReactiveCacheManagerBuilder.newCustomReactiveManagerBuilder()
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
  
* For more details, please refer to the source code and the test cases.

#### Note:

* Test cases run with `testcontainers`, so please ensure that you have access to `Docker` in your local environment.







