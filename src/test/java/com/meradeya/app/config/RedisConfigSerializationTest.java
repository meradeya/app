package com.meradeya.app.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.meradeya.app.dto.category.CategoryTreeDto;
import com.meradeya.app.dto.listing.ListingDetail;
import com.meradeya.app.dto.listing.ListingPhotoDto;
import com.meradeya.app.prop.CacheProperties;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import tools.jackson.databind.ObjectMapper;

class RedisConfigSerializationTest {

  @Test
  void listingDetailSerializerPreservesType() {
    CacheProperties cacheProperties = new CacheProperties();
    RedisConfig redisConfig = new RedisConfig(cacheProperties);

    RedisCacheConfiguration baseConfig = redisConfig.redisCacheConfiguration();
    ObjectMapper objectMapper = redisConfig.cacheObjectMapper();
    RedisCacheConfiguration listingConfig = redisConfig.buildCacheConfigurations(objectMapper, baseConfig)
        .get(CacheNames.LISTINGS);
    assertNotNull(listingConfig, "Listings cache config should be present");

    ListingDetail detail = new ListingDetail(
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        "Title",
        "Description",
        new BigDecimal("123.45"),
        "MDL",
        "NEW",
        "ACTIVE",
        "Chisinau",
        Map.of("color", "black"),
        List.of(new ListingPhotoDto(UUID.randomUUID(), "/media/1.jpg", (short) 0)),
        OffsetDateTime.now(),
        OffsetDateTime.now(),
        1L
    );

    SerializationPair<Object> pair = listingConfig.getValueSerializationPair();
    ByteBuffer buffer = pair.write(detail);
    Object decoded = pair.read(buffer);

    ListingDetail decodedDetail = assertInstanceOf(ListingDetail.class, decoded);
    assertEquals(detail.id(), decodedDetail.id());
    assertEquals(detail.sellerId(), decodedDetail.sellerId());
    assertEquals(detail.categoryId(), decodedDetail.categoryId());
    assertEquals(detail.title(), decodedDetail.title());
    assertEquals(detail.currency(), decodedDetail.currency());
    assertEquals(detail.photos().size(), decodedDetail.photos().size());
  }

  @Test
  void categoryTreeSerializerPreservesType() {
    CacheProperties cacheProperties = new CacheProperties();
    RedisConfig redisConfig = new RedisConfig(cacheProperties);

    RedisCacheConfiguration baseConfig = redisConfig.redisCacheConfiguration();
    ObjectMapper objectMapper = redisConfig.cacheObjectMapper();
    RedisCacheConfiguration categoryConfig = redisConfig.buildCacheConfigurations(objectMapper, baseConfig)
        .get(CacheNames.CATEGORY_TREE);
    assertNotNull(categoryConfig, "Category tree cache config should be present");

    List<CategoryTreeDto> tree = List.of(
        new CategoryTreeDto(UUID.randomUUID(), "Electronics", "electronics", List.of())
    );

    SerializationPair<Object> pair = categoryConfig.getValueSerializationPair();
    ByteBuffer buffer = pair.write(tree);
    Object decoded = pair.read(buffer);

    @SuppressWarnings("unchecked")
    List<CategoryTreeDto> decodedTree = assertInstanceOf(List.class, decoded);
    assertEquals(tree, decodedTree);
    assertInstanceOf(CategoryTreeDto.class, decodedTree.getFirst());
  }
}
