package com.quadexercise.quad.dto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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
        // Arrange & Act
        AnswerDTO answerDTO = new AnswerDTO();

        // Assert
        assertNull(answerDTO.getQuestionId());
        assertNull(answerDTO.getSelectedAnswer());
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
        // Arrange & Act
        AnswerResultDTO resultDTO = new AnswerResultDTO();

        // Assert
        assertNull(resultDTO.getQuestionId());
        assertFalse(resultDTO.isCorrect());
        assertNull(resultDTO.getCorrectAnswer());
    }

    @SuppressWarnings("DuplicateStringLiteralInspection")
    @Test
    void testQuestionDTO_GettersAndSetters() {
        // Arrange
        QuestionDTO questionDTO = new QuestionDTO();
        String id = "60aee2aa-80fa-4aa3-a916-8adad0f190b4";
        String category = "Science";
        String type = "multiple";
        String difficulty = "medium";
        String question = "What is H2O?";
        List<String> answers = Arrays.asList("Water", "Carbon Dioxide", "Oxygen", "Hydrogen");

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
        // Arrange & Act
        QuestionDTO questionDTO = new QuestionDTO();
        String id = questionDTO.getId();

        // Assert
        assertNotNull(id);
        assertFalse(id.isEmpty());

        // Try to parse as UUID to verify format
        assertDoesNotThrow(() -> UUID.fromString(id));
    }

    @Test
    void testQuestionDTO_AnswersImmutability() {
        // Arrange
        QuestionDTO questionDTO = new QuestionDTO();
        List<String> originalAnswers = Arrays.asList("A", "B", "C");
        questionDTO.setAnswers(originalAnswers);

        // Act & Assert
        List<String> returnedAnswers = questionDTO.getAnswers();

        // Should be unmodifiable
        assertThrows(UnsupportedOperationException.class, () ->
                returnedAnswers.add("D"));

        // Original list shouldn't be affected by attempts to modify returned list
        assertEquals(3, originalAnswers.size());
    }

    @Test
    void testQuestionDTO_DefensiveCopy() {
        // Arrange
        QuestionDTO questionDTO = new QuestionDTO();
        List<String> originalAnswers = Arrays.asList("A", "B", "C");
        questionDTO.setAnswers(originalAnswers);

        // Act - modify original list
        List<String> modifiableList = Arrays.asList("A", "B", "C");
        questionDTO.setAnswers(modifiableList);

        // Try to modify the original list
        modifiableList.set(0, "Modified");

        // Assert - the DTO's list should not be affected
        List<String> returnedAnswers = questionDTO.getAnswers();
        assertEquals("A", returnedAnswers.get(0));
        assertEquals(3, returnedAnswers.size());
    }
}