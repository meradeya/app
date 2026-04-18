package com.meradeya.app.exception;

import com.meradeya.app.exception.face.AppException;

public class OwnerAccessDeniedException extends AppException {

  public static final String TITLE = "Owner Access Denied";
  public static final String MESSAGE = "You can only access your own user resources";

  public OwnerAccessDeniedException() {
    super(MESSAGE);
  }

  @Override
  public String getTitle() {
    return TITLE;
  }
}

