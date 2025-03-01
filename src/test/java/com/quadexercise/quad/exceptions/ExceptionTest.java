package com.quadexercise.quad.exceptions;

import org.junit.jupiter.api.Test;

import static com.quadexercise.quad.testutilities.TestConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ExceptionTest {

    @Test
    void testQuestionNotFoundException_CreatesCorrectException() {
        // Arrange
        String questionId = TEST_QUESTION_ID;

        // Act
        QuestionNotFoundException exception = new QuestionNotFoundException(questionId);

        // Assert
        assertEquals(questionId, exception.getQuestionId());
        assertEquals("Question ID not found: " + TEST_QUESTION_ID, exception.getMessage());
    }

    @Test
    void testTriviaParseException_WithMessage() {
        // Arrange
        String errorMessage = PARSE_ERROR_MESSAGE;

        // Act
        TriviaParseException exception = new TriviaParseException(errorMessage);

        // Assert
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void testTriviaParseException_WithMessageAndCause() {
        // Arrange
        String errorMessage = PARSE_ERROR_MESSAGE;
        Throwable cause = new RuntimeException(ORIGINAL_ERROR);

        // Act
        TriviaParseException exception = new TriviaParseException(errorMessage, cause);

        // Assert
        assertEquals(errorMessage, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testTriviaServiceException_WithMessage() {
        // Arrange
        String errorMessage = SERVICE_ERROR_MESSAGE;

        // Act
        TriviaServiceException exception = new TriviaServiceException(errorMessage);

        // Assert
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void testTriviaServiceException_WithMessageAndCause() {
        // Arrange
        String errorMessage = SERVICE_ERROR_MESSAGE;
        Throwable cause = new RuntimeException(ORIGINAL_ERROR);

        // Act
        TriviaServiceException exception = new TriviaServiceException(errorMessage, cause);

        // Assert
        assertEquals(errorMessage, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testExceptionHierarchy_VerifiesInheritance() {
        // Arrange
        QuestionNotFoundException qnfe = new QuestionNotFoundException("test-id");
        TriviaParseException tpe = new TriviaParseException("parse error");

        // Act - in this case, we're just inspecting the class hierarchy

        // Assert - verify inheritance
        assertInstanceOf(TriviaServiceException.class, qnfe);
        assertInstanceOf(TriviaServiceException.class, tpe);
        assertInstanceOf(RuntimeException.class, qnfe);
    }
}