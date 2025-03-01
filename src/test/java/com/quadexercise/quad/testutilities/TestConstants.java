package com.quadexercise.quad.testutilities;

import java.util.Arrays;
import java.util.List;

/**
 * Test constants to promote reuse across test classes.
 * Prevents duplication of string literals and other constants in tests.
 */
@SuppressWarnings("DuplicateStringLiteralInspection")
public enum TestConstants {
    ;

    // Error messages for testing
    public static final String ERROR_SERVICE_UNAVAILABLE = "{\"error\": \"Service temporarily unavailable\"}";
    public static final String ERROR_FETCH_TRIVIA = "{\"error\": \"Failed to fetch trivia\"}";
    public static final String ERROR_PARSING_DATA = "{\"error\": \"Error parsing trivia data from provider\"}";
    public static final String PARSE_ERROR_MESSAGE = "Error parsing trivia data";
    public static final String SERVICE_ERROR_MESSAGE = "Service error occurred";
    public static final String ORIGINAL_ERROR = "Original error";

    // View controller constants
    public static final String HOME_PATH = "/";
    public static final String PLAY_PATH = "/play";
    public static final String RESULTS_PATH = "/results";
    public static final String HOME_TEMPLATE = "homeTemplate";
    public static final String TRIVIA_TEMPLATE = "triviaTemplate";
    public static final String RESULTS_TEMPLATE = "resultsTemplate";
    public static final String QUESTIONS_ATTR = "questions";
    public static final int DEFAULT_QUESTION_AMOUNT = 5;


    // Common test data values
    public static final String TEST_QUESTION_ID = "test-id-123";
    public static final String TEST_CATEGORY = "Science";
    public static final String TEST_TYPE = "multiple";
    public static final String TEST_DIFFICULTY = "medium";
    public static final String TEST_QUESTION = "What is H2O?";
    public static final String TEST_CORRECT_ANSWER = "Water";
    // Test message service
    public static final String TEST_KEY = "test.key";
    public static final String TEST_KEY_WITH_ARGS = "test.key.with.args";
    public static final String TEST_MESSAGE = "Test Message";
    public static final String TEST_MESSAGE_WITH_ARGS = "Test Message with arg1 and arg2";
    // Test JSON responses
    public static final String EMPTY_JSON_RESPONSE = "{}";
    public static final String EMPTY_RESPONSE = "{\"response_code\":0,\"results\":[]}";
    public static final String VALID_QUESTION_JSON = "{\"response_code\":0,\"results\":[" +
            "{\"category\":\"Science\",\"type\":\"multiple\",\"difficulty\":\"medium\"," +
            "\"question\":\"What is H2O?\",\"correct_answer\":\"Water\"," +
            "\"incorrect_answers\":[\"Carbon Dioxide\",\"Oxygen\",\"Hydrogen\"]}" +
            "]}";

    // Constants for time-related testing
    public static final long SMALL_DELAY_MS = 100L;
    public static final long LARGE_DELAY_MS = 1000L;
    public static final long RATE_LIMIT_MS = 5000L;
    // Content types for testing
    public static final String TEXT_PLAIN_UTF8 = "text/plain;charset=UTF-8";
    public static final String RESPONSE_CODE_KEY = "response_code";
    public static final String EXPECTED_API_URL = "https://opentdb.com/api.php?amount=1";

    // Integration test constants
    public static final String HOME_TEMPLATE_NAME = "homeTemplate";
    public static final String HOME_EXPECTED_CONTENT = "Test your knowledge";

    public static List<String> TEST_INCORRECT_ANSWERS() {
        return Arrays.asList("Carbon Dioxide", "Oxygen", "Hydrogen");
    }

    static List<String> TEST_ALL_ANSWERS() {
        return Arrays.asList(TEST_CORRECT_ANSWER, "Carbon Dioxide", "Oxygen", "Hydrogen");
    }
}
