package com.jobiq.shared.error;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
        String code,
        String message,
        Instant timestamp,
        String path,
        String requestId,
        List<ApiFieldError> fieldErrors) {

    public ApiErrorResponse {
        fieldErrors = List.copyOf(fieldErrors);
    }
}
