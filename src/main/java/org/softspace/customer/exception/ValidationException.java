package org.softspace.customer.exception;

import org.softspace.customer.exception.error.ErrorCode;

import java.util.Map;

public class ValidationException extends BusinessException{
    public ValidationException(String message, Map<String, Object> details) {
        super(message, details);
    }

    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.VALIDATION_ERROR;
    }
}
