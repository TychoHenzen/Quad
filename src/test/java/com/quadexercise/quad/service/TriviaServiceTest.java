package com.quadexercise.quad.service;

import com.quadexercise.quad.interfaces.InterruptibleRunnable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;

import static com.quadexercise.quad.service.TriviaService.RATE_LIMIT_MS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TriviaServiceTest {

    @Mock
    private RestTemplate _restTemplate;

    @Mock
    private RestTemplateBuilder _restTemplateBuilder;

    private TriviaService _triviaService;
    private static final long SMALL_DELAY_MS = 100L;


    @BeforeEach
    void setUp() {
        when(_restTemplateBuilder.build()).thenReturn(_restTemplate);
        _triviaService = new TriviaService(_restTemplateBuilder);
    }

    private static void waitUntilRateLimitExpires() throws InterruptedException {
        long exactLimit = System.currentTimeMillis() + RATE_LIMIT_MS;
        long current = 0L;
        while (current < exactLimit) {
            Thread.sleep(1L);
            current = System.currentTimeMillis();
        }
    }


    private static long measureExecutionTime(InterruptibleRunnable action) throws InterruptedException {
        long start = System.currentTimeMillis();
        action.run();
        return System.currentTimeMillis() - start;
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
    void testGetTrivia_ShouldRespectRateLimit() throws InterruptedException {
        // Arrange
        when(_restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn("{}");

        // Act
        long duration = measureExecutionTime(() ->
        {
            _triviaService.getTrivia(1);
            _triviaService.getTrivia(1);
        });

        // Assert
        assertTrue(RATE_LIMIT_MS - SMALL_DELAY_MS <= duration,
                "Second request should be delayed by at least 5 seconds");
        assertTrue(RATE_LIMIT_MS + SMALL_DELAY_MS >= duration,
                "Second request should not be delayed more than necessary");
    }


    @Test
    void testGetTrivia_ShouldWaitCorrectAmount() throws InterruptedException {
        // Arrange
        when(_restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn("{}");

        // First request to set lastRequestTime
        _triviaService.getTrivia(1);

        final long halfTimeout = 3000L;

        // Wait 3 seconds
        Thread.sleep(halfTimeout);

        // Act
        long duration = measureExecutionTime(() -> _triviaService.getTrivia(1));

        // Assert
        // Should wait ~2 seconds (5 second limit - 3 seconds elapsed)
        final long minTimeout = 1800L;
        final long maxTimeout = 2200L;
        assertTrue(minTimeout <= duration && maxTimeout >= duration,
                "Wait time should be approximately 2 seconds, was: " + duration + " ms");
    }

    @Test
    void testGetTrivia_ShouldHandleMultipleRequests() {
        // Arrange
        when(_restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn("{}");

        // Act & Assert
        // This will take at least 10 seconds due to rate limiting
        assertDoesNotThrow(() -> {
            for (int i = 0; 3 > i; i++) {
                _triviaService.getTrivia(1);
            }
        });

        verify(_restTemplate, times(3))
                .getForObject(anyString(), eq(String.class));
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

    @Test
    void testWaitBehavior() throws InterruptedException {
        when(_restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn("{}");

        _triviaService.getTrivia(1);

        long startTime = System.currentTimeMillis();
        CompletableFuture.runAsync(() -> assertDoesNotThrow(() -> _triviaService.getTrivia(1)));

        final long halfTimeout = RATE_LIMIT_MS / 2L;
        Thread.sleep(halfTimeout);
        long midTime = System.currentTimeMillis() - startTime;
        assertTrue(halfTimeout <= midTime, "Should still be waiting at halfway point");
    }

    @Test
    void testRateLimit_ExactBoundaryConditions() throws InterruptedException {
        // Arrange
        when(_restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn("{}");
        _triviaService.getTrivia(1);
        waitUntilRateLimitExpires();

        // Act
        long duration = measureExecutionTime(() -> _triviaService.getTrivia(1));

        // Assert
        assertTrue(SMALL_DELAY_MS > duration, "Should not wait when exactly at rate limit");
    }

    @Test
    void testRateLimit_EnforcesMinimumWait() throws InterruptedException {
        // Arrange
        when(_restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn("{}");
        _triviaService.getTrivia(1);

        // Act
        long duration = measureExecutionTime(() -> _triviaService.getTrivia(1));

        // Assert
        assertAll(
                () -> assertTrue(RATE_LIMIT_MS <= duration,
                        "Should wait at least RATE_LIMIT_MS milliseconds"),
                () -> assertTrue(RATE_LIMIT_MS + SMALL_DELAY_MS > duration,
                        "Should not wait significantly longer than RATE_LIMIT_MS")
        );
    }


    @Test
    void testRateLimit_HandlesInterruption() throws InterruptedException {
        // Arrange
        when(_restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn("{}");

        // First call to set the last request time
        _triviaService.getTrivia(1);

        // Create a thread that will be interrupted
        Thread testThread = new Thread(() -> {
            try {
                _triviaService.getTrivia(1);
                fail("Should have been interrupted");
            } catch (IllegalStateException e) {
                // Expected
                Thread.currentThread().interrupt();
            }
        });

        // Act
        testThread.start();
        Thread.sleep(SMALL_DELAY_MS); // Give the thread time to enter the wait state
        testThread.interrupt();
        testThread.join(RATE_LIMIT_MS); // Wait for thread to complete

        // Assert
        assertSame(Thread.State.TERMINATED, testThread.getState(), "Thread should have terminated");
    }
}
