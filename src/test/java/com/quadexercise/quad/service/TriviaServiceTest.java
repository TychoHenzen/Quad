package com.quadexercise.quad.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TriviaServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    private TriviaService triviaService;

    @BeforeEach
    void setUp() {
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        triviaService = new TriviaService(restTemplateBuilder);
    }

    @Test
    void getTrivia_ShouldReturnData() throws InterruptedException {
        // Arrange
        String expectedResponse = "{\"response_code\":0,\"results\":[]}";
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(expectedResponse);

        // Act
        String result = triviaService.getTrivia(1);

        // Assert
        assertEquals(expectedResponse, result);
        verify(restTemplate).getForObject(anyString(), eq(String.class));
    }

    @Test
    void getTrivia_ShouldRespectRateLimit() throws InterruptedException {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn("{}");

        // Act
        long startTime = System.currentTimeMillis();
        triviaService.getTrivia(1);
        triviaService.getTrivia(1);
        long endTime = System.currentTimeMillis();

        // Assert
        long timeDifference = endTime - startTime;
        assertTrue(timeDifference >= 5000,
                "Second request should be delayed by at least 5 seconds");
        assertTrue(timeDifference <= 5500,
                "Second request should not be delayed more than necessary");
    }

    @Test
    void getTrivia_ShouldWaitCorrectAmount() throws InterruptedException {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn("{}");

        // First request to set lastRequestTime
        triviaService.getTrivia(1);

        // Wait 3 seconds
        wait(3000);

        // Act
        long startTime = System.currentTimeMillis();
        triviaService.getTrivia(1);
        long endTime = System.currentTimeMillis();

        // Assert
        long timeDifference = endTime - startTime;
        // Should wait ~2 seconds (5 second limit - 3 seconds elapsed)
        assertTrue(timeDifference >= 1800 && timeDifference <= 2200,
                "Wait time should be approximately 2 seconds, was: " + timeDifference + "ms");
    }

    @Test
    void getTrivia_ShouldHandleMultipleRequests() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn("{}");

        // Act & Assert
        // This will take at least 10 seconds due to rate limiting
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 3; i++) {
                triviaService.getTrivia(1);
            }
        });

        verify(restTemplate, times(3))
                .getForObject(anyString(), eq(String.class));
    }

    @Test
    void getTrivia_ShouldBuildCorrectUrl() throws InterruptedException {
        // Arrange
        String expectedUrl = "https://opentdb.com/api.php?amount=1";
        when(restTemplate.getForObject(expectedUrl, eq(String.class)))
                .thenReturn("{}");

        // Act
        triviaService.getTrivia(1);

        // Assert
        
        verify(restTemplate).getForObject(expectedUrl, eq(String.class));
    }

    @Test
    void getTrivia_ShouldHandleErrorResponses() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RestClientException("API Error"));

        // Act & Assert
        assertThrows(RestClientException.class, () -> triviaService.getTrivia(1));
    }

    @Test
    void getTrivia_ShouldValidateInputAmount() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> triviaService.getTrivia(-1),
                "Should throw exception for negative amounts");
        assertThrows(IllegalArgumentException.class, () -> triviaService.getTrivia(0),
                "Should throw exception for zero amount");
    }
}