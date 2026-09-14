package com.jobiq.shared.error;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.jobiq.auth.exception.AuthValidationException;
import com.jobiq.auth.exception.EmailAlreadyExistsException;
import com.jobiq.auth.exception.InvalidCredentialsException;
import com.jobiq.auth.exception.UnauthenticatedException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> validation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        List<ApiFieldError> fieldErrors = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new ApiFieldError(
                        error.getField(),
                        error.getDefaultMessage() == null ? "invalid value" : error.getDefaultMessage()))
                .toList();
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed", fieldErrors, request);
    }

    @ExceptionHandler(AuthValidationException.class)
    ResponseEntity<ApiErrorResponse> authValidation(AuthValidationException exception, HttpServletRequest request) {
        return response(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "Request validation failed",
                exception.getFieldErrors(),
                request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiErrorResponse> malformed(HttpMessageNotReadableException exception, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", "Malformed request", List.of(), request);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    ResponseEntity<ApiErrorResponse> invalidCredentials(InvalidCredentialsException exception, HttpServletRequest request) {
        return response(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid credentials", List.of(), request);
    }

    @ExceptionHandler(UnauthenticatedException.class)
    ResponseEntity<ApiErrorResponse> unauthenticated(UnauthenticatedException exception, HttpServletRequest request) {
        return response(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "Authentication required", List.of(), request);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    ResponseEntity<ApiErrorResponse> duplicateEmail(EmailAlreadyExistsException exception, HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", "Email already exists", List.of(), request);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponse> internalError(Exception exception, HttpServletRequest request) {
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Internal server error", List.of(), request);
    }

    private ResponseEntity<ApiErrorResponse> response(
            HttpStatus status,
            String code,
            String message,
            List<ApiFieldError> fieldErrors,
            HttpServletRequest request) {
        return ResponseEntity.status(status)
                .body(ApiErrorFactory.create(request, code, message, fieldErrors));
    }
}
