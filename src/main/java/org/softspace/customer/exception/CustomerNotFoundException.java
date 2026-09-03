package org.softspace.customer.exception;

import org.softspace.customer.exception.error.ErrorCode;

import java.util.Map;

public class CustomerNotFoundException extends BusinessException{
    public CustomerNotFoundException(String message, Map<String, Object> details) {
        super(message, details);
    }

    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.CUSTOMER_NOT_FOUND;
    }
}
