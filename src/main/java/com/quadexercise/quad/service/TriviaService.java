package com.quadexercise.quad.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.quadexercise.quad.dto.AnswerDTO;
import com.quadexercise.quad.dto.AnswerResultDTO;
import com.quadexercise.quad.dto.QuestionDTO;
import com.quadexercise.quad.exceptions.QuestionNotFoundException;
import com.quadexercise.quad.exceptions.TriviaParseException;
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
    public static final long RATE_LIMIT_MS = 5000L;

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

    private static void validateField(TreeNode node, String value) {
        if (node.get(value) == null) {
            throw new TriviaParseException(
                    String.format("Missing required field (%s) in trivia question", value));
        }
    }

    private static void validateResultNode(TreeNode node) {
        // Check for required fields existence
        validateField(node, "category");
        validateField(node, "type");
        validateField(node, "difficulty");
        validateField(node, "question");
        validateField(node, "correct_answer");
        validateField(node, "incorrect_answers");
    }

    protected List<String> shuffleAnswers(List<String> answers) {
        List<String> shuffled = new ArrayList<>(answers);
        Collections.shuffle(shuffled);
        return shuffled;
    }

    protected long getCurrentTimeMillis() {
        return System.currentTimeMillis();
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
            if (results instanceof ArrayNode) {
                return StreamSupport.stream(results.spliterator(), false)
                        .map(this::createQuestionDtoFromNode)
                        .toList();
            }
            throw new TriviaParseException("required field (results) not found in trivia response");
        } catch (JsonMappingException e) {
            throw new TriviaParseException("Error mapping trivia JSON", e);
        } catch (JsonProcessingException e) {
            throw new TriviaParseException("Failed to parse trivia response", e);
        }
    }

    private QuestionDTO createQuestionDtoFromNode(JsonNode resultNode) {

        validateResultNode(resultNode);

        QuestionDTO questionDTO = new QuestionDTO();

        // Set question properties
        questionDTO.setCategory(resultNode.get("category").asText());
        questionDTO.setType(resultNode.get("type").asText());
        questionDTO.setDifficulty(resultNode.get("difficulty").asText());
        questionDTO.setQuestion(resultNode.get("question").asText());

        // Get correct answer
        String correctAnswer = resultNode.get("correct_answer").asText();

        // Prepare all answers

        // Get incorrect answers
        JsonNode incorrectAnswersNode = resultNode.get("incorrect_answers");
        List<String> incorrectAnswers = StreamSupport
                .stream(incorrectAnswersNode.spliterator(), false)
                .map(JsonNode::asText)
                .toList();

        // Add all answers and shuffle
        List<String> allAnswers = new ArrayList<>(incorrectAnswers);
        allAnswers.add(correctAnswer);
        allAnswers = shuffleAnswers(allAnswers);


        questionDTO.setAnswers(allAnswers);

        // Store the correct answer mapped to the question ID
        _questionAnswers.put(questionDTO.getId(), correctAnswer);

        return questionDTO;
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
            long millisecondsToWait = RATE_LIMIT_MS - elapsed;

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
