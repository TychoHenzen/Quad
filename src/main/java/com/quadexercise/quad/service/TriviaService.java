package com.quadexercise.quad.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quadexercise.quad.constants.ApiConstants;
import com.quadexercise.quad.dto.AnswerDTO;
import com.quadexercise.quad.dto.AnswerResultDTO;
import com.quadexercise.quad.dto.QuestionDTO;
import com.quadexercise.quad.exceptions.QuestionNotFoundException;
import com.quadexercise.quad.exceptions.TriviaParseException;
import com.quadexercise.quad.utils.JsonValidator;
import com.quadexercise.quad.utils.TriviaDtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;


@SuppressWarnings("DuplicateStringLiteralInspection")
@Service
public class TriviaService {

    private final RestTemplate _restTemplate;
    private final ObjectMapper _objectMapper;
    private final MessageService _messageService;
    private long _lastRequestTime;

    // In-memory store of question IDs to correct answers
    private final Map<String, String> _questionAnswers = new HashMap<>(0);

    @Autowired
    public TriviaService(RestTemplateBuilder restTemplateBuilder, MessageService messageService) {
        _restTemplate = restTemplateBuilder.build();
        _objectMapper = new ObjectMapper();
        _messageService = messageService;
    }

    /**
     * Gets the current system time in milliseconds.
     * Extracted to facilitate testing with mocks.
     *
     * @return Current time in milliseconds
     */
    public long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }

    private String fetchTrivia(int amount) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .newInstance()
                .scheme("https")
                .host(ApiConstants.TRIVIA_API_HOST)
                .path(ApiConstants.TRIVIA_API_PATH)
                .queryParam(ApiConstants.PARAM_AMOUNT, amount);

        return _restTemplate.getForObject(
                builder.toUriString(),
                String.class
        );
    }

    public synchronized String getTrivia(int amount) {
        validateAmount(amount);
        return rateLimit(() -> fetchTrivia(amount));
    }

    private void validateAmount(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException(
                    _messageService.getMessage("error.amount.greater.than.zero"));
        }
    }

    public List<QuestionDTO> getQuestions(int amount) {
        validateAmount(amount);
        String response = rateLimit(() -> fetchTrivia(amount));
        return parseQuestionsFromResponse(response);
    }

    private List<QuestionDTO> parseQuestionsFromResponse(String response) {
        try {
            JsonNode root = _objectMapper.readTree(response);
            JsonNode results = root.get("results");
            JsonValidator.validateTriviaResponse(results);
            return StreamSupport.stream(results.spliterator(), false)
                    .map(jsonNode -> TriviaDtoMapper
                            .createQuestionDtoFromNode(jsonNode, _questionAnswers))
                    .toList();
        } catch (JsonMappingException e) {
            throw new TriviaParseException("Error mapping trivia JSON", e);
        } catch (JsonProcessingException e) {
            throw new TriviaParseException("Failed to parse trivia response", e);
        }
    }

    public List<AnswerResultDTO> checkAnswers(Collection<? extends AnswerDTO> answers) {
        return answers.stream()
                .map(this::checkAnswer)
                .toList();
    }

    AnswerResultDTO checkAnswer(AnswerDTO answerDTO) {
        String questionId = answerDTO.getQuestionId();

        // Get the correct answer
        String correctAnswer = _questionAnswers.get(questionId);
        if (correctAnswer == null) {
            throw new QuestionNotFoundException(questionId);
        }

        // Create and populate the result
        AnswerResultDTO result = new AnswerResultDTO();
        result.setQuestionId(questionId);
        result.setCorrect(Objects.equals(correctAnswer, answerDTO.getSelectedAnswer()));
        result.setCorrectAnswer(correctAnswer);

        return result;
    }

    private <T> T rateLimit(Supplier<T> operation) {
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
                            _messageService.getMessage("error.rate.limit.interrupted"), e);
                }
                throw new IllegalStateException(
                        String.format("%s (Thread wasn't interrupted)",
                                _messageService.getMessage("error.rate.limit.interrupted")), e);
            }

            remainingWait = waitEnd - System.currentTimeMillis();
        }
    }
}
