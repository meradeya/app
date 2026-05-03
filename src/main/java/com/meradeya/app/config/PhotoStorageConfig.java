package com.meradeya.app.config;

import org.apache.tika.Tika;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures photo storage settings.
 */
@Configuration
public class PhotoStorageConfig {

  /**
   * Provides a shared {@link Tika} instance for content-based MIME type detection.
   *
   * <p>{@link Tika} is thread-safe and expensive to construct, so it is a singleton bean.
   *
   * @return configured {@link Tika} detector
   */
  @Bean
  public Tika tika() {
    return new Tika();
  }
}
