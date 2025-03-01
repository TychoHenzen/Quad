package com.quadexercise.quad.utils;

/**
 * Application-wide constants for API-related operations.
 * Centralizes all constants to improve maintainability.
 */
@SuppressWarnings("WeakerAccess")
public enum ApiConstants {
    ;

    // Error messages
    public static final String ERROR_SERVICE_UNAVAILABLE = "Service temporarily unavailable";
    public static final String ERROR_FETCH_TRIVIA = "Failed to fetch trivia";
    public static final String ERROR_PARSING_DATA = "Error parsing trivia data from provider";
    public static final String ERROR_FORMAT = "{\"error\": \"%s\"}\"";
    public static final String RATE_LIMIT_INTERRUPTED = "error.rate.limit.interrupted";

    // API constants
    public static final long RATE_LIMIT_MS = 5000L;
    public static final String TRIVIA_API_HOST = "opentdb.com";
    public static final String TRIVIA_API_PATH = "/api.php";
    public static final String PARAM_AMOUNT = "amount";
    public static final String CONTENT_TYPE_UTF8 = ";charset=UTF-8";
}
