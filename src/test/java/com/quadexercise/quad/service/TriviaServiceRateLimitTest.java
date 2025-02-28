package com.quadexercise.quad.service;

import com.quadexercise.quad.interfaces.InterruptibleRunnable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.quadexercise.quad.service.TriviaService.RATE_LIMIT_MS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SuppressWarnings("DuplicateStringLiteralInspection")
@ExtendWith(MockitoExtension.class)
class TriviaServiceRateLimitTest {
    private static final long SMALL_DELAY_MS = 100L;
    private static final long LARGE_DELAY_MS = 1000L;
    @Mock
    private RestTemplate _restTemplate;
    @Mock
    private RestTemplateBuilder _restTemplateBuilder;
    @Mock
    private MessageService _messageService;
    private TriviaService _triviaService;

    private static long measureExecutionTime(InterruptibleRunnable action) throws InterruptedException {
        long start = System.currentTimeMillis();
        action.run();
        return System.currentTimeMillis() - start;
    }

    private static void waitUntilRateLimitExpires() throws InterruptedException {
        long exactLimit = System.currentTimeMillis() + RATE_LIMIT_MS;
        long current = 0L;
        while (current < exactLimit) {
            Thread.sleep(1L);
            current = System.currentTimeMillis();
        }
    }

    @BeforeEach
    void setUp() {
        when(_restTemplateBuilder.build()).thenReturn(_restTemplate);
        _triviaService = new TriviaService(_restTemplateBuilder, _messageService);
    }

    private Thread getTestingThread(CountDownLatch threadStarted,
                                    AtomicBoolean wasInterrupted,
                                    CountDownLatch interruptProcessed) {
        Thread testThread = new Thread(() -> {
            try {
                when(_restTemplate.getForObject(anyString(), eq(String.class))).thenReturn("{}");

                // First call to set lastRequestTime
                _triviaService.getTrivia(1);
                threadStarted.countDown();

                // Second call should wait and get interrupted
                _triviaService.getTrivia(1);
                fail("Should have thrown exception due to interruption");
            } catch (IllegalStateException e) {
                // Check if the cause is InterruptedException
                wasInterrupted.set(e.getCause() instanceof InterruptedException);
                // Verify thread interrupt flag is set
                wasInterrupted.set(wasInterrupted.get() && Thread.currentThread().isInterrupted());
                interruptProcessed.countDown();
            }
        });

        // Start thread and interrupt it during the wait
        testThread.start();
        return testThread;
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
                () -> assertTrue(RATE_LIMIT_MS - SMALL_DELAY_MS <= duration,
                        "Should wait at least RATE_LIMIT_MS milliseconds"),
                () -> assertTrue((RATE_LIMIT_MS + (SMALL_DELAY_MS * 2L)) > duration,
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

    @Test
    void testInterruptHandling() throws InterruptedException {
        // Set up a test thread that will be interrupted
        AtomicBoolean wasInterrupted = new AtomicBoolean(false);
        CountDownLatch threadStarted = new CountDownLatch(1);
        CountDownLatch interruptProcessed = new CountDownLatch(1);

        Thread testThread = getTestingThread(threadStarted, wasInterrupted, interruptProcessed);

        // Wait for thread to make first call
        threadStarted.await(RATE_LIMIT_MS, TimeUnit.MILLISECONDS);
        Thread.sleep(SMALL_DELAY_MS); // Give thread time to enter waiting state
        testThread.interrupt();

        // Wait for thread to process the interrupt
        interruptProcessed.await(RATE_LIMIT_MS, TimeUnit.MILLISECONDS);
        testThread.join(LARGE_DELAY_MS);

        assertFalse(testThread.isAlive(), "Thread should have terminated");
        assertTrue(wasInterrupted.get(), "Thread should have been interrupted and exception caught");
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
}