package com.quadexercise.quad.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class QuestionDTO {
    private String _id;
    private String _category;
    private String _type;
    private String _difficulty;
    private String _question;
    // All possible answers, shuffled
    private List<String> _answers;

    public QuestionDTO() {
        _id = UUID.randomUUID().toString();
    }

    // Getters and setters
    public String getId() {
        return _id;
    }

    public void setId(String id) {
        _id = id;
    }

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

    public List<String> getAnswers() {
        return Collections.unmodifiableList(_answers);
    }

    public void setAnswers(List<String> answers) {
        _answers = new ArrayList<>(answers);
    }
}
