package com.commerce.common.exception;

import com.commerce.common.UniversalResponse.UniversalResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.naming.AuthenticationException;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : ""
                ));

        UniversalResponse response = UniversalResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Validation failed")
                .data(errors)
                .build();

        return ResponseEntity.badRequest().body(response);
    }


    // 401 - Unauthorized
    @ExceptionHandler({InvalidCredentialsException.class, AuthenticationException.class})
    public ResponseEntity<UniversalResponse> handleUnauthorized(Exception ex) {
        UniversalResponse response = UniversalResponse.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .message(ex.getMessage())
                .data(null)
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<UniversalResponse> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Authentication attempt failed: {}", ex.getMessage());
        UniversalResponse response = UniversalResponse.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .message("Invalid username or password") // Generic message for security
                .data(null)
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // 403 - Forbidden (e.g., invalid login data)
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<UniversalResponse> handleForbidden(ValidationException ex) {
        UniversalResponse response = UniversalResponse.builder()
                .status(HttpStatus.FORBIDDEN.value())
                .message(ex.getMessage())
                .data(null)
                .build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    // 404 - Not Found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<UniversalResponse> handleNotFound(ResourceNotFoundException ex) {
        UniversalResponse response = UniversalResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .message(ex.getMessage())
                .data(null)
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // 409 - Conflict (e.g., duplicate resource)
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<UniversalResponse> handleConflict(ConflictException ex) {
        UniversalResponse response = UniversalResponse.builder()
                .status(HttpStatus.CONFLICT.value())
                .message(ex.getMessage())
                .data(null)
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    // 500 - Internal Server Error
    @ExceptionHandler(Exception.class)
    public ResponseEntity<UniversalResponse> handleGenericException(Exception ex) {
        log.error("Unhandled exception", ex);
        UniversalResponse response = UniversalResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Internal server error")
                .data(null)
                .build();
        return ResponseEntity.internalServerError().body(response);
    }
}