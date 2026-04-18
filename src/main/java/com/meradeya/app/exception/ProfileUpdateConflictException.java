package com.meradeya.app.exception;

import com.meradeya.app.exception.face.AppException;

public class ProfileUpdateConflictException extends AppException {

  public static final String TITLE = "Profile Update Conflict";
  public static final String MESSAGE = "Profile was modified by another request. Re-fetch and retry.";

  public ProfileUpdateConflictException() {
    super(MESSAGE);
  }

  @Override
  public String getTitle() {
    return TITLE;
  }
}

