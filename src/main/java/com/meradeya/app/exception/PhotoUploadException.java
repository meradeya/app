package com.meradeya.app.exception;

import com.meradeya.app.exception.face.AppException;

/** Thrown when an uploaded photo fails client-side validation (empty, wrong type, too large). */
public class PhotoUploadException extends AppException {

  public static final String TITLE = "Invalid Photo";
  public static final String MESSAGE = "The uploaded file is not a valid photo.";

  public PhotoUploadException(String message) {
    super(message);
  }

  @Override
  public String getTitle() {
    return TITLE;
  }
}
