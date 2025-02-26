package com.quadexercise.quad.dto;

public class AnswerResultDTO {
    private String _questionId;
    private boolean _correct;
    private String _correctAnswer;

    public AnswerResultDTO() {
        // Default constructor
    }

    // Getters and setters
    public String getQuestionId() {
        return _questionId;
    }

    public void setQuestionId(String questionId) {
        _questionId = questionId;
    }

    public boolean isCorrect() {
        return _correct;
    }

    public void setCorrect(boolean correct) {
        _correct = correct;
    }

    public String getCorrectAnswer() {
        return _correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        _correctAnswer = correctAnswer;
    }
}
