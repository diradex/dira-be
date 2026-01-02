package com.driveflow.backend.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleException(Exception e) {
        log.error("Error occurred: ", e);
        // Return a generic error message in production, but log the full exception
        String errorMessage = e.getMessage() != null ? e.getMessage() : "An unexpected error occurred";
        return ResponseEntity.internalServerError().body(ApiResponse.error(errorMessage));
    }
}
