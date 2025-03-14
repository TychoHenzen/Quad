package com.quadexercise.quad.dto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.quadexercise.quad.testutilities.TestConstants.*;
import static org.junit.jupiter.api.Assertions.*;

class DataTransferTest {

    @Test
    void testAnswerDTO_GettersAndSetters() {
        // Arrange
        AnswerDTO answerDTO = new AnswerDTO();
        String questionId = "a30b4296-e015-4319-bfab-5fcfa659ffd2";
        String selectedAnswer = "Test Answer";

        // Act
        answerDTO.setQuestionId(questionId);
        answerDTO.setSelectedAnswer(selectedAnswer);

        // Assert
        assertEquals(questionId, answerDTO.getQuestionId());
        assertEquals(selectedAnswer, answerDTO.getSelectedAnswer());
    }

    @Test
    void testAnswerDTO_DefaultValues() {
        // Arrange
        AnswerDTO answerDTO = new AnswerDTO();

        // Act
        String retrievedId = answerDTO.getQuestionId();
        String retrievedAnswer = answerDTO.getSelectedAnswer();

        // Assert
        assertNull(retrievedId);
        assertNull(retrievedAnswer);
    }

    @Test
    void testAnswerResultDTO_GettersAndSetters() {
        // Arrange
        AnswerResultDTO resultDTO = new AnswerResultDTO();
        String questionId = "c65c5467-3302-4560-8008-c2bbfa2f9a00";
        boolean correct = true;
        String correctAnswer = "Correct Answer";

        // Act
        resultDTO.setQuestionId(questionId);
        resultDTO.setCorrect(correct);
        resultDTO.setCorrectAnswer(correctAnswer);

        // Assert
        assertEquals(questionId, resultDTO.getQuestionId());
        assertTrue(resultDTO.isCorrect());
        assertEquals(correctAnswer, resultDTO.getCorrectAnswer());
    }

    @Test
    void testAnswerResultDTO_DefaultValues() {
        // Arrange
        AnswerResultDTO resultDTO = new AnswerResultDTO();

        // Act
        String retrievedId = resultDTO.getQuestionId();
        boolean isCorrect = resultDTO.isCorrect();
        String correctAnswer = resultDTO.getCorrectAnswer();

        // Assert
        assertNull(retrievedId);
        assertFalse(isCorrect);
        assertNull(correctAnswer);
    }

    @Test
    void testQuestionDTO_GettersAndSetters() {
        // Arrange
        QuestionDTO questionDTO = new QuestionDTO();
        String id = "60aee2aa-80fa-4aa3-a916-8adad0f190b4";
        String category = TEST_CATEGORY;
        String type = TEST_TYPE;
        String difficulty = TEST_DIFFICULTY;
        String question = TEST_QUESTION;
        List<String> answers = TEST_INCORRECT_ANSWERS();

        // Act
        questionDTO.setId(id);
        questionDTO.setCategory(category);
        questionDTO.setType(type);
        questionDTO.setDifficulty(difficulty);
        questionDTO.setQuestion(question);
        questionDTO.setAnswers(answers);

        // Assert
        assertEquals(id, questionDTO.getId());
        assertEquals(category, questionDTO.getCategory());
        assertEquals(type, questionDTO.getType());
        assertEquals(difficulty, questionDTO.getDifficulty());
        assertEquals(question, questionDTO.getQuestion());
        assertEquals(answers, questionDTO.getAnswers());
    }

    @Test
    void testQuestionDTO_AutoGeneratedId() {
        // Arrange
        QuestionDTO questionDTO = new QuestionDTO();

        // Act
        String id = questionDTO.getId();

        // Assert
        assertNotNull(id);
        assertFalse(id.isEmpty());
        assertDoesNotThrow(() -> UUID.fromString(id), "ID should be a valid UUID");
    }

    @Test
    void testQuestionDTO_AnswersImmutability() {
        // Arrange
        QuestionDTO questionDTO = new QuestionDTO();
        List<String> originalAnswers = Arrays.asList("A", "B", "C");
        questionDTO.setAnswers(originalAnswers);

        // Act
        List<String> returnedAnswers = questionDTO.getAnswers();

        // Assert
        assertThrows(UnsupportedOperationException.class, () ->
                returnedAnswers.add("D"), "Returned list should be unmodifiable");
        assertEquals(3, originalAnswers.size(), "Original list should remain unaffected");
    }

    @Test
    void testQuestionDTO_DefensiveCopy() {
        // Arrange
        QuestionDTO questionDTO = new QuestionDTO();
        List<String> modifiableList = Arrays.asList("A", "B", "C");
        questionDTO.setAnswers(modifiableList);

        // Act
        modifiableList.set(0, "Modified");
        List<String> returnedAnswers = questionDTO.getAnswers();

        // Assert
        assertEquals("A", returnedAnswers.get(0), "DTO should contain a defensive copy");
        assertEquals(3, returnedAnswers.size());
    }
}