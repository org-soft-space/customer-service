package org.softspace.customer.controller.error.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.softspace.customer.dto.error.ErrorResponse;
import org.softspace.customer.exception.CustomerNotFoundException;
import org.softspace.customer.exception.error.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponse> customerNotFoundExceptionHandler(CustomerNotFoundException ex, HttpServletRequest request) {
        String message = "Customer not found.";
        Instant timestamp = Instant.now();
        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.CUSTOMER_NOT_FOUND.name(),
                message,
                ex.getDetails(),
                request.getRequestURI(),
                timestamp,
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
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
