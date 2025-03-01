package com.quadexercise.quad.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quadexercise.quad.dto.QuestionDTO;
import com.quadexercise.quad.exceptions.TriviaParseException;
import com.quadexercise.quad.utils.JsonValidator;
import com.quadexercise.quad.utils.TriviaDtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

/**
 * Service responsible for parsing trivia JSON responses into data objects.
 * Handles validation and conversion of API responses.
 */
@Service
public class TriviaParsingService {

    private final ObjectMapper _objectMapper;

    @Autowired
    public TriviaParsingService(ObjectMapper objectMapper) {
        _objectMapper = objectMapper;
    }

    /**
     * Parses a JSON response string into a list of QuestionDTO objects.
     *
     * @param response        The JSON response string from the trivia API
     * @param questionAnswers Map to store question ID to correct answer mappings
     * @return List of parsed QuestionDTO objects
     * @throws TriviaParseException if parsing fails or the response is invalid
     */
    List<QuestionDTO> parseQuestionsFromResponse(
            String response,
            Map<? super String, ? super String> questionAnswers) {
        try {
            JsonNode root = _objectMapper.readTree(response);
            JsonValidator.validateTriviaResponse(root);
            JsonNode results = root.get("results");
            return StreamSupport.stream(results.spliterator(), false)
                    .map(jsonNode -> TriviaDtoMapper
                            .createQuestionDtoFromNode(jsonNode, questionAnswers))
                    .toList();
        } catch (JsonMappingException e) {
            throw new TriviaParseException("Error mapping trivia JSON", e);
        } catch (JsonProcessingException e) {
            throw new TriviaParseException("Failed to parse trivia response", e);
        }
    }
}
