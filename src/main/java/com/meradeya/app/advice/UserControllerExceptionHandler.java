package com.meradeya.app.advice;

import com.meradeya.app.controller.impl.UsersController;
import com.meradeya.app.dto.common.ExceptionDetail;
import com.meradeya.app.exception.ProfileUpdateConflictException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = UsersController.class)
public class UserControllerExceptionHandler {

  @ExceptionHandler(OptimisticLockingFailureException.class)
  public ResponseEntity<ExceptionDetail> handleOptimisticLockingFailure(
      OptimisticLockingFailureException ex) {
    log.debug("Optimistic locking conflict in UsersController", ex);
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ExceptionDetail.forStatusAndException(HttpStatus.CONFLICT,
            new ProfileUpdateConflictException()));
  }
}
