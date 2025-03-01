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
import java.util.concurrent.atomic.AtomicLong;

import static com.quadexercise.quad.testUtils.TestConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SuppressWarnings({"DuplicateStringLiteralInspection", "ResultOfMethodCallIgnored"})
@ExtendWith(MockitoExtension.class)
class TriviaServiceRateLimitTest {

    @Mock
    private RestTemplate _restTemplate;

    @Mock
    private RestTemplateBuilder _restTemplateBuilder;

    @Mock
    private MessageService _messageService;

    private TriviaService _triviaService;


    /**
     * Helper method to wait until the rate limit period expires
     */
    private static void waitUntilRateLimitExpires() throws InterruptedException {
        Thread.sleep(RATE_LIMIT_MS + SMALL_DELAY_MS);
    }

    /**
     * Helper method to measure execution time of a given action
     */
    private static long measureExecutionTime(InterruptibleRunnable action) throws InterruptedException {
        long start = System.currentTimeMillis();
        action.run();
        return System.currentTimeMillis() - start;
    }

    @BeforeEach
    void setUp() {
        // Arrange
        when(_restTemplateBuilder.build()).thenReturn(_restTemplate);
        _triviaService = new TriviaService(_restTemplateBuilder, _messageService);
    }

    private void interruptThread(CountDownLatch threadStarted,
                                 AtomicBoolean wasInterrupted,
                                 CountDownLatch interruptProcessed) {
        // Setup - not under test for exceptions
        when(_restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(EMPTY_JSON_RESPONSE);
        _triviaService.getTrivia(1); // First call to set lastRequestTime
        threadStarted.countDown();

        try {
            // Act - This specific call is expected to throw the exception
            _triviaService.getTrivia(1);

            // If we reach here, the test failed
            fail("Should have thrown exception due to interruption");
        } catch (IllegalStateException e) {
            // Assert the exception details
            boolean isInterruptedException = e.getCause() instanceof InterruptedException;
            boolean isThreadInterrupted = Thread.currentThread().isInterrupted();

            // Both conditions must be true
            wasInterrupted.set(isInterruptedException && isThreadInterrupted);
            interruptProcessed.countDown();
        }
    }

    private void ConcurrentRequestThread(CountDownLatch startSignal,
                                         AtomicLong threadStartTime,
                                         AtomicLong threadEndTime,
                                         CountDownLatch doneSignal) {
        try {
            startSignal.await();
            threadStartTime.set(System.currentTimeMillis());
            _triviaService.getTrivia(1);
            threadEndTime.set(System.currentTimeMillis());
            doneSignal.countDown();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    void testRateLimit_SecondRequestIsDelayed() throws InterruptedException {
        // Arrange
        when(_restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn("{}");

        // Act
        long duration = measureExecutionTime(() -> {
            _triviaService.getTrivia(1);
            _triviaService.getTrivia(1);
        });

        // Assert
        assertTrue(RATE_LIMIT_MS - SMALL_DELAY_MS <= duration,
                String.format("Second request should be delayed by at least %d ms",
                        RATE_LIMIT_MS - SMALL_DELAY_MS));

        assertTrue(RATE_LIMIT_MS + SMALL_DELAY_MS >= duration,
                String.format("Second request should not be delayed more than %d ms",
                        RATE_LIMIT_MS + SMALL_DELAY_MS));
    }

    @Test
    void testRateLimit_PartialDelay() throws InterruptedException {
        // Arrange
        when(_restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn("{}");

        // First request to set lastRequestTime
        _triviaService.getTrivia(1);

        final long halfTimeout = RATE_LIMIT_MS / 2L;

        // Wait half the timeout period
        Thread.sleep(halfTimeout);

        // Act
        long duration = measureExecutionTime(() -> _triviaService.getTrivia(1));

        // Assert
        // Should wait approximately the remaining time
        final long expectedDelay = RATE_LIMIT_MS - halfTimeout;
        final long minExpectedDelay = expectedDelay - SMALL_DELAY_MS;
        final long maxExpectedDelay = expectedDelay + SMALL_DELAY_MS;

        String message = String.format("Wait time should be approximately %d ms, was: %d ms",
                expectedDelay, duration);
        assertTrue(minExpectedDelay <= duration && maxExpectedDelay >= duration, message);
    }

    @Test
    void testRateLimit_ExactBoundaryTiming() throws InterruptedException {
        // Arrange
        when(_restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn("{}");
        _triviaService.getTrivia(1);
        waitUntilRateLimitExpires();

        // Act
        long duration = measureExecutionTime(() -> _triviaService.getTrivia(1));

        // Assert
        String message = String.format("Should not wait when rate limit has expired (time: %d ms)", duration);
        assertTrue(SMALL_DELAY_MS > duration, message);
    }

    @Test
    void testRateLimit_EnforcesMinimumWaitTime() throws InterruptedException {
        // Arrange
        when(_restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn("{}");
        _triviaService.getTrivia(1);

        // Act
        long duration = measureExecutionTime(() -> _triviaService.getTrivia(1));

        // Assert
        String minMessage = String.format("Should wait at least %d ms", RATE_LIMIT_MS - SMALL_DELAY_MS);
        String maxMessage = String.format("Should not wait more than %d ms", RATE_LIMIT_MS + SMALL_DELAY_MS * 2L);

        assertAll(
                () -> assertTrue(RATE_LIMIT_MS - SMALL_DELAY_MS <= duration, minMessage),
                () -> assertTrue(RATE_LIMIT_MS + (SMALL_DELAY_MS * 2L) > duration, maxMessage)
        );
    }

    @Test
    void testRateLimit_WaitIsInterruptible() throws InterruptedException {
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
        Thread.sleep(SMALL_DELAY_MS); // Give thread time to enter wait state
        testThread.interrupt();
        testThread.join(LARGE_DELAY_MS); // Wait for thread to complete

        // Assert
        assertSame(Thread.State.TERMINATED, testThread.getState(), "Thread should have terminated");
    }

    @Test
    void testRateLimit_ControlledTimeMechanism() {
        // Arrange
        TriviaService serviceSpy = spy(_triviaService);

        // Mock current time to return controlled values
        AtomicLong currentTime = new AtomicLong(LARGE_DELAY_MS);
        doAnswer(inv -> currentTime.get()).when(serviceSpy).getCurrentTimeMillis();

        // Set up response
        when(_restTemplate.getForObject(anyString(), eq(String.class))).thenReturn("{}");

        // First call - sets lastRequestTime
        serviceSpy.getTrivia(1);

        // Advance time slightly (but less than rate limit)
        currentTime.set(LARGE_DELAY_MS * 2L);

        // Act
        serviceSpy.getTrivia(1);

        // Assert
        verify(serviceSpy, atLeastOnce()).waitForRateLimit(eq(RATE_LIMIT_MS - LARGE_DELAY_MS), any());
    }

    @Test
    void testRateLimit_InterruptProcessing() throws InterruptedException {
        // Arrange
        AtomicBoolean wasInterrupted = new AtomicBoolean(false);
        CountDownLatch threadStarted = new CountDownLatch(1);
        CountDownLatch interruptProcessed = new CountDownLatch(1);

        Thread testThread = new Thread(() -> interruptThread(threadStarted, wasInterrupted, interruptProcessed));
        testThread.start();

        // Wait for thread to make first call
        threadStarted.await(RATE_LIMIT_MS, TimeUnit.MILLISECONDS);
        Thread.sleep(SMALL_DELAY_MS); // Give thread time to enter waiting state

        testThread.interrupt();

        // Wait for thread to process the interrupt
        interruptProcessed.await(RATE_LIMIT_MS, TimeUnit.MILLISECONDS);
        testThread.join(LARGE_DELAY_MS);

        // Assert
        assertFalse(testThread.isAlive(), "Thread should have terminated");
        assertTrue(wasInterrupted.get(), "Thread should have been interrupted and exception caught");
    }

    @SuppressWarnings("ConstantValue")
    @Test
    void testRateLimit_ConcurrentRequests() throws InterruptedException {
        // Arrange
        when(_restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn("{}");

        // Set up two threads that will try to make requests at the same time
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(2);

        AtomicLong thread1StartTime = new AtomicLong(0L);
        AtomicLong thread1EndTime = new AtomicLong(0L);
        AtomicLong thread2StartTime = new AtomicLong(0L);
        AtomicLong thread2EndTime = new AtomicLong(0L);

        Thread thread1 = new Thread(() ->
                ConcurrentRequestThread(startSignal, thread1StartTime, thread1EndTime, doneSignal));

        Thread thread2 = new Thread(() ->
                ConcurrentRequestThread(startSignal, thread2StartTime, thread2EndTime, doneSignal));

        // Act
        thread1.start();
        thread2.start();
        startSignal.countDown(); // Let threads proceed
        doneSignal.await(RATE_LIMIT_MS * 3L, TimeUnit.MILLISECONDS); // Wait for both to finish

        // Assert
        // One thread should complete quickly, the other should be delayed
        long thread1Duration = thread1EndTime.get() - thread1StartTime.get();
        long thread2Duration = thread2EndTime.get() - thread2StartTime.get();

        // At least one thread should be delayed by rate limit
        String message = String.format("At least one thread should be delayed by rate limit. " +
                "Thread1: %d ms, Thread2: %d ms", thread1Duration, thread2Duration);
        assertTrue(thread1Duration > RATE_LIMIT_MS - SMALL_DELAY_MS ||
                thread2Duration > RATE_LIMIT_MS - SMALL_DELAY_MS, message);

        // If both requests were processed sequentially due to synchronization:
        if (thread1Duration < SMALL_DELAY_MS * 2L) {
            // Thread 1 was fast, thread 2 should be delayed
            assertTrue(thread2Duration >= RATE_LIMIT_MS - SMALL_DELAY_MS,
                    "Second thread should be delayed when first thread is fast");
        } else if (thread2Duration < SMALL_DELAY_MS * 2L) {
            // Thread 2 was fast, thread 1 should be delayed
            assertTrue(thread1Duration >= RATE_LIMIT_MS - SMALL_DELAY_MS,
                    "First thread should be delayed when second thread is fast");
        }
    }
}