package com.meradeya.app.advice;

import com.meradeya.app.dto.common.ExceptionDetail;
import com.meradeya.app.exception.AccountSuspendedException;
import com.meradeya.app.exception.CategoryNotFoundException;
import com.meradeya.app.exception.EmailAlreadyExistsException;
import com.meradeya.app.exception.InvalidCredentialsException;
import com.meradeya.app.exception.ListingNotFoundException;
import com.meradeya.app.exception.OwnerAccessDeniedException;
import com.meradeya.app.exception.PhotoNotFoundException;
import com.meradeya.app.exception.PhotoStorageException;
import com.meradeya.app.exception.PhotoUploadException;
import com.meradeya.app.exception.UserNotFoundException;
import com.meradeya.app.exception.face.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(PhotoUploadException.class)
  public ResponseEntity<ExceptionDetail> handlePhotoUpload(PhotoUploadException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ExceptionDetail.forStatusAndException(HttpStatus.BAD_REQUEST, ex));
  }

  @ExceptionHandler(PhotoStorageException.class)
  public ResponseEntity<ExceptionDetail> handlePhotoStorage(PhotoStorageException ex) {
    log.error("Photo storage failure: {}", ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ExceptionDetail.forStatusAndException(HttpStatus.INTERNAL_SERVER_ERROR, ex));
  }

  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<Void> handleInvalidCredentials() {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
  }

  @ExceptionHandler(AccountSuspendedException.class)
  public ResponseEntity<ExceptionDetail> handleAccountSuspended(AccountSuspendedException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(ExceptionDetail.forStatusAndException(HttpStatus.FORBIDDEN, ex));
  }

  @ExceptionHandler(OwnerAccessDeniedException.class)
  public ResponseEntity<ExceptionDetail> handleOwnerAccessDenied(OwnerAccessDeniedException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(ExceptionDetail.forStatusAndException(HttpStatus.FORBIDDEN, ex));
  }

  @ExceptionHandler({ListingNotFoundException.class, CategoryNotFoundException.class,
      PhotoNotFoundException.class, UserNotFoundException.class})
  public ResponseEntity<ExceptionDetail> handleNotFound(AppException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ExceptionDetail.forStatusAndException(HttpStatus.NOT_FOUND, ex));
  }

  @ExceptionHandler(EmailAlreadyExistsException.class)
  public ResponseEntity<ExceptionDetail> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ExceptionDetail.forStatusAndException(HttpStatus.CONFLICT, ex));
  }

  @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
  public ResponseEntity<ExceptionDetail> handleOptimisticLock(
      ObjectOptimisticLockingFailureException ignored) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ExceptionDetail.forStatusTitleAndDetail(
            HttpStatus.CONFLICT,
            "Conflict",
            "The resource was modified by another request. Re-fetch and retry."));
  }

}
