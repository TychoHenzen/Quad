package com.quadexercise.quad.controller;

import com.quadexercise.quad.dto.AnswerDTO;
import com.quadexercise.quad.dto.AnswerResultDTO;
import com.quadexercise.quad.dto.QuestionDTO;
import com.quadexercise.quad.exceptions.QuestionNotFoundException;
import com.quadexercise.quad.exceptions.TriviaParseException;
import com.quadexercise.quad.exceptions.TriviaServiceException;
import com.quadexercise.quad.interfaces.ITriviaService;
import com.quadexercise.quad.utils.ApiConstants;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.quadexercise.quad.utils.ResponseUtils.*;

@SuppressWarnings("DuplicateStringLiteralInspection")
@RestController
public class TriviaController {
    private final ITriviaService _triviaService;

    public TriviaController(ITriviaService triviaService) {
        _triviaService = triviaService;
    }

    @GetMapping("/questions")
    public ResponseEntity<Object> getQuestions(
            @RequestParam(name = ApiConstants.PARAM_AMOUNT, defaultValue = "5") int amount) {
        try {
            List<QuestionDTO> questions = _triviaService.getQuestions(amount);
            return ResponseEntity.ok(questions);
        } catch (IllegalStateException e) {
            Thread.currentThread().interrupt();
            return createUnavailableResponse();
        } catch (TriviaParseException e) {
            return createBadGatewayResponse();
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
            return createBadRequestResponse
                    (String.format("Invalid question ID: %s", e.getQuestionId()));
        } catch (IllegalStateException e) {
            Thread.currentThread().interrupt();
            return createUnavailableResponse();
        } catch (TriviaServiceException e) {
            return createServiceErrorResponse(e.getMessage());
        } catch (RuntimeException e) {
            return createErrorResponse();
        }
    }
}
