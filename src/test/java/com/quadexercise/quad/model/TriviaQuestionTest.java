package com.quadexercise.quad.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TriviaQuestionTest {

    private static final String CORRECT_ANSWER = "Paris";
    private static final String[] INCORRECT_ANSWERS = {"London", "Berlin", "Madrid"};

    @Test
    void testTriviaQuestionGettersAndSetters() {
        // Arrange
        TriviaQuestion question = new TriviaQuestion();
        String category = "Science";
        String type = "multiple";
        String difficulty = "medium";
        String questionText = "What is the capital of France?";
        String correctAnswer = CORRECT_ANSWER;
        List<String> incorrectAnswers = Arrays.asList(INCORRECT_ANSWERS);

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

    @Test
    void testTriviaQuestionDefaultValues() {
        // Arrange
        TriviaQuestion question = new TriviaQuestion();

        // Assert
        assertEquals("", question.getCategory());
        assertEquals("", question.getType());
        assertEquals("", question.getDifficulty());
        assertEquals("", question.getQuestion());
        assertEquals("", question.getCorrectAnswer());
        String msg = "New TriviaQuestion should have empty incorrect answers list";
        assertTrue(question.getIncorrectAnswers().isEmpty(), msg);
        msg = "New TriviaQuestion should have exactly 0 incorrect answers";
        assertEquals(0, question.getIncorrectAnswers().size(), msg);
    }

    @Test
    void testTriviaQuestionIncorrectValuesImmutable() {
        // Arrange
        TriviaQuestion question = new TriviaQuestion();
        List<String> incorrectAnswers = Arrays.asList(INCORRECT_ANSWERS);
        question.setIncorrectAnswers(incorrectAnswers);

        // Act & Assert
        List<String> returnedList = question.getIncorrectAnswers();
        assertThrows(UnsupportedOperationException.class, () ->
                returnedList.add(CORRECT_ANSWER)
        );
    }
}