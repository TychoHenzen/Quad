package com.quadexercise.quad.service;

import com.quadexercise.quad.utils.ApiConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

/**
 * Service responsible for implementing rate limiting for API requests.
 * Ensures that requests are not made too frequently.
 */
@Service
public class TriviaRateLimitService {

    private final MessageService _messageService;
    private long _lastRequestTime;

    @Autowired
    public TriviaRateLimitService(MessageService messageService) {
        _messageService = messageService;
        _lastRequestTime = 0L;
    }

    /**
     * Gets the current system time in milliseconds.
     * Extracted to facilitate testing with mocks.
     *
     * @return Current time in milliseconds
     */
    @SuppressWarnings("MethodMayBeStatic")
    public long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * Executes an operation with rate limiting applied.
     * Ensures that operations are spaced at least RATE_LIMIT_MS milliseconds apart.
     *
     * @param operation The operation to execute
     * @param <T>       The return type of the operation
     * @return The result of the operation
     * @throws IllegalStateException if the thread is interrupted while waiting
     */
    <T> T rateLimit(Supplier<T> operation) {
        Thread currentThread = Thread.currentThread();

        synchronized (this) {
            long currentTime = getCurrentTimeMillis();
            long elapsed = currentTime - _lastRequestTime;
            long millisecondsToWait = ApiConstants.RATE_LIMIT_MS - elapsed;

            if (millisecondsToWait > 0L) {
                waitForRateLimit(millisecondsToWait, currentThread);
            }
            try {
                return operation.get();
            } finally {
                _lastRequestTime = getCurrentTimeMillis();
            }
        }
    }

    /**
     * Waits for the rate limit period to expire.
     *
     * @param millisecondsToWait The number of milliseconds to wait
     * @param currentThread      The current thread
     * @throws IllegalStateException if the thread is interrupted while waiting
     */
    synchronized void waitForRateLimit(long millisecondsToWait, Thread currentThread) {
        long waitEnd = System.currentTimeMillis() + millisecondsToWait;
        long remainingWait = millisecondsToWait;

        while (remainingWait > 0L) {
            try {
                wait(remainingWait);
            } catch (InterruptedException e) {
                boolean wasInterrupted = Thread.interrupted();
                currentThread.interrupt();
                if (wasInterrupted) {
                    throw new IllegalStateException(
                            _messageService.getMessage(ApiConstants.RATE_LIMIT_INTERRUPTED), e);
                }
                throw new IllegalStateException(
                        String.format("%s (Thread wasn't interrupted)",
                                _messageService.getMessage(
                                        ApiConstants.RATE_LIMIT_INTERRUPTED)), e);
            }

            remainingWait = waitEnd - System.currentTimeMillis();
        }
    }
}
