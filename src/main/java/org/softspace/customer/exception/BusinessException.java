package org.softspace.customer.exception;

import lombok.Getter;
import org.softspace.customer.exception.error.ErrorCode;

import java.util.Map;

@Getter
public abstract class BusinessException extends RuntimeException{

    private final transient Map<String, Object> details;


    public BusinessException(String message, Map<String, Object> details) {
        super(message);
        this.details = details == null ? Map.of() : details;
    }

    public BusinessException(String message, Throwable cause, Map<String, Object> details) {
        super(message, cause);
        this.details = details == null ? Map.of() : details;
    }

    public abstract ErrorCode getErrorCode();
}
