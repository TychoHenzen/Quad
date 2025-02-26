package com.quadexercise.quad.dto;

public class AnswerDTO {
    private String _questionId;
    private String _selectedAnswer;

    public AnswerDTO() {
        // Default constructor
    }

    // Getters and setters
    public String getQuestionId() {
        return _questionId;
    }

    public void setQuestionId(String questionId) {
        _questionId = questionId;
    }

    public String getSelectedAnswer() {
        return _selectedAnswer;
    }

    public void setSelectedAnswer(String selectedAnswer) {
        _selectedAnswer = selectedAnswer;
    }
}
