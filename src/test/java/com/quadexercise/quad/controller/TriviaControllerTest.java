package com.quadexercise.quad.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quadexercise.quad.dto.AnswerDTO;
import com.quadexercise.quad.dto.AnswerResultDTO;
import com.quadexercise.quad.dto.QuestionDTO;
import com.quadexercise.quad.exceptions.QuestionNotFoundException;
import com.quadexercise.quad.exceptions.TriviaParseException;
import com.quadexercise.quad.exceptions.TriviaServiceException;
import com.quadexercise.quad.interfaces.ITriviaService;
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
import java.util.List;

import static com.quadexercise.quad.testutilities.TestConstants.*;
import static com.quadexercise.quad.testutilities.TestDataFactory.*;
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

    @Mock
    private ITriviaService _triviaService;

    @InjectMocks
    private TriviaController _triviaController;

    private MockMvc _mockMvc;
    private ObjectMapper _objectMapper;

    @BeforeEach
    void setUp() {
        _mockMvc = MockMvcBuilders
                .standaloneSetup(_triviaController)
                .build();
        _objectMapper = new ObjectMapper();
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
                .andExpect(status().is(HttpStatus.SERVICE_UNAVAILABLE.value()))
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


        List<AnswerResultDTO> results = createTestResults();

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
                .andExpect(status().is(HttpStatus.SERVICE_UNAVAILABLE.value()))
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

        List<AnswerResultDTO> results = createTestResults();

        when(_triviaService.checkAnswers(any())).thenReturn(results);

        // Act
        ResponseEntity<Object> response = _triviaController.checkAnswers(answers);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(results, response.getBody());
    }
}