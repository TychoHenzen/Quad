package com.quadexercise.quad.service;

import com.quadexercise.quad.dto.AnswerDTO;
import com.quadexercise.quad.dto.AnswerResultDTO;
import com.quadexercise.quad.dto.QuestionDTO;
import com.quadexercise.quad.interfaces.ITriviaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Primary implementation of the TriviaService interface.
 * Coordinates the specialized services to provide the complete trivia functionality.
 */
@Service
@Primary
public class TriviaServiceImpl implements ITriviaService {

    private final TriviaFetchService _fetchService;
    private final TriviaParsingService _parsingService;
    private final TriviaAnswerService _answerService;
    private final TriviaRateLimitService _rateLimitService;
    private final MessageService _messageService;

    @Autowired
    public TriviaServiceImpl(
            RestTemplateBuilder restTemplateBuilder,
            MessageService messageService) {

        // Create RestTemplate
        RestTemplate restTemplate = restTemplateBuilder.build();

        // Initialize services
        _fetchService = new TriviaFetchService(restTemplate);
        _parsingService = new TriviaParsingService(
                new com.fasterxml.jackson.databind.ObjectMapper());
        _answerService = new TriviaAnswerService();
        _rateLimitService = new TriviaRateLimitService(messageService);
        _messageService = messageService;
    }

    /**
     * For testing purposes - allows injection of the services
     */
    TriviaServiceImpl(
            TriviaFetchService fetchService,
            TriviaParsingService parsingService,
            TriviaAnswerService answerService,
            TriviaRateLimitService rateLimitService,
            MessageService messageService) {

        _fetchService = fetchService;
        _parsingService = parsingService;
        _answerService = answerService;
        _rateLimitService = rateLimitService;
        _messageService = messageService;
    }

    @Override
    public String getTrivia(int amount) {
        validateAmount(amount);
        return _rateLimitService.rateLimit(() -> _fetchService.fetchTrivia(amount));
    }

    @Override
    public List<QuestionDTO> getQuestions(int amount) {
        validateAmount(amount);
        String response = _rateLimitService.rateLimit(() ->
                _fetchService.fetchTrivia(amount));

        // Use an internal map for parsing that will be synchronized with the answer service
        Map<String, String> questionAnswersMap = new HashMap<>(0);
        List<QuestionDTO> questions = _parsingService
                .parseQuestionsFromResponse(response, questionAnswersMap);

        // Update the answer service with the new questions
        questionAnswersMap.forEach(_answerService::addQuestionAnswer);

        return questions;
    }

    @Override
    public List<AnswerResultDTO> checkAnswers(Collection<? extends AnswerDTO> answers) {
        return _answerService.checkAnswers(answers);
    }

    @Override
    public AnswerResultDTO checkAnswer(AnswerDTO answerDTO) {
        return _answerService.checkAnswer(answerDTO);
    }

    private void validateAmount(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException(
                    _messageService.getMessage("error.amount.greater.than.zero"));
        }
    }
}
