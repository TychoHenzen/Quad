package com.quadexercise.quad.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.quadexercise.quad.dto.QuestionDTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

/**
 * Utility class for mapping API data to DTOs.
 * Centralizes DTO creation logic and improves separation of concerns.
 */
@SuppressWarnings("DuplicateStringLiteralInspection")
public enum TriviaDtoMapper {
    ;

    /**
     * Creates a QuestionDTO from a JsonNode representing a trivia question.
     *
     * @param resultNode      The JsonNode containing question data
     * @param questionAnswers Map to store question ID to correct answer mappings
     * @return A fully populated QuestionDTO
     */
    public static QuestionDTO createQuestionDtoFromNode
    (JsonNode resultNode, Map<? super String, ? super String> questionAnswers) {
        JsonValidator.validateResultNode(resultNode);

        QuestionDTO questionDTO = new QuestionDTO();

        // Set question properties
        questionDTO.setCategory(resultNode.get("category").asText());
        questionDTO.setType(resultNode.get("type").asText());
        questionDTO.setDifficulty(resultNode.get("difficulty").asText());
        questionDTO.setQuestion(resultNode.get("question").asText());

        // Get correct answer and prepare all answers
        String correctAnswer = resultNode.get("correct_answer").asText();

        // Get incorrect answers
        List<String> incorrectAnswers = getIncorrectAnswersFromNode(resultNode);

        // Add all answers and shuffle
        List<String> allAnswers = new ArrayList<>(incorrectAnswers);
        allAnswers.add(correctAnswer);
        allAnswers = shuffleAnswers(allAnswers);
        questionDTO.setAnswers(allAnswers);

        // Store the correct answer mapped to the question ID
        questionAnswers.put(questionDTO.getId(), correctAnswer);

        return questionDTO;
    }

    /**
     * Extracts the list of incorrect answers from a question JsonNode.
     *
     * @param resultNode The JsonNode containing question data
     * @return List of incorrect answers
     */
    private static List<String> getIncorrectAnswersFromNode(JsonNode resultNode) {
        JsonNode incorrectAnswersNode = resultNode.get("incorrect_answers");
        return StreamSupport
                .stream(incorrectAnswersNode.spliterator(), false)
                .map(JsonNode::asText)
                .toList();
    }

    /**
     * Shuffles a list of answers to randomize their order.
     *
     * @param answers List of answers to shuffle
     * @return New list with shuffled answers
     */
    private static List<String> shuffleAnswers(List<String> answers) {
        List<String> shuffled = new ArrayList<>(answers);
        Collections.shuffle(shuffled);
        return shuffled;
    }
}
