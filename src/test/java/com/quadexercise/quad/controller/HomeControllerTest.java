package com.quadexercise.quad.controller;

import com.quadexercise.quad.service.TriviaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class HomeControllerTest {

    @Mock
    private TriviaService _triviaService;

    @InjectMocks
    private ViewController _homeController;
    @InjectMocks
    private TriviaController _triviaController;

    private MockMvc _mockMvc;

    @BeforeEach
    void setUp() {
        _mockMvc = MockMvcBuilders
                .standaloneSetup(_homeController, _triviaController)
                .build();
    }

    @Test
    void testHome_ShouldReturnHomePage() throws Exception {
        _mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"));
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
}