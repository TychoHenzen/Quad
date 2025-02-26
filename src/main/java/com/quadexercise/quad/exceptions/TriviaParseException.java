package com.quadexercise.quad.exceptions;

/**
 * Exception thrown when there's an error parsing trivia data.
 */
public class TriviaParseException extends TriviaServiceException {

    public TriviaParseException(String message) {
        super(message);
    }

    public TriviaParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
