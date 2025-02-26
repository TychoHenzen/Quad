package com.quadexercise.quad.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@SuppressWarnings("DuplicateStringLiteralInspection")
class ExceptionTest {

    @Test
    void testQuestionNotFoundException() {
        // Arrange
        String questionId = "test-id-123";

        // Act
        QuestionNotFoundException exception = new QuestionNotFoundException(questionId);

        // Assert
        assertEquals(questionId, exception.getQuestionId());
        assertEquals("Question ID not found: test-id-123", exception.getMessage());
    }

    @Test
    void testTriviaParseException_WithMessage() {
        // Arrange
        String errorMessage = "Error parsing trivia data";

        // Act
        TriviaParseException exception = new TriviaParseException(errorMessage);

        // Assert
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void testTriviaParseException_WithMessageAndCause() {
        // Arrange
        String errorMessage = "Error parsing trivia data";
        Throwable cause = new RuntimeException("Original error");

        // Act
        TriviaParseException exception = new TriviaParseException(errorMessage, cause);

        // Assert
        assertEquals(errorMessage, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testTriviaServiceException_WithMessage() {
        // Arrange
        String errorMessage = "Service error occurred";

        // Act
        TriviaServiceException exception = new TriviaServiceException(errorMessage);

        // Assert
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void testTriviaServiceException_WithMessageAndCause() {
        // Arrange
        String errorMessage = "Service error occurred";
        Throwable cause = new RuntimeException("Original error");

        // Act
        TriviaServiceException exception = new TriviaServiceException(errorMessage, cause);

        // Assert
        assertEquals(errorMessage, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testExceptionHierarchy() {
        // Arrange
        QuestionNotFoundException qnfe = new QuestionNotFoundException("test-id");
        TriviaParseException tpe = new TriviaParseException("parse error");

        // Assert - verify inheritance
        assertInstanceOf(TriviaServiceException.class, qnfe);
        assertInstanceOf(TriviaServiceException.class, tpe);
        assertInstanceOf(RuntimeException.class, qnfe);
    }
}