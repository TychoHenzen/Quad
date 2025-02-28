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

import java.util.Arrays;
import java.util.List;

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

    @BeforeEach
    void setUp() {
        when(_restTemplateBuilder.build()).thenReturn(_restTemplate);
        _triviaService = new TriviaService(_restTemplateBuilder, _messageService);
    }

    @Test
    void testCheckAnswers_ShouldReturnCorrectResults() {
        // Arrange - First get some questions to populate the answer map
        String jsonResponse = "{\"response_code\":0,\"results\":[" +
                "{\"category\":\"Science\",\"type\":\"multiple\",\"difficulty\":\"medium\"," +
                "\"question\":\"What is H2O?\",\"correct_answer\":\"Water\"," +
                "\"incorrect_answers\":[\"Carbon Dioxide\",\"Oxygen\",\"Hydrogen\"]}" +
                "]}";

        when(_restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(jsonResponse);

        List<QuestionDTO> questions = _triviaService.getQuestions(1);
        String questionId = questions.get(0).getId();

        AnswerDTO correctAnswer = new AnswerDTO();
        correctAnswer.setQuestionId(questionId);
        correctAnswer.setSelectedAnswer("Water");

        AnswerDTO incorrectAnswer = new AnswerDTO();
        incorrectAnswer.setQuestionId(questionId);
        incorrectAnswer.setSelectedAnswer("Oxygen");

        // Act
        List<AnswerResultDTO> correctResults = _triviaService.checkAnswers(List.of(correctAnswer));
        List<AnswerResultDTO> incorrectResults = _triviaService.checkAnswers(List.of(incorrectAnswer));

        // Assert
        assertEquals(1, correctResults.size());
        assertTrue(correctResults.get(0).isCorrect());
        assertEquals("Water", correctResults.get(0).getCorrectAnswer());

        assertEquals(1, incorrectResults.size());
        assertFalse(incorrectResults.get(0).isCorrect());
        assertEquals("Water", incorrectResults.get(0).getCorrectAnswer());
    }

    @Test
    void testCheckAnswers_ShouldThrowQuestionNotFoundException() {
        // Arrange
        AnswerDTO answerDTO = new AnswerDTO();
        answerDTO.setQuestionId("non-existent-id");
        answerDTO.setSelectedAnswer("Some Answer");
        List<AnswerDTO> answers = List.of(answerDTO);

        // Act & Assert
        assertThrows(QuestionNotFoundException.class, () -> _triviaService.checkAnswers(answers));
    }

    @Test
    void testCheckAnswers_EmptyCollection() {
        // Act
        List<AnswerResultDTO> results = _triviaService.checkAnswers(List.of());

        // Assert
        assertTrue(results.isEmpty());
    }

    @Test
    void testCheckAnswers_MultipleAnswers() {
        // Arrange - First get some questions to populate the answer map
        String jsonResponse = "{\"response_code\":0,\"results\":[" +
                "{\"category\":\"Science\",\"type\":\"multiple\",\"difficulty\":\"medium\"," +
                "\"question\":\"What is H2O?\",\"correct_answer\":\"Water\"," +
                "\"incorrect_answers\":[\"Carbon Dioxide\",\"Oxygen\",\"Hydrogen\"]}," +
                "{\"category\":\"History\",\"type\":\"multiple\",\"difficulty\":\"medium\"," +
                "\"question\":\"Who was the first US President?\",\"correct_answer\":\"Washington\"," +
                "\"incorrect_answers\":[\"Adams\",\"Jefferson\",\"Madison\"]}" +
                "]}";

        when(_restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(jsonResponse);

        List<QuestionDTO> questions = _triviaService.getQuestions(2);

        AnswerDTO answer1 = new AnswerDTO();
        answer1.setQuestionId(questions.get(0).getId());
        answer1.setSelectedAnswer("Water");

        AnswerDTO answer2 = new AnswerDTO();
        answer2.setQuestionId(questions.get(1).getId());
        answer2.setSelectedAnswer("Adams"); // Incorrect

        // Act
        List<AnswerResultDTO> results = _triviaService.checkAnswers(Arrays.asList(answer1, answer2));

        // Assert
        assertEquals(2, results.size());
        assertTrue(results.get(0).isCorrect());
        assertFalse(results.get(1).isCorrect());
        assertEquals("Washington", results.get(1).getCorrectAnswer());
    }

    @Test
    void testCheckAnswer_SetsAllFields() {
        // Arrange
        // Prepare some questions first
        String jsonResponse = "{\"response_code\":0,\"results\":[" +
                "{\"category\":\"Science\",\"type\":\"multiple\",\"difficulty\":\"medium\"," +
                "\"question\":\"What is H2O?\",\"correct_answer\":\"Water\"," +
                "\"incorrect_answers\":[\"Carbon Dioxide\",\"Oxygen\",\"Hydrogen\"]}" +
                "]}";

        when(_restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(jsonResponse);

        // Get the questions to populate the answer map
        List<QuestionDTO> questions = _triviaService.getQuestions(1);
        String questionId = questions.get(0).getId();

        AnswerDTO answerDTO = new AnswerDTO();
        answerDTO.setQuestionId(questionId);
        answerDTO.setSelectedAnswer("Water");

        // Act
        AnswerResultDTO result = _triviaService.checkAnswer(answerDTO);

        // Assert
        assertEquals(questionId, result.getQuestionId());
        assertTrue(result.isCorrect());
        assertEquals("Water", result.getCorrectAnswer());
    }
}