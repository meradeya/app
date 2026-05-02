package com.meradeya.app.config;

import com.meradeya.app.prop.PhotoStorageProperties;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registers {@code /media/**} as a static resource handler pointing to the local upload directory.
 * Photo URLs stored in the database ({@code /media/listings/{listingId}/{filename}}) are served
 * directly by Spring MVC without hitting any controller.
 *
 * <p>On CDN migration, only this class (and {@link com.meradeya.app.service.impl.PhotoStorageServiceImpl}) needs updating.
 * The {@code mediaUrlRoot} property in {@link PhotoStorageProperties} must stay in sync with
 * the resource handler path registered here and the {@code SecurityConfig} permit rule.
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

  private final PhotoStorageProperties photoStorageProperties;

  @Override
  public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
    String uploadPath = photoStorageProperties.getUploadDir().toUri().toString();

    registry.addResourceHandler(photoStorageProperties.getMediaUrlRoot() + "/**")
        .addResourceLocations(uploadPath);
  }
}

