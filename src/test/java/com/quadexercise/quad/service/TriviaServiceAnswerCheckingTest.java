package com.quadexercise.quad.service;

import com.quadexercise.quad.dto.AnswerDTO;
import com.quadexercise.quad.dto.AnswerResultDTO;
import com.quadexercise.quad.dto.QuestionDTO;
import com.quadexercise.quad.exceptions.QuestionNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.quadexercise.quad.testUtils.TestConstants.TEST_CORRECT_ANSWER;
import static com.quadexercise.quad.testUtils.TestConstants.VALID_QUESTION_JSON;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SuppressWarnings("DuplicateStringLiteralInspection")
@ExtendWith(MockitoExtension.class)
class TriviaServiceAnswerCheckingTest {

    @Mock
    private RestTemplate _restTemplate;

    @Mock
    private RestTemplateBuilder _restTemplateBuilder;

    @Mock
    private MessageService _messageService;

    private TriviaService _triviaService;
    private String _validQuestionId;

    @BeforeEach
    void setUp() {
        // Arrange - Common setup for all tests
        when(_restTemplateBuilder.build()).thenReturn(_restTemplate);
        _triviaService = new TriviaService(_restTemplateBuilder, _messageService);

        // Set up test data for questions

        when(_restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(VALID_QUESTION_JSON);

        // Load questions to populate internal answer map
        List<QuestionDTO> questions = _triviaService.getQuestions(1);
        _validQuestionId = questions.get(0).getId();
    }

    @Test
    void testCheckAnswer_SingleCorrectAnswer() {
        // Arrange
        AnswerDTO correctAnswer = new AnswerDTO();
        correctAnswer.setQuestionId(_validQuestionId);
        correctAnswer.setSelectedAnswer(TEST_CORRECT_ANSWER);

        // Act
        AnswerResultDTO result = _triviaService.checkAnswer(correctAnswer);

        // Assert
        assertTrue(result.isCorrect());
        assertEquals(TEST_CORRECT_ANSWER, result.getCorrectAnswer());
        assertEquals(_validQuestionId, result.getQuestionId());
    }

    @Test
    void testCheckAnswer_SingleIncorrectAnswer() {
        // Arrange
        AnswerDTO incorrectAnswer = new AnswerDTO();
        incorrectAnswer.setQuestionId(_validQuestionId);
        incorrectAnswer.setSelectedAnswer("Oxygen");

        // Act
        AnswerResultDTO result = _triviaService.checkAnswer(incorrectAnswer);

        // Assert
        assertFalse(result.isCorrect());
        assertEquals(TEST_CORRECT_ANSWER, result.getCorrectAnswer());
        assertEquals(_validQuestionId, result.getQuestionId());
    }

    @Test
    void testCheckAnswers_MultipleAnswers() {
        // Arrange
        AnswerDTO correctAnswer = new AnswerDTO();
        correctAnswer.setQuestionId(_validQuestionId);
        correctAnswer.setSelectedAnswer(TEST_CORRECT_ANSWER);

        AnswerDTO incorrectAnswer = new AnswerDTO();
        incorrectAnswer.setQuestionId(_validQuestionId);
        incorrectAnswer.setSelectedAnswer("Oxygen");

        List<AnswerDTO> answers = Arrays.asList(correctAnswer, incorrectAnswer);

        // Act
        List<AnswerResultDTO> results = _triviaService.checkAnswers(answers);

        // Assert
        assertEquals(2, results.size());
        assertTrue(results.get(0).isCorrect());
        assertFalse(results.get(1).isCorrect());
        assertEquals(TEST_CORRECT_ANSWER, results.get(0).getCorrectAnswer());
        assertEquals(TEST_CORRECT_ANSWER, results.get(1).getCorrectAnswer());
    }

    @Test
    void testCheckAnswers_EmptyCollection() {
        // Arrange
        Collection<AnswerDTO> emptyList = new ArrayList<>(0);

        // Act
        List<AnswerResultDTO> results = _triviaService.checkAnswers(emptyList);

        // Assert
        assertTrue(results.isEmpty());
    }

    @Test
    void testCheckAnswers_InvalidQuestionId() {
        // Arrange
        AnswerDTO answerWithInvalidId = new AnswerDTO();
        answerWithInvalidId.setQuestionId("non-existent-id");
        answerWithInvalidId.setSelectedAnswer("Some Answer");
        List<AnswerDTO> answers = List.of(answerWithInvalidId);
        // Act & Assert
        assertThrows(QuestionNotFoundException.class,
                () -> _triviaService.checkAnswers(answers));
    }
}