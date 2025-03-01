package com.quadexercise.quad.constants;

/**
 * Application-wide constants for API-related operations.
 * Centralizes all constants to improve maintainability.
 */
public enum ApiConstants {
    ;

    // Rate limiting
    public static final long RATE_LIMIT_MS = 5000L;

    // Error messages
    public static final String ERROR_SERVICE_UNAVAILABLE = "Service temporarily unavailable";
    public static final String ERROR_FETCH_TRIVIA = "Failed to fetch trivia";
    public static final String ERROR_PARSING_DATA = "Error parsing trivia data from provider";
    public static final String ERROR_FORMAT = "{\"error\": \"%s\"}\"";

    // API endpoints
    public static final String TRIVIA_API_HOST = "opentdb.com";
    public static final String TRIVIA_API_PATH = "/api.php";

    // API parameters
    public static final String PARAM_AMOUNT = "amount";

    // Content type constants
    public static final String CONTENT_TYPE_UTF8 = ";charset=UTF-8";
}
