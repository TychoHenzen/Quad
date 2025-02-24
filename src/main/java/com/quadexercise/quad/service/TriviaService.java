package com.quadexercise.quad.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.function.Supplier;

@Service
public class TriviaService {
    public static final long RATE_LIMIT_MS = 5000L;
    private static final String RATE_LIMIT_INTERRUPTED_MESSAGE = "Rate limit wait interrupted";

    private final RestTemplate _restTemplate;
    private long _lastRequestTime;

    @Autowired
    public TriviaService(RestTemplateBuilder restTemplateBuilder) {
        _restTemplate = restTemplateBuilder.build();
    }

    public synchronized String getTrivia(int amount) {
        if (0 >= amount) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        return rateLimit(() -> fetchTrivia(amount));
    }

    private String fetchTrivia(int amount) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .newInstance()
                .scheme("https")
                .host("opentdb.com")
                .path("/api.php")
                .queryParam("amount", amount);

        return _restTemplate.getForObject(
                builder.toUriString(),
                String.class
        );
    }

    private <T> T rateLimit(Supplier<T> operation) {
        Thread currentThread = Thread.currentThread();

        synchronized (this) {
            long currentTime = System.currentTimeMillis();
            long elapsed = currentTime - _lastRequestTime;
            long requiredWait = RATE_LIMIT_MS - elapsed;

            if (0L < requiredWait) {
                waitForRateLimit(requiredWait, currentThread);
            }

            try {
                return operation.get();
            } finally {
                _lastRequestTime = System.currentTimeMillis();
            }
        }
    }

    private synchronized void waitForRateLimit(long requiredWait, Thread currentThread) {
        long remainingWait = requiredWait;
        long waitStart = System.currentTimeMillis();

        while (0L < remainingWait) {
            try {
                wait(remainingWait);
            } catch (InterruptedException e) {
                currentThread.interrupt();
                throw new IllegalStateException(RATE_LIMIT_INTERRUPTED_MESSAGE, e);
            }

            if (Thread.interrupted()) {
                currentThread.interrupt();
                throw new IllegalStateException(RATE_LIMIT_INTERRUPTED_MESSAGE);
            }

            remainingWait = requiredWait - (System.currentTimeMillis() - waitStart);
        }
    }
}
