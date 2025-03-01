package com.quadexercise.quad.testUtils;

import com.quadexercise.quad.dto.AnswerDTO;
import com.quadexercise.quad.dto.AnswerResultDTO;
import com.quadexercise.quad.dto.QuestionDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.quadexercise.quad.testUtils.TestConstants.*;

/**
 * Factory class for creating test data objects.
 * Centralizes test data creation to improve consistency and reduce duplication.
 */
@SuppressWarnings("DuplicateStringLiteralInspection")
public enum TestDataFactory {
    ;

    /**
     * Creates a standardized list of mock questions for testing.
     *
     * @return List of QuestionDTO objects for testing
     */
    public static List<QuestionDTO> createMockQuestions() {
        QuestionDTO question1 = new QuestionDTO();
        question1.setCategory(TEST_CATEGORY);
        question1.setQuestion(TEST_QUESTION);
        question1.setDifficulty(TEST_DIFFICULTY);
        List<String> answersStrings = TEST_INCORRECT_ANSWERS();
        answersStrings.add(TEST_CORRECT_ANSWER);
        question1.setAnswers(answersStrings);

        QuestionDTO question2 = new QuestionDTO();
        question2.setCategory("History");
        question2.setQuestion("Who was the first president of the United States?");
        question2.setDifficulty("medium");
        question2.setAnswers(Arrays.asList("George Washington", "Thomas Jefferson", "Abraham Lincoln", "John Adams"));

        return Arrays.asList(question1, question2);
    }

    /**
     * Creates a standardized list of mock answers for testing.
     *
     * @return List of AnswerDTO objects for testing
     */
    public static List<AnswerDTO> createTestAnswers() {
        List<AnswerDTO> answers = new ArrayList<>(0);
        AnswerDTO answer = new AnswerDTO();
        answer.setQuestionId("q1");
        answer.setSelectedAnswer("Paris");
        answers.add(answer);
        return answers;
    }

    /**
     * Creates a standardized list of mock answer results for testing.
     *
     * @return List of AnswerResultDTO objects for testing
     */
    public static List<AnswerResultDTO> createTestResults() {
        List<AnswerResultDTO> results = new ArrayList<>(0);
        AnswerResultDTO result = new AnswerResultDTO();
        result.setQuestionId("q1");
        result.setCorrect(true);
        result.setCorrectAnswer("Paris");
        results.add(result);
        return results;
    }
}