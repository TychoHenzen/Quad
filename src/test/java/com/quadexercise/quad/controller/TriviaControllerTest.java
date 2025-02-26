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
import java.util.Collection;
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

    @Mock
    private TriviaService _triviaService;

    @InjectMocks
    private TriviaController _triviaController;

    private MockMvc _mockMvc;
    private ObjectMapper _objectMapper;

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

    @Test
    void testTrivia_ShouldReturnTriviaData() throws Exception {
        // Arrange
        String expectedResponse = "{\"response_code\":0,\"results\":[]}";
        when(_triviaService.getTrivia(1)).thenReturn(expectedResponse);

        // Act & Assert
        _mockMvc.perform(get("/test"))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));
    }

    @Test
    void testTrivia_ShouldHandleServiceError() throws Exception {
        // Arrange
        when(_triviaService.getTrivia(1)).thenThrow(new RuntimeException("Service Error"));

        // Act & Assert
        _mockMvc.perform(get("/test"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().json("{\"error\": \"Failed to fetch trivia\"}"));
    }

    @Test
    void testTrivia_ShouldHandleInterruption() throws Exception {
        // Arrange
        when(_triviaService.getTrivia(1)).thenThrow(new IllegalStateException("Test interrupt"));

        // Act & Assert
        _mockMvc.perform(get("/test"))
                .andExpect(status().is(Errors.ERR_UNAVAILABLE))
                .andExpect(content().json("{\"error\": \"Service temporarily unavailable\"}"));

        // Verify thread interrupt status was set
        assertTrue(Thread.currentThread().isInterrupted());
        assertTrue(interrupted()); // Clear interrupted status for other tests
    }

    @BeforeEach
    void setUp() {
        _mockMvc = MockMvcBuilders
                .standaloneSetup(_triviaController)
                .build();
        _objectMapper = new ObjectMapper();
    }

    @Test
    void testGetQuestions_ShouldReturnQuestions() throws Exception {
        // Arrange
        List<QuestionDTO> questions = createMockQuestions();
        when(_triviaService.getQuestions(5)).thenReturn(questions);

        // Act & Assert
        _mockMvc.perform(get("/questions"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testGetQuestions_WithCustomAmount() throws Exception {
        // Arrange
        List<QuestionDTO> questions = createMockQuestions();
        when(_triviaService.getQuestions(10)).thenReturn(questions);

        // Act & Assert
        _mockMvc.perform(get("/questions").param("amount", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetQuestions_ShouldHandleParseException() throws Exception {
        // Arrange
        when(_triviaService.getQuestions(anyInt())).thenThrow(new TriviaParseException("Parse error"));

        // Act & Assert
        _mockMvc.perform(get("/questions"))
                .andExpect(status().isBadGateway())
                .andExpect(content().json("{\"error\": \"Error parsing trivia data from provider\"}"));
    }

    @Test
    void testCheckAnswers_ShouldReturnResults() throws Exception {
        // Arrange
        Collection<AnswerDTO> answers = new ArrayList<>(0);
        AnswerDTO answer = new AnswerDTO();
        answer.setQuestionId("q1");
        answer.setSelectedAnswer("Paris");
        answers.add(answer);

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
    void testCheckAnswers_ShouldHandleQuestionNotFoundException() throws Exception {
        // Arrange
        Collection<AnswerDTO> answers = new ArrayList<>(0);
        AnswerDTO answer = new AnswerDTO();
        answer.setQuestionId("invalid-id");
        answer.setSelectedAnswer("Wrong Answer");
        answers.add(answer);

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
    void testCheckAnswers_ShouldHandleTriviaServiceException() throws Exception {
        // Arrange
        Collection<AnswerDTO> answers = new ArrayList<>(0);
        AnswerDTO answer = new AnswerDTO();
        answer.setQuestionId("q1");
        answer.setSelectedAnswer("Paris");
        answers.add(answer);

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
    void testDirectMethodCall_CheckAnswers() {
        // Arrange
        List<AnswerDTO> answers = new ArrayList<>(0);
        AnswerDTO answer = new AnswerDTO();
        answer.setQuestionId("q1");
        answer.setSelectedAnswer("Paris");
        answers.add(answer);

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

    @Test
    void testGetQuestions_WithRuntimeException() throws Exception {
        // Arrange
        when(_triviaService.getQuestions(anyInt()))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        _mockMvc.perform(get("/questions"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().json("{\"error\": \"Failed to fetch trivia\"}"));
    }

    @Test
    void testGetQuestions_WithIllegalStateException() throws Exception {
        // Arrange
        when(_triviaService.getQuestions(anyInt()))
                .thenThrow(new IllegalStateException("Interrupted"));

        // Act & Assert
        _mockMvc.perform(get("/questions"))
                .andExpect(status().is(Errors.ERR_UNAVAILABLE))
                .andExpect(content().json("{\"error\": \"Service temporarily unavailable\"}"));

        // Check thread interrupt status
        assertTrue(interrupted(), "Thread should be interrupted");
    }

    @Test
    void testDirectMethodCall_GetQuestions() {
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

}