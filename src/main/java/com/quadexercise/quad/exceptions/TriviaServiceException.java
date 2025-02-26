package com.quadexercise.quad.exceptions;

/**
 * Base exception class for trivia service related exceptions.
 */
public class TriviaServiceException extends RuntimeException {

    public TriviaServiceException(String message) {
        super(message);
    }

    public TriviaServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
