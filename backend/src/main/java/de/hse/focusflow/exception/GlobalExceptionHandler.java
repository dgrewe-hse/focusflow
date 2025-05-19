package de.hse.focusflow.exception;

import de.hse.focusflow.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(
      ResourceNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleValidationException(
      MethodArgumentNotValidException ex) {
    FieldError fieldError = ex.getBindingResult().getFieldError();
    String errorMessage;

    if (fieldError != null) {
      errorMessage = fieldError.getDefaultMessage();
    } else {
      errorMessage = "Validation error occurred";
    }

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error(errorMessage));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
    // Log the full exception for server-side debugging
    logger.error("Unhandled exception occurred:", ex);

    // Include simplified exception message in the response
    String errorMessage = "An unexpected error occurred: " + ex.getClass().getSimpleName();
    if (ex.getMessage() != null) {
      errorMessage += " - " + ex.getMessage();
    }

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResponse.error(errorMessage));
  }
}
