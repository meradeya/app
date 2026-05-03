package com.meradeya.app.runner;

import com.meradeya.app.prop.PhotoStorageProperties;
import java.io.IOException;
import java.nio.file.Files;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PhotoStorageRunner implements CommandLineRunner {

  private final PhotoStorageProperties props;

  @Override
  public void run(String @NonNull ... args) {
    log.info("Running upload directory initializer for {}", props.getUploadDir());
    try {
      Files.createDirectories(props.getUploadDir());
      log.info("Upload directory ready");
    } catch (IOException e) {
      throw new RuntimeException("Cannot create upload directory: " + e.getMessage(), e);
    }
  }
}

