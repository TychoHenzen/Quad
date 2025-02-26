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

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
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
    void testHome_ShouldReturnHomePage() throws Exception {
        _mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("homeTemplate"));
    }

    @Test
    void testPlayTrivia_ShouldReturnTriviaPageWithQuestions() throws Exception {
        // Arrange
        List<QuestionDTO> mockQuestions = createMockQuestions();
        when(_triviaService.getQuestions(anyInt())).thenReturn(mockQuestions);

        // Act & Assert
        _mockMvc.perform(get("/play").param("amount", "3"))
                .andExpect(status().isOk())
                .andExpect(view().name("triviaTemplate"))
                .andExpect(model().attributeExists("questions"))
                .andExpect(model().attribute("questions", mockQuestions));

        verify(_triviaService).getQuestions(3);
    }

    @Test
    void testPlayTrivia_WithDefaultAmount() throws Exception {
        // Arrange
        List<QuestionDTO> mockQuestions = createMockQuestions();
        when(_triviaService.getQuestions(5)).thenReturn(mockQuestions);

        // Act & Assert
        _mockMvc.perform(get("/play"))
                .andExpect(status().isOk())
                .andExpect(view().name("triviaTemplate"));

        verify(_triviaService).getQuestions(5);
    }

    @Test
    void testResults_ShouldReturnResultsPage() throws Exception {
        _mockMvc.perform(get("/results"))
                .andExpect(status().isOk())
                .andExpect(view().name("resultsTemplate"));
    }

    @Test
    void testDirectMethodCall_PlayTrivia() {
        // Arrange
        List<QuestionDTO> mockQuestions = createMockQuestions();
        when(_triviaService.getQuestions(5)).thenReturn(mockQuestions);

        // Act
        String viewName = _viewController.playTrivia(5, _model);

        // Assert
        assertEquals("triviaTemplate", viewName);
        verify(_model).addAttribute("questions", mockQuestions);
    }
}