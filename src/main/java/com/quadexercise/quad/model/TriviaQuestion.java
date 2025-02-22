package com.quadexercise.quad.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TriviaQuestion {
    private String _category = "";
    private String _type = "";
    private String _difficulty = "";
    private String _question = "";
    private String _correctAnswer = "";
    private List<String> _incorrectAnswers = new ArrayList<>(0);

    // Getters and Setters
    public String getCategory() {
        return _category;
    }

    public void setCategory(String category) {
        _category = category;
    }

    public String getType() {
        return _type;
    }

    public void setType(String type) {
        _type = type;
    }

    public String getDifficulty() {
        return _difficulty;
    }

    public void setDifficulty(String difficulty) {
        _difficulty = difficulty;
    }

    public String getQuestion() {
        return _question;
    }

    public void setQuestion(String question) {
        _question = question;
    }

    public String getCorrectAnswer() {
        return _correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        _correctAnswer = correctAnswer;
    }

    public List<String> getIncorrectAnswers() {
        return Collections.unmodifiableList(_incorrectAnswers);
    }

    public void setIncorrectAnswers(List<String> incorrectAnswers) {
        // Create a defensive copy of the input list
        _incorrectAnswers = new ArrayList<>(incorrectAnswers);
    }
}
