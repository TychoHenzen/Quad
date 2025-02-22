package com.quadexercise.quad.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TriviaQuestionTest {

    @Test
    void testTriviaQuestionGettersAndSetters() {
        // Arrange
        TriviaQuestion question = new TriviaQuestion();
        String category = "Science";
        String type = "multiple";
        String difficulty = "medium";
        String questionText = "What is the capital of France?";
        String correctAnswer = "Paris";
        List<String> incorrectAnswers = Arrays.asList("London", "Berlin", "Madrid");

        // Act
        question.setCategory(category);
        question.setType(type);
        question.setDifficulty(difficulty);
        question.setQuestion(questionText);
        question.setCorrectAnswer(correctAnswer);
        question.setIncorrectAnswers(incorrectAnswers);

        // Assert
        assertEquals(category, question.getCategory());
        assertEquals(type, question.getType());
        assertEquals(difficulty, question.getDifficulty());
        assertEquals(questionText, question.getQuestion());
        assertEquals(correctAnswer, question.getCorrectAnswer());
        assertEquals(incorrectAnswers, question.getIncorrectAnswers());
    }
}