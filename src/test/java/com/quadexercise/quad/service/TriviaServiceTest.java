package com.quadexercise.quad.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static com.quadexercise.quad.testutilities.TestConstants.EMPTY_RESPONSE;
import static com.quadexercise.quad.testutilities.TestConstants.EXPECTED_API_URL;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("DuplicateStringLiteralInspection")
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
        when(_restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(EMPTY_RESPONSE);

        // Act
        String result = _triviaService.getTrivia(1);

        // Assert
        assertEquals(EMPTY_RESPONSE, result);
        verify(_restTemplate).getForObject(anyString(), eq(String.class));
    }

    @Test
    void testGetTrivia_ShouldBuildCorrectUrl() {
        // Arrange
        when(_restTemplate.getForObject(EXPECTED_API_URL, String.class))
                .thenReturn("{}");

        // Act
        _triviaService.getTrivia(1);

        // Assert
        verify(_restTemplate).getForObject(EXPECTED_API_URL, String.class);
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
        // Arrange
        when(_messageService.getMessage(anyString()))
                .thenReturn("Amount must be greater than zero");

        // Act & Assert
        Exception negativeEx = assertThrows(
                IllegalArgumentException.class,
                () -> _triviaService.getTrivia(-1),
                "Should throw exception for negative amounts"
        );

        Exception zeroEx = assertThrows(
                IllegalArgumentException.class,
                () -> _triviaService.getTrivia(0),
                "Should throw exception for zero amount"
        );

        assertTrue(negativeEx.getMessage().contains("greater than zero"));
        assertTrue(zeroEx.getMessage().contains("greater than zero"));
    }
}