package com.commerce.common.exception;

import org.springframework.security.core.AuthenticationException;

public class BadCredentialsException extends AuthenticationException {
    public BadCredentialsException(String msg) {
        super(msg);
    }
}