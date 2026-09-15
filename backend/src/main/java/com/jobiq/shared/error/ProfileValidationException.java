package com.jobiq.shared.error;

import java.util.List;

public class ProfileValidationException extends RuntimeException {

    private final List<ApiFieldError> fieldErrors;

    public ProfileValidationException(List<ApiFieldError> fieldErrors) {
        super("Request validation failed");
        this.fieldErrors = List.copyOf(fieldErrors);
    }

    public List<ApiFieldError> getFieldErrors() {
        return fieldErrors;
    }
}
