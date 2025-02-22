package com.quadexercise.quad.controller;

import com.quadexercise.quad.enums.Errors;
import com.quadexercise.quad.service.TriviaService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TriviaController {
    private final TriviaService _triviaService;

    public TriviaController(TriviaService triviaService) {
        _triviaService = triviaService;
    }

    private static ResponseEntity<String> createUnavailableResponse() {
        return ResponseEntity.status(Errors.ERR_UNAVAILABLE)
                .body("{\"error\": \"Service temporarily unavailable\"}");
    }

    private static ResponseEntity<String> createErrorResponse() {
        return ResponseEntity.internalServerError()
                .body("{\"error\": \"Failed to fetch trivia\"}");
    }

    @GetMapping(value = "/test", produces = MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8")
    public ResponseEntity<String> testTrivia() {
        try {
            String response = _triviaService.getTrivia(1);
            return ResponseEntity.ok(response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return createUnavailableResponse();
        } catch (RuntimeException e) {
            return createErrorResponse();
        }
    }
}
