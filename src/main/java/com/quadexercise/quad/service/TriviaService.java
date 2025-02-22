package com.quadexercise.quad.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.function.Supplier;

@Service
public class TriviaService {
    private static final long RATE_LIMIT_MS = 5000L;
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

    private synchronized <T> T rateLimit(Supplier<T> operation) throws InterruptedException {
        long currentTime = System.currentTimeMillis();
        while (RATE_LIMIT_MS > currentTime - _lastRequestTime) {
            currentTime = System.currentTimeMillis();
            long remainingWait = RATE_LIMIT_MS - (currentTime - _lastRequestTime);
            if (0L < remainingWait) {
                wait(remainingWait);
            }
        }

        try {
            return operation.get();
        } finally {
            _lastRequestTime = System.currentTimeMillis();
        }
    }
}
