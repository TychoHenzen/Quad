package com.quadexercise.quad.utils;

import com.quadexercise.quad.constants.ApiConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Utility class for creating standardized HTTP responses.
 * Consolidates response creation logic to improve consistency and reduce duplication.
 */
public enum ResponseUtils {
    ;

    /**
     * Create a response indicating that the service is temporarily unavailable.
     *
     * @return ResponseEntity with 503 status and appropriate error message
     */
    public static ResponseEntity<Object> createUnavailableResponse() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(String.format(ApiConstants.ERROR_FORMAT,
                        ApiConstants.ERROR_SERVICE_UNAVAILABLE));
    }

    /**
     * Create a response for internal server errors.
     *
     * @return ResponseEntity with 500 status and appropriate error message
     */
    public static ResponseEntity<Object> createErrorResponse() {
        return ResponseEntity.internalServerError()
                .body(String.format(ApiConstants.ERROR_FORMAT,
                        ApiConstants.ERROR_FETCH_TRIVIA));
    }

    /**
     * Create a response for trivia service errors.
     *
     * @return ResponseEntity with 500 status and appropriate error message
     */
    public static ResponseEntity<Object> createServiceErrorResponse(String message) {
        return ResponseEntity.internalServerError()
                .body(String.format(ApiConstants.ERROR_FORMAT, message));
    }

    /**
     * Create a response for parsing/gateway errors.
     *
     * @return ResponseEntity with 502 status and appropriate error message
     */
    public static ResponseEntity<Object> createBadGatewayResponse() {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(String.format(ApiConstants.ERROR_FORMAT,
                        ApiConstants.ERROR_PARSING_DATA));
    }

    /**
     * Create a response for client request errors with a custom message.
     *
     * @param message Custom error message to include in the response
     * @return ResponseEntity with 400 status and the provided error message
     */
    public static ResponseEntity<Object> createBadRequestResponse(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(String.format(ApiConstants.ERROR_FORMAT, message));
    }
}
