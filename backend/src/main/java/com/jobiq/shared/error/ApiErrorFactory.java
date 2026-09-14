package com.jobiq.shared.error;

import java.time.Instant;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

public final class ApiErrorFactory {

    private ApiErrorFactory() {
    }

    public static ApiErrorResponse create(
            HttpServletRequest request,
            String code,
            String message,
            List<ApiFieldError> fieldErrors) {
        Object requestId = request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);
        return new ApiErrorResponse(
                code,
                message,
                Instant.now(),
                request.getRequestURI(),
                requestId == null ? "" : requestId.toString(),
                fieldErrors);
    }
}
