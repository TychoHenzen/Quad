package com.quadexercise.quad.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class TriviaService {
    private final RestTemplate restTemplate;

    private static final long RATE_LIMIT_MS = 5000; 
    private long lastRequestTime = 0;

    @Autowired
    public TriviaService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }
    public synchronized String getTrivia(int amount) throws InterruptedException {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        while (System.currentTimeMillis() - lastRequestTime < RATE_LIMIT_MS) {
            long remainingWait = RATE_LIMIT_MS - (System.currentTimeMillis() - lastRequestTime);
            if (remainingWait > 0) {
                wait(remainingWait);
            }
        }

        try {
            UriComponentsBuilder builder = UriComponentsBuilder
                    .newInstance()
                    .scheme("https")
                    .host("opentdb.com")
                    .path("/api.php")
                    .queryParam("amount", amount);

            return restTemplate.getForObject(
                    builder.toUriString(),
                    String.class
            );
        } finally {
            lastRequestTime = System.currentTimeMillis();
        }
    }
}