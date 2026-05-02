package com.meradeya.app.service.impl;

import com.meradeya.app.exception.PhotoStorageException;
import com.meradeya.app.exception.PhotoUploadException;
import com.meradeya.app.prop.PhotoStorageProperties;
import com.meradeya.app.service.face.PhotoStorageService;
import com.meradeya.app.util.AllowedPhotoMimeType;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoStorageServiceImpl implements PhotoStorageService {

  private final PhotoStorageProperties props;
  private final Tika tika;

  @Override
  public String store(UUID listingId, MultipartFile file) {
    String detectedMime = validateFile(file);
    String ext = extensionFor(detectedMime);
    String filename = UUID.randomUUID() + "." + ext;
    Path listingDir = props.listingUploadDir(listingId);
    try {
      Files.createDirectories(listingDir);
      Path target = listingDir.resolve(filename);
      try (InputStream in = file.getInputStream()) {
        Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
      }
    } catch (IOException e) {
      log.error("Failed to store photo for listing {}", listingId, e);
      throw new PhotoStorageException(e);
    }
    String url = props.buildMediaUrl(listingId, filename);
    log.info("Stored photo for listing {}: {}", listingId, url);
    return url;
  }

  @Override
  public void delete(String url) {
    if (url == null || !url.startsWith(props.getMediaUrlRoot())) {
      log.warn("Skipping delete for unexpected URL: {}", url);
      return;
    }
    Path target = props.mediaUrlToFilePath(url);
    try {
      Files.deleteIfExists(target);
      log.info("Deleted photo file: {}", target);
    } catch (IOException e) {
      log.warn("Could not delete file {}: {}", target, e.getMessage());
    }
  }

  /**
   * Resolves the file extension for a validated MIME type using Tika's built-in registry.
   * Throws if no extension is found — this should never happen for types that passed validation.
   */
  private String extensionFor(String mimeType) {
    try {
      String ext = MimeTypes.getDefaultMimeTypes().forName(mimeType).getExtension();
      if (ext == null || ext.isBlank()) {
        throw new PhotoStorageException("No file extension found for MIME type: " + mimeType, null);
      }
      return ext.startsWith(".") ? ext.substring(1) : ext;
    } catch (MimeTypeException e) {
      throw new PhotoStorageException("No file extension found for MIME type: " + mimeType, e);
    }
  }

  /**
   * Validates the uploaded file and returns the Tika-detected MIME type.
   *
   * <p>Checks performed in order:
   * <ol>
   *   <li>File must be non-null and non-empty.</li>
   *   <li>File size must not exceed the configured maximum.</li>
   *   <li>Actual content type detected from magic bytes must be in {@link AllowedPhotoMimeType}.</li>
   * </ol>
   */
  private String validateFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new PhotoUploadException("Uploaded file is empty.");
    }
    if (file.getSize() > props.getMaxFileSizeBytes()) {
      throw new PhotoUploadException("File size exceeds the maximum allowed size of "
          + props.getMaxFileSizeBytes() / 1_048_576 + " MB.");
    }
    String detectedMime;
    try {
      detectedMime = tika.detect(file.getInputStream());
    } catch (IOException e) {
      throw new PhotoStorageException("Failed to read uploaded file for MIME detection.", e);
    }
    if (!AllowedPhotoMimeType.isAllowed(detectedMime)) {
      throw new PhotoUploadException("Detected file type '" + detectedMime + "' is not allowed. "
          + "Allowed types: " + AllowedPhotoMimeType.allowedMimeTypes());
    }
    return detectedMime;
  }
}
