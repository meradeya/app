package com.meradeya.app.service.face;

import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface PhotoStorageService {


  String store(UUID listingId, MultipartFile file);

  /**
   * Deletes the physical file for the given server-relative URL. Silently ignores missing files.
   */
  void delete(String url);
}
