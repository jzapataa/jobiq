package com.jobiq.auth.exception;

import java.util.List;

import com.jobiq.shared.error.ApiFieldError;

public class AuthValidationException extends RuntimeException {

    private final List<ApiFieldError> fieldErrors;

    public AuthValidationException(List<ApiFieldError> fieldErrors) {
        super("Request validation failed");
        this.fieldErrors = List.copyOf(fieldErrors);
    }

    public List<ApiFieldError> getFieldErrors() {
        return fieldErrors;
    }
}
