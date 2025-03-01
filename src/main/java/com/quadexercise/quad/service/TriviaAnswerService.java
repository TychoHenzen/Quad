package com.quadexercise.quad.service;

import com.quadexercise.quad.dto.AnswerDTO;
import com.quadexercise.quad.dto.AnswerResultDTO;
import com.quadexercise.quad.exceptions.QuestionNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service responsible for checking trivia answers.
 * Manages the mapping between question IDs and their correct answers.
 */
@Service
public class TriviaAnswerService {

    // In-memory store of question IDs to correct answers
    private final Map<String, String> _questionAnswers = new HashMap<>(0);


    /**
     * Adds a mapping from question ID to correct answer.
     *
     * @param questionId    The ID of the question
     * @param correctAnswer The correct answer for the question
     */
    void addQuestionAnswer(String questionId, String correctAnswer) {
        _questionAnswers.put(questionId, correctAnswer);
    }

    /**
     * Checks multiple user answers against stored correct answers.
     *
     * @param answers Collection of user answers to check
     * @return List of results indicating if each answer was correct
     */
    List<AnswerResultDTO> checkAnswers(Collection<? extends AnswerDTO> answers) {
        return answers.stream()
                .map(this::checkAnswer)
                .toList();
    }

    /**
     * Checks a single user answer against the stored correct answer.
     *
     * @param answerDTO The user's answer to check
     * @return Result indicating if the answer was correct
     * @throws QuestionNotFoundException if the question ID is not found
     */
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
}
