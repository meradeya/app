package com.meradeya.app.exception;

import com.meradeya.app.exception.face.AppException;

public class PhotoLimitExceededException extends AppException {

  public static final String TITLE = "Photo Limit Exceeded";

  public PhotoLimitExceededException(int maxPhotos) {
    super("A listing may have at most " + maxPhotos + " photos.");
  }

  @Override
  public String getTitle() {
    return TITLE;
  }
}
