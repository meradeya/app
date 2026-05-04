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
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Global REST exception handler that maps domain exceptions to HTTP responses.
 *
 * <p>Produces {@link ExceptionDetail} bodies for known error cases and uses
 * sensible HTTP status codes for authentication, authorization, and validation errors.
 */
@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
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

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      org.springframework.http.@NonNull HttpHeaders headers,
      @NonNull HttpStatusCode status,
      @NonNull WebRequest request
  ) {
    String validationErrors = ex.getBindingResult().getFieldErrors()
        .stream()
        .map(objectError -> objectError.getField() + " " + objectError.getDefaultMessage())
        .collect(Collectors.joining("; "));

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ExceptionDetail.forStatusTitleAndDetail(HttpStatus.BAD_REQUEST,
            "Validation failed", validationErrors));
  }

}
