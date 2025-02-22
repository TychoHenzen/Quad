
package com.quadexercise.quad.controller;

import com.quadexercise.quad.service.TriviaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TriviaController {
    private final TriviaService triviaService;

    public TriviaController(TriviaService triviaService) {
        this.triviaService = triviaService;
    }

    @GetMapping("/test")
    public ResponseEntity<String> testTrivia() {
        try {
            String response = triviaService.getTrivia(1);
            return ResponseEntity.ok(response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(503)
                    .body("{\"error\": \"Service temporarily unavailable\"}");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"Failed to fetch trivia\"}");
        }
    }
}