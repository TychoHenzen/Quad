package com.quadexercise.quad.exceptions;

/**
 * Exception thrown when a question ID can't be found in the system.
 */
public class QuestionNotFoundException extends TriviaServiceException {

    private final String _questionId;

    public QuestionNotFoundException(String questionId) {
        super(String.format("Question ID not found: %s", questionId));
        _questionId = questionId;
    }

    public String getQuestionId() {
        return _questionId;
    }
}
