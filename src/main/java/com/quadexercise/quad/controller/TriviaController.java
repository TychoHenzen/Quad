package com.quadexercise.quad.controller;

import com.quadexercise.quad.dto.AnswerDTO;
import com.quadexercise.quad.dto.AnswerResultDTO;
import com.quadexercise.quad.dto.QuestionDTO;
import com.quadexercise.quad.enums.Errors;
import com.quadexercise.quad.exceptions.QuestionNotFoundException;
import com.quadexercise.quad.exceptions.TriviaParseException;
import com.quadexercise.quad.exceptions.TriviaServiceException;
import com.quadexercise.quad.service.TriviaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SuppressWarnings("DuplicateStringLiteralInspection")
@RestController
public class TriviaController {
    private final TriviaService _triviaService;

    public TriviaController(TriviaService triviaService) {
        _triviaService = triviaService;
    }

    private static ResponseEntity<Object> createUnavailableResponse() {
        return ResponseEntity.status(Errors.ERR_UNAVAILABLE)
                .body("{\"error\": \"Service temporarily unavailable\"}");
    }

    private static ResponseEntity<Object> createErrorResponse() {
        return ResponseEntity.internalServerError()
                .body("{\"error\": \"Failed to fetch trivia\"}");
    }

    @GetMapping(value = "/test", produces = MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8")
    public ResponseEntity<Object> testTrivia() {
        try {
            String response = _triviaService.getTrivia(1);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            Thread.currentThread().interrupt();
            return createUnavailableResponse();
        } catch (RuntimeException e) {
            return createErrorResponse();
        }
    }

    @GetMapping("/questions")
    public ResponseEntity<Object> getQuestions(
            @RequestParam(name = "amount", defaultValue = "5") int amount) {
        try {
            List<QuestionDTO> questions = _triviaService.getQuestions(amount);
            return ResponseEntity.ok(questions);
        } catch (IllegalStateException e) {
            Thread.currentThread().interrupt();
            return createUnavailableResponse();
        } catch (TriviaParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("{\"error\": \"Error parsing trivia data from provider\"}");
        } catch (RuntimeException e) {
            return createErrorResponse();
        }
    }

    @PostMapping("/checkanswers")
    public ResponseEntity<Object> checkAnswers(@RequestBody List<? extends AnswerDTO> answers) {
        try {
            List<AnswerResultDTO> results = _triviaService.checkAnswers(answers);
            return ResponseEntity.ok(results);
        } catch (QuestionNotFoundException e) {
            String errorMsg = String.format(
                    "{\"error\": \"Invalid question ID: %s\"}", e.getQuestionId());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMsg);
        } catch (IllegalStateException e) {
            Thread.currentThread().interrupt();
            return createUnavailableResponse();
        } catch (TriviaServiceException e) {
            String errorMsg = String.format("{\"error\": \"%s\"}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMsg);
        } catch (RuntimeException e) {
            return createErrorResponse();
        }
    }
}
