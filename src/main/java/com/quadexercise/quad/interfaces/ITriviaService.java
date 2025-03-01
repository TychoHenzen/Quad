package com.quadexercise.quad.interfaces;

import com.quadexercise.quad.dto.AnswerDTO;
import com.quadexercise.quad.dto.AnswerResultDTO;
import com.quadexercise.quad.dto.QuestionDTO;

import java.util.Collection;
import java.util.List;

/**
 * Service interface for trivia-related operations.
 * Defines the contract for fetching trivia questions and checking answers.
 */
public interface ITriviaService {

    /**
     * Gets raw trivia data in JSON format.
     *
     * @param amount The number of trivia questions to retrieve
     * @return JSON response as a string
     * @throws IllegalArgumentException if amount is <= 0
     */
    String getTrivia(int amount);

    /**
     * Gets parsed trivia questions.
     *
     * @param amount The number of trivia questions to retrieve
     * @return List of QuestionDTO objects
     * @throws IllegalArgumentException if amount is <= 0
     */
    List<QuestionDTO> getQuestions(int amount);

    /**
     * Checks a collection of user answers against stored correct answers.
     *
     * @param answers Collection of user answers to check
     * @return List of results indicating if each answer was correct
     */
    List<AnswerResultDTO> checkAnswers(Collection<? extends AnswerDTO> answers);

    /**
     * Checks a single user answer against the stored correct answer.
     *
     * @param answerDTO The user's answer to check
     * @return Result indicating if the answer was correct
     */
    AnswerResultDTO checkAnswer(AnswerDTO answerDTO);
}
