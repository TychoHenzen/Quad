package com.quadexercise.quad.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TriviaServiceTest {
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
    void testGetTrivia_ShouldReturnData() {
        // Arrange
        String expectedResponse = "{\"response_code\":0,\"results\":[]}";
        when(_restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(expectedResponse);

        // Act
        String result = _triviaService.getTrivia(1);

        // Assert
        assertEquals(expectedResponse, result);
        verify(_restTemplate).getForObject(anyString(), eq(String.class));
    }

    @Test
    void testGetTrivia_ShouldBuildCorrectUrl() {
        // Arrange
        String expectedUrl = "https://opentdb.com/api.php?amount=1";
        when(_restTemplate.getForObject(expectedUrl, String.class))
                .thenReturn("{}");

        // Act
        _triviaService.getTrivia(1);

        // Assert

        verify(_restTemplate).getForObject(expectedUrl, String.class);
    }


    @Test
    void testGetTrivia_ShouldHandleErrorResponses() {
        // Arrange
        when(_restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RestClientException("API Error"));

        // Act & Assert
        assertThrows(RestClientException.class, () -> _triviaService.getTrivia(1));
    }


    @Test
    void testGetTrivia_ShouldValidateInputAmount() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> _triviaService.getTrivia(-1),
                "Should throw exception for negative amounts");
        assertThrows(IllegalArgumentException.class, () -> _triviaService.getTrivia(0),
                "Should throw exception for zero amount");
    }
}