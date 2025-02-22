package com.quadexercise.quad.controller;

import com.quadexercise.quad.service.TriviaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class HomeControllerTest {

    @Mock
    private TriviaService triviaService;

    @InjectMocks
    private ViewController homeController;
    @InjectMocks
    private TriviaController triviaController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(homeController, triviaController)
                .build();
    }

    @Test
    void home_ShouldReturnHomePage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"));
    }

    @Test
    void testTrivia_ShouldReturnTriviaData() throws Exception {
        // Arrange
        String expectedResponse = "{\"response_code\":0,\"results\":[]}";
        when(triviaService.getTrivia(1)).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(get("/test"))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));
    }

    @Test
    void testTrivia_ShouldHandleServiceError() throws Exception {
        // Arrange
        when(triviaService.getTrivia(1)).thenThrow(new RuntimeException("Service Error"));

        // Act & Assert
        mockMvc.perform(get("/test"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().json("{\"error\": \"Failed to fetch trivia\"}"));
    }
}