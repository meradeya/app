package com.meradeya.app.config;

import com.meradeya.app.dto.category.CategoryTreeDto;
import com.meradeya.app.dto.listing.ListingDetail;
import com.meradeya.app.prop.CacheProperties;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Redis-backed cache configuration.
 *
 * <p>Defines cache serialization, TTLs, key prefixing, and fail-open error handling
 * for the application's cache abstraction.
 */
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "app.cache.enabled", havingValue = "true", matchIfMissing = true)
public class RedisConfig {

  private static final Logger log = LoggerFactory.getLogger(RedisConfig.class);

  private final CacheProperties cacheProperties;

  public RedisConfig(CacheProperties cacheProperties) {
    this.cacheProperties = cacheProperties;
  }

  /**
   * Base cache configuration shared by all Redis caches.
   *
   * <p>Uses JSON value serialization, String keys, configured TTL, and null-value
   * suppression to avoid caching 404-style misses.
   */
  @Bean
  public RedisCacheConfiguration redisCacheConfiguration() {
    ObjectMapper objectMapper = cacheObjectMapper();

    JacksonJsonRedisSerializer<Object> valueSerializer =
        new JacksonJsonRedisSerializer<>(objectMapper, Object.class);

    return RedisCacheConfiguration.defaultCacheConfig()
        .computePrefixWith(cacheName -> normalizedPrefix() + cacheName + "::")
        .serializeKeysWith(SerializationPair.fromSerializer(new StringRedisSerializer()))
        .serializeValuesWith(SerializationPair.fromSerializer(valueSerializer))
        .entryTtl(cacheProperties.getListings().getTtl())
        .disableCachingNullValues();
  }

  /**
   * Builds the ObjectMapper used for Redis cache serialization.
   */
  ObjectMapper cacheObjectMapper() {
    return JsonMapper.builder()
        .findAndAddModules()
        .build();
  }

  /**
   * Normalize the configured key prefix to a stable {@code "prefix::"} format.
   */
  private String normalizedPrefix() {
    String prefix = cacheProperties.getKeyPrefix();
    if (prefix == null || prefix.isBlank()) {
      return "";
    }
    return prefix.endsWith("::") ? prefix : prefix + "::";
  }

  /**
   * Build per-cache configurations with typed serializers.
   *
   * @param objectMapper mapper used for JSON serialization
   * @param baseConfig   base cache configuration to extend
   * @return map of cache name to cache configuration
   */
  Map<String, RedisCacheConfiguration> buildCacheConfigurations(
      ObjectMapper objectMapper,
      RedisCacheConfiguration baseConfig) {
    JacksonJsonRedisSerializer<ListingDetail> listingDetailSerializer =
        new JacksonJsonRedisSerializer<>(objectMapper, ListingDetail.class);
    JavaType categoryTreeType = objectMapper.getTypeFactory()
        .constructCollectionType(List.class, CategoryTreeDto.class);
    JacksonJsonRedisSerializer<List<CategoryTreeDto>> categoryTreeSerializer =
        new JacksonJsonRedisSerializer<>(objectMapper, categoryTreeType);

    Map<String, RedisCacheConfiguration> initialCacheConfigurations = new HashMap<>();
    initialCacheConfigurations.put(CacheNames.CATEGORY_TREE,
        baseConfig.entryTtl(cacheProperties.getCategories().getTtl())
            .serializeValuesWith(SerializationPair.fromSerializer(categoryTreeSerializer)));
    initialCacheConfigurations.put(CacheNames.LISTINGS,
        baseConfig.entryTtl(cacheProperties.getListings().getTtl())
            .serializeValuesWith(SerializationPair.fromSerializer(listingDetailSerializer)));

    return initialCacheConfigurations;
  }

  /**
   * Cache manager with per-cache TTL overrides.
   */
  @Bean
  public RedisCacheManager cacheManager(
      RedisConnectionFactory connectionFactory,
      @Autowired RedisCacheConfiguration redisCacheConfiguration) {
    ObjectMapper objectMapper = cacheObjectMapper();
    Map<String, RedisCacheConfiguration> initialCacheConfigurations =
        buildCacheConfigurations(objectMapper, redisCacheConfiguration);

    return RedisCacheManager.builder(connectionFactory)
        .cacheDefaults(redisCacheConfiguration)
        .transactionAware()
        .withInitialCacheConfigurations(initialCacheConfigurations)
        .build();
  }

  /**
   * Fail-open cache error handler that logs cache failures and continues request processing without
   * failing the request.
   */
  @Bean
  public CacheErrorHandler cacheErrorHandler() {
    return new CacheErrorHandler() {
      @Override
      public void handleCacheGetError(@NonNull RuntimeException exception, @NonNull Cache cache,
          @NonNull Object key) {
        log.warn("Cache get failed for cache={}, key={}", cache.getName(), key, exception);
      }

      @Override
      public void handleCachePutError(@NonNull RuntimeException exception, @NonNull Cache cache,
          @NonNull Object key, Object value) {
        log.warn("Cache put failed for cache={}, key={}", cache.getName(), key, exception);
      }

      @Override
      public void handleCacheEvictError(@NonNull RuntimeException exception, @NonNull Cache cache,
          @NonNull Object key) {
        log.warn("Cache evict failed for cache={}, key={}", cache.getName(), key, exception);
      }

      @Override
      public void handleCacheClearError(@NonNull RuntimeException exception, @NonNull Cache cache) {
        log.warn("Cache clear failed for cache={}", cache.getName(), exception);
      }
    };
  }
}
