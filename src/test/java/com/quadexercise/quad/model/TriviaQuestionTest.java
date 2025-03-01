package com.quadexercise.quad.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.quadexercise.quad.testutilities.TestConstants.*;
import static org.junit.jupiter.api.Assertions.*;

class TriviaQuestionTest {


    @Test
    void testTriviaQuestion_GettersAndSetters() {
        // Arrange
        TriviaQuestion question = new TriviaQuestion();
        String category = TEST_CATEGORY;
        String type = TEST_TYPE;
        String difficulty = TEST_DIFFICULTY;
        String questionText = TEST_QUESTION;
        String correctAnswer = TEST_CORRECT_ANSWER;
        List<String> incorrectAnswers = TEST_INCORRECT_ANSWERS();

        // Act
        question.setCategory(category);
        question.setType(type);
        question.setDifficulty(difficulty);
        question.setQuestion(questionText);
        question.setCorrectAnswer(correctAnswer);
        question.setIncorrectAnswers(incorrectAnswers);

        // Assert
        assertEquals(category, question.getCategory(), "Category getter should return the set value");
        assertEquals(type, question.getType(), "Type getter should return the set value");
        assertEquals(difficulty, question.getDifficulty(), "Difficulty getter should return the set value");
        assertEquals(questionText, question.getQuestion(), "Question getter should return the set value");
        assertEquals(correctAnswer, question.getCorrectAnswer(), "CorrectAnswer getter should return the set value");
        assertEquals(incorrectAnswers, question.getIncorrectAnswers(),
                "IncorrectAnswers getter should return the set value");
    }

    @Test
    void testTriviaQuestion_DefaultValues() {
        // Arrange
        TriviaQuestion question = new TriviaQuestion();

        // Assert
        assertEquals("", question.getCategory(), "Default category should be empty string");
        assertEquals("", question.getType(), "Default type should be empty string");
        assertEquals("", question.getDifficulty(), "Default difficulty should be empty string");
        assertEquals("", question.getQuestion(), "Default question should be empty string");
        assertEquals("", question.getCorrectAnswer(), "Default correctAnswer should be empty string");
        assertTrue(question.getIncorrectAnswers().isEmpty(), "Default incorrectAnswers should be empty list");
        assertEquals(0, question.getIncorrectAnswers().size(), "Default incorrectAnswers should have zero elements");
    }

    @Test
    void testTriviaQuestion_IncorrectAnswersImmutability() {
        // Arrange
        TriviaQuestion question = new TriviaQuestion();
        List<String> incorrectAnswers = TEST_INCORRECT_ANSWERS();
        question.setIncorrectAnswers(incorrectAnswers);

        // Act
        List<String> returnedList = question.getIncorrectAnswers();

        // Assert
        assertThrows(UnsupportedOperationException.class, () ->
                        returnedList.add(TEST_CORRECT_ANSWER),
                "Returned list should be immutable and throw exception when modified"
        );
    }
}