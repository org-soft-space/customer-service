package org.softspace.customer.exception;

import org.softspace.customer.exception.error.ErrorCode;

import java.util.Map;

public class CustomerNotArchivedException extends BusinessException {
    public CustomerNotArchivedException(String message, Map<String, Object> details) {
        super(message, details);
    }

    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.CUSTOMER_NOT_ARCHIVED;
    }
}
