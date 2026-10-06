package org.softspace.customer.controller.error.handler;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.softspace.customer.dto.error.ErrorResponse;
import org.softspace.customer.exception.CustomerNotFoundException;
import org.softspace.customer.exception.ValidationException;
import org.softspace.customer.exception.error.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponse> customerNotFoundExceptionHandler(CustomerNotFoundException ex, HttpServletRequest request) {
        String message = "Customer not found.";
        Instant timestamp = Instant.now();
        log.warn(message, ex);
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

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> methodWebExchangeBindExceptionHandler(HttpMessageNotReadableException ex, HttpServletRequest request) {
        Instant timestamp = Instant.now();
        Throwable cause = ex.getMostSpecificCause();
        switch (cause) {
            case InvalidFormatException exception -> {
                String message = exception.getMessage() != null? exception.getMessage() : "Invalid format of field.";
                log.warn(message, ex);

                ErrorResponse errorResponse = new ErrorResponse(
                        ErrorCode.FIELD_INVALID.name(),
                        message,
                        Map.of("fieldName", exception.getPath().getFirst().getFieldName(),
                                "value", exception.getValue()),
                        request.getRequestURI(),
                        timestamp,
                        null
                );
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            case UnrecognizedPropertyException exception -> {
                String message = exception.getMessage() != null? exception.getMessage() : "Unrecognized property of field.";
                log.warn(message, ex);

                ErrorResponse errorResponse = new ErrorResponse(
                        ErrorCode.FIELD_NOT_ALLOWED.name(),
                        message,
                        Map.of("fieldName", exception.getPropertyName()),
                        request.getRequestURI(),
                        timestamp,
                        null
                );
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }


            default -> {
                String message = ex.getMessage() != null ? ex.getMessage() : "Invalid JSON.";
                log.warn(message, ex);
                ErrorResponse errorResponse = new ErrorResponse(
                        ErrorCode.VALIDATION_ERROR.name(),
                        message,
                        Map.of(),
                        request.getRequestURI(),
                        timestamp,
                        null
                );
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
        }
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = "Validation error.";
        Instant timestamp = Instant.now();
        Map<String, Object> details = new HashMap<>();
        log.warn(message, ex);
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
        String message = "Validation error.";
        Instant timestamp = Instant.now();
        log.warn(message, ex);
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
        log.error(message, ex);
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
