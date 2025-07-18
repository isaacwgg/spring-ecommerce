package com.commerce.common.exception;

public class ValidationException extends RuntimeException {  // 400 or 403
    public ValidationException(String msg) {
        super(msg);
    }
}
