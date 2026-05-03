package com.meradeya.app.exception;

import com.meradeya.app.exception.face.AppException;

/** Thrown when a photo cannot be written to or read from persistent storage (I/O failure). */
public class PhotoStorageException extends AppException {

  public static final String TITLE = "Storage Error";
  public static final String MESSAGE = "Failed to store the uploaded file.";

  public PhotoStorageException(Throwable cause) {
    super(MESSAGE, cause);
  }

  public PhotoStorageException(String message, Throwable cause) {
    super(message, cause);
  }

  @Override
  public String getTitle() {
    return TITLE;
  }
}


