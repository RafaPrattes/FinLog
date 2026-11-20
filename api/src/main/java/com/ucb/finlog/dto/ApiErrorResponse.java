package com.ucb.finlog.dto;

public record ApiErrorResponse(
        int status,
        String error,
        String message,
        String path
) {
}
