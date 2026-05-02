package com.meradeya.app.prop;

import java.nio.file.Path;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for photo storage.
 *
 * <p>Allowed MIME types are NOT configurable via properties — they are a security boundary
 * defined in {@link com.meradeya.app.util.AllowedPhotoMimeType}.
 *
 * <p>{@code mediaUrlRoot} and {@code listingsSubdir} define the URL and filesystem structure
 * for serving photos. The Spring MVC resource handler in {@code WebMvcConfig} and the
 * {@code SecurityConfig} permit rule must align with {@code mediaUrlRoot}.
 */
@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "app.photo.storage")
public class PhotoStorageProperties {

  private Path uploadDir;
  private String mediaUrlRoot = "/media";
  private String listingsSubDir = "listings";
  
  private long maxFileSizeBytes = 10_485_760L; // 10 MB
  private int maxPhotosPerListing = 10;

  public String buildMediaUrl(UUID listingId, String filename) {
    return mediaUrlRoot + "/" + listingsSubDir + "/" + listingId + "/" + filename;
  }

  /**
   * Resolves a media URL back to the physical file path on disk.
   * Strips {@code mediaUrlRoot + "/"} and resolves the remainder against {@code uploadDir}.
   */
  public Path mediaUrlToFilePath(String url) {
    String prefix = mediaUrlRoot + "/";
    String relative = url.startsWith(prefix) ? url.substring(prefix.length()) : url;
    return uploadDir.resolve(relative);
  }

  /** Returns the upload directory for photos of a specific listing. */
  public Path listingUploadDir(UUID listingId) {
    return uploadDir.resolve(listingsSubDir).resolve(listingId.toString());
  }
}
