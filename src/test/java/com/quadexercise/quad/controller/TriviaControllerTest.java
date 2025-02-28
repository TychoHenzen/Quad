package com.quadexercise.quad.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quadexercise.quad.dto.AnswerDTO;
import com.quadexercise.quad.dto.AnswerResultDTO;
import com.quadexercise.quad.dto.QuestionDTO;
import com.quadexercise.quad.enums.Errors;
import com.quadexercise.quad.exceptions.QuestionNotFoundException;
import com.quadexercise.quad.exceptions.TriviaParseException;
import com.quadexercise.quad.exceptions.TriviaServiceException;
import com.quadexercise.quad.service.TriviaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Thread.interrupted;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("DuplicateStringLiteralInspection")
@ExtendWith(MockitoExtension.class)
class TriviaControllerTest {

    private static final String ERROR_SERVICE_UNAVAILABLE = "{\"error\": \"Service temporarily unavailable\"}";
    private static final String ERROR_FETCH_TRIVIA = "{\"error\": \"Failed to fetch trivia\"}";
    private static final String ERROR_PARSING_DATA = "{\"error\": \"Error parsing trivia data from provider\"}";

    @Mock
    private TriviaService _triviaService;

    @InjectMocks
    private TriviaController _triviaController;

    private MockMvc _mockMvc;
    private ObjectMapper _objectMapper;

    private static List<AnswerDTO> createTestAnswers() {
        List<AnswerDTO> answers = new ArrayList<>(0);
        AnswerDTO answer = new AnswerDTO();
        answer.setQuestionId("q1");
        answer.setSelectedAnswer("Paris");
        answers.add(answer);
        return answers;
    }

    // Helper methods

    private static List<QuestionDTO> createMockQuestions() {
        QuestionDTO question1 = new QuestionDTO();
        question1.setCategory("Science");
        question1.setQuestion("What is H2O?");
        question1.setDifficulty("easy");
        question1.setAnswers(Arrays.asList("Water", "Carbon Dioxide", "Oxygen", "Hydrogen"));

        QuestionDTO question2 = new QuestionDTO();
        question2.setCategory("History");
        question2.setQuestion("Who was the first president of the United States?");
        question2.setDifficulty("medium");
        question2.setAnswers(Arrays.asList("George Washington", "Thomas Jefferson", "Abraham Lincoln", "John Adams"));

        return Arrays.asList(question1, question2);
    }

    @BeforeEach
    void setUp() {
        _mockMvc = MockMvcBuilders
                .standaloneSetup(_triviaController)
                .build();
        _objectMapper = new ObjectMapper();
    }

    // Test endpoint tests

    @Test
    void testTriviaEndpoint_ReturnsData() throws Exception {
        // Arrange
        String expectedResponse = "{\"response_code\":0,\"results\":[]}";
        when(_triviaService.getTrivia(1)).thenReturn(expectedResponse);

        // Act & Assert
        _mockMvc.perform(get("/test"))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));
    }

    @Test
    void testTriviaEndpoint_HandlesServiceError() throws Exception {
        // Arrange
        when(_triviaService.getTrivia(1)).thenThrow(new RuntimeException("Service Error"));

        // Act & Assert
        _mockMvc.perform(get("/test"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().json(ERROR_FETCH_TRIVIA));
    }

    @Test
    void testTriviaEndpoint_HandlesInterruption() throws Exception {
        // Arrange
        when(_triviaService.getTrivia(1)).thenThrow(new IllegalStateException("Test interrupt"));

        // Act & Assert
        _mockMvc.perform(get("/test"))
                .andExpect(status().is(Errors.ERR_UNAVAILABLE))
                .andExpect(content().json(ERROR_SERVICE_UNAVAILABLE));

        // Verify thread interrupt status was set
        assertTrue(Thread.currentThread().isInterrupted());
        // Clear interrupted status for other tests
        assertTrue(interrupted());
    }

    // Questions endpoint tests

    @Test
    void testGetQuestions_ReturnsQuestionsList() throws Exception {
        // Arrange
        List<QuestionDTO> questions = createMockQuestions();
        when(_triviaService.getQuestions(5)).thenReturn(questions);

        // Act & Assert
        _mockMvc.perform(get("/questions"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testGetQuestions_AcceptsCustomAmount() throws Exception {
        // Arrange
        List<QuestionDTO> questions = createMockQuestions();
        when(_triviaService.getQuestions(10)).thenReturn(questions);

        // Act & Assert
        _mockMvc.perform(get("/questions").param("amount", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetQuestions_HandlesParseException() throws Exception {
        // Arrange
        when(_triviaService.getQuestions(anyInt())).thenThrow(new TriviaParseException("Parse error"));

        // Act & Assert
        _mockMvc.perform(get("/questions"))
                .andExpect(status().isBadGateway())
                .andExpect(content().json(ERROR_PARSING_DATA));
    }

    @Test
    void testGetQuestions_HandlesRuntimeException() throws Exception {
        // Arrange
        when(_triviaService.getQuestions(anyInt()))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        _mockMvc.perform(get("/questions"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().json(ERROR_FETCH_TRIVIA));
    }

    @Test
    void testGetQuestions_HandlesInterruption() throws Exception {
        // Arrange
        when(_triviaService.getQuestions(anyInt()))
                .thenThrow(new IllegalStateException("Interrupted"));

        // Act & Assert
        _mockMvc.perform(get("/questions"))
                .andExpect(status().is(Errors.ERR_UNAVAILABLE))
                .andExpect(content().json(ERROR_SERVICE_UNAVAILABLE));

        // Check thread interrupt status
        assertTrue(interrupted(), "Thread should be interrupted");
    }

    @Test
    void testGetQuestions_DirectCall() {
        // Arrange
        List<QuestionDTO> mockQuestions = new ArrayList<>(0);
        when(_triviaService.getQuestions(10)).thenReturn(mockQuestions);

        // Act
        ResponseEntity<Object> response = _triviaController.getQuestions(10);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockQuestions, response.getBody());
        verify(_triviaService).getQuestions(10);
    }

    // Check answers endpoint tests

    @Test
    void testCheckAnswers_ReturnsResults() throws Exception {
        // Arrange
        List<AnswerDTO> answers = createTestAnswers();

        List<AnswerResultDTO> results = new ArrayList<>(0);
        AnswerResultDTO result = new AnswerResultDTO();
        result.setQuestionId("q1");
        result.setCorrect(true);
        result.setCorrectAnswer("Paris");
        results.add(result);

        when(_triviaService.checkAnswers(any())).thenReturn(results);

        // Act & Assert
        _mockMvc.perform(post("/checkanswers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(_objectMapper.writeValueAsString(answers)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testCheckAnswers_HandlesQuestionNotFoundException() throws Exception {
        // Arrange
        List<AnswerDTO> answers = createTestAnswers();
        answers.get(0).setQuestionId("invalid-id");

        when(_triviaService.checkAnswers(any()))
                .thenThrow(new QuestionNotFoundException("invalid-id"));

        // Act & Assert
        _mockMvc.perform(post("/checkanswers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(_objectMapper.writeValueAsString(answers)))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"error\": \"Invalid question ID: invalid-id\"}"));
    }

    @Test
    void testCheckAnswers_HandlesTriviaServiceException() throws Exception {
        // Arrange
        List<AnswerDTO> answers = createTestAnswers();

        when(_triviaService.checkAnswers(any()))
                .thenThrow(new TriviaServiceException("Service error"));

        // Act & Assert
        _mockMvc.perform(post("/checkanswers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(_objectMapper.writeValueAsString(answers)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().json("{\"error\": \"Service error\"}"));
    }

    @Test
    void testCheckAnswers_HandlesInterruption() throws Exception {
        // Arrange
        List<AnswerDTO> answers = createTestAnswers();

        when(_triviaService.checkAnswers(any()))
                .thenThrow(new IllegalStateException("Interrupted"));

        // Act & Assert
        _mockMvc.perform(post("/checkanswers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(_objectMapper.writeValueAsString(answers)))
                .andExpect(status().is(Errors.ERR_UNAVAILABLE))
                .andExpect(content().json(ERROR_SERVICE_UNAVAILABLE));

        // Check thread interrupt status
        assertTrue(interrupted(), "Thread should be interrupted");
    }

    @Test
    void testCheckAnswers_HandlesRuntimeException() throws Exception {
        // Arrange
        List<AnswerDTO> answers = createTestAnswers();

        when(_triviaService.checkAnswers(any()))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        _mockMvc.perform(post("/checkanswers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(_objectMapper.writeValueAsString(answers)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().json(ERROR_FETCH_TRIVIA));
    }

    @Test
    void testCheckAnswers_DirectCall() {
        // Arrange
        List<AnswerDTO> answers = createTestAnswers();

        List<AnswerResultDTO> results = new ArrayList<>(0);
        AnswerResultDTO result = new AnswerResultDTO();
        result.setQuestionId("q1");
        result.setCorrect(true);
        result.setCorrectAnswer("Paris");
        results.add(result);

        when(_triviaService.checkAnswers(any())).thenReturn(results);

        // Act
        ResponseEntity<Object> response = _triviaController.checkAnswers(answers);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(results, response.getBody());
    }
}