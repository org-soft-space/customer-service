package org.softspace.customer.controller.error.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.softspace.customer.dto.error.ErrorResponse;
import org.softspace.customer.exception.CustomerNotFoundException;
import org.softspace.customer.exception.ValidationException;
import org.softspace.customer.exception.error.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponse> customerNotFoundExceptionHandler(CustomerNotFoundException ex, HttpServletRequest request) {
        String message = "Customer not found.";
        Instant timestamp = Instant.now();
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getErrorCode().name(),
                message,
                ex.getDetails(),
                request.getRequestURI(),
                timestamp,
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = "Validation error.";
        Instant timestamp = Instant.now();

        Map<String, Object> details = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            details.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.VALIDATION_ERROR.name(),
                message,
                details,
                request.getRequestURI(),
                timestamp,
                null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> validationExceptionHandler(ValidationException ex, HttpServletRequest request) {
        String message = ex.getMessage() == null ? "Validation error." : ex.getMessage();
        Instant timestamp = Instant.now();
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getErrorCode().name(),
                message,
                ex.getDetails(),
                request.getRequestURI(),
                timestamp,
                null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> internalExceptionHandler(RuntimeException ex, HttpServletRequest request) {
        String message = "Unexpected error.";
        Instant timestamp = Instant.now();
        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.INTERNAL_ERROR.name(),
                message,
                Map.of(),
                request.getRequestURI(),
                timestamp,
                null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
