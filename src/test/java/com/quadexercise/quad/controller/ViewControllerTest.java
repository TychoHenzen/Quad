package com.quadexercise.quad.controller;

import com.quadexercise.quad.dto.QuestionDTO;
import com.quadexercise.quad.service.TriviaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;

import java.util.List;

import static com.quadexercise.quad.testutilities.TestConstants.*;
import static com.quadexercise.quad.testutilities.TestDataFactory.createMockQuestions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("DuplicateStringLiteralInspection")
@ExtendWith(MockitoExtension.class)
class ViewControllerTest {

    @Mock
    private TriviaService _triviaService;

    @Mock
    private Model _model;

    @InjectMocks
    private ViewController _viewController;

    private MockMvc _mockMvc;

    @BeforeEach
    void setUp() {
        _mockMvc = MockMvcBuilders
                .standaloneSetup(_viewController)
                .build();
    }


    @Test
    void testHomeEndpoint_ReturnsHomeTemplate() throws Exception {
        // Arrange

        // Act & Assert
        _mockMvc.perform(get(HOME_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(HOME_TEMPLATE));
    }

    @Test
    void testPlayEndpoint_ReturnsPageWithQuestions() throws Exception {
        // Arrange
        List<QuestionDTO> mockQuestions = createMockQuestions();
        int customAmount = 3;
        when(_triviaService.getQuestions(customAmount)).thenReturn(mockQuestions);

        // Act & Assert
        _mockMvc.perform(get(PLAY_PATH).param("amount", String.valueOf(customAmount)))
                .andExpect(status().isOk())
                .andExpect(view().name(TRIVIA_TEMPLATE))
                .andExpect(model().attributeExists(QUESTIONS_ATTR))
                .andExpect(model().attribute(QUESTIONS_ATTR, mockQuestions));

        verify(_triviaService).getQuestions(customAmount);
    }

    @Test
    void testPlayEndpoint_UsesDefaultAmount_WhenNotSpecified() throws Exception {
        // Arrange
        List<QuestionDTO> mockQuestions = createMockQuestions();
        when(_triviaService.getQuestions(DEFAULT_QUESTION_AMOUNT)).thenReturn(mockQuestions);

        // Act & Assert
        _mockMvc.perform(get(PLAY_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(TRIVIA_TEMPLATE));

        verify(_triviaService).getQuestions(DEFAULT_QUESTION_AMOUNT);
    }

    @Test
    void testResultsEndpoint_ReturnsResultsTemplate() throws Exception {
        // Arrange

        // Act & Assert
        _mockMvc.perform(get(RESULTS_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(RESULTS_TEMPLATE));
    }

    @Test
    void testPlayTriviaMethod_ReturnsCorrectViewAndAddsQuestions() {
        // Arrange
        List<QuestionDTO> mockQuestions = createMockQuestions();
        when(_triviaService.getQuestions(DEFAULT_QUESTION_AMOUNT)).thenReturn(mockQuestions);

        // Act
        String viewName = _viewController.playTrivia(DEFAULT_QUESTION_AMOUNT, _model);

        // Assert
        assertEquals(TRIVIA_TEMPLATE, viewName);
        verify(_model).addAttribute(QUESTIONS_ATTR, mockQuestions);
    }
}