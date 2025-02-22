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
    private static final long NS_PER_MS = 1_000_000L;
    private final RestTemplate _restTemplate;
    private long _lastRequestTime;

    @Autowired
    public TriviaService(RestTemplateBuilder restTemplateBuilder) {
        _restTemplate = restTemplateBuilder.build();
    }

    public synchronized String getTrivia(int amount) throws InterruptedException {
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

    private <T> T rateLimit(Supplier<T> operation) throws InterruptedException {
        Thread currentThread = Thread.currentThread();

        synchronized (this) {
            long startWait = System.nanoTime();
            long elapsed = startWait - _lastRequestTime;

            long timeToWait = RATE_LIMIT_MS - (elapsed) / NS_PER_MS;

            while (0L < timeToWait) {
                long waitStartTime = System.nanoTime();
                wait(timeToWait);
                long actualWaitNanos = System.nanoTime() - waitStartTime;
                timeToWait -= actualWaitNanos / NS_PER_MS;

                if (Thread.interrupted()) {
                    currentThread.interrupt();
                    throw new IllegalStateException("Rate limit wait interrupted");
                }
            }

            try {
                return operation.get();
            } finally {
                _lastRequestTime = System.nanoTime() / NS_PER_MS;
            }
        }
    }
}
