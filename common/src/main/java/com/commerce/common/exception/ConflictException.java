package com.commerce.common.exception;

public class ConflictException extends RuntimeException {  // 409
    public ConflictException(String msg) {
        super(msg);
    }
}