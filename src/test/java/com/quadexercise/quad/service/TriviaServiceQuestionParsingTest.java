package com.quadexercise.quad.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quadexercise.quad.dto.QuestionDTO;
import com.quadexercise.quad.exceptions.TriviaParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static com.quadexercise.quad.testUtils.TestConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@SuppressWarnings("DuplicateStringLiteralInspection")
@ExtendWith(MockitoExtension.class)
class TriviaServiceQuestionParsingTest {

    @Mock
    private RestTemplate _restTemplate;

    @Mock
    private RestTemplateBuilder _restTemplateBuilder;

    @Mock
    private MessageService _messageService;

    private TriviaService _triviaService;
    private ObjectMapper _objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        when(_restTemplateBuilder.build()).thenReturn(_restTemplate);
        _triviaService = new TriviaService(_restTemplateBuilder, _messageService);
    }

    @Test
    void testGetQuestionsFromValidJson_ShouldReturnParsedQuestions() {
        // Arrange
        when(_restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(VALID_QUESTION_JSON);

        // Act
        List<QuestionDTO> result = _triviaService.getQuestions(1);

        // Assert
        assertEquals(1, result.size());
        QuestionDTO question = result.get(0);
        assertEquals(TEST_CATEGORY, question.getCategory());
        assertEquals(TEST_TYPE, question.getType());
        assertEquals(TEST_DIFFICULTY, question.getDifficulty());
        assertEquals(TEST_QUESTION, question.getQuestion());
        assertEquals(4, question.getAnswers().size());
        assertTrue(question.getAnswers().contains(TEST_CORRECT_ANSWER));
        for (String answer : TEST_INCORRECT_ANSWERS()) {
            assertTrue(question.getAnswers().contains(answer));
        }
    }

    @Test
    void testParseInvalidJson_ShouldThrowTriviaParseException() {
        // Arrange
        String invalidJson = "{invalid-json";
        when(_restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(invalidJson);

        // Act & Assert
        assertThrows(TriviaParseException.class, () -> _triviaService.getQuestions(1));
    }

    @Test
    void testParseMalformedResultObject_ShouldThrowTriviaParseException() {
        // Arrange
        String invalidResponse = "{\"response_code\":0,\"results\":[" +
                "\"This is not a valid result object\"" +
                "]}";

        when(_restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(invalidResponse);

        // Act & Assert
        assertThrows(TriviaParseException.class, () -> _triviaService.getQuestions(1));
    }

    @Test
    void testParseNonArrayResults_ShouldThrowTriviaParseException() {
        // Arrange
        String invalidJson = "{\"response_code\":0,\"results\":\"not-an-array\"}";

        // Use lenient stubbing for both mocks
        lenient().when(_messageService.getMessage(anyString())).thenReturn("Test message");
        lenient().when(_restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(invalidJson);

        // Act & Assert
        assertThrows(TriviaParseException.class, () -> _triviaService.getQuestions(1));
    }

    @Test
    void testParseMissingResultsField_ShouldThrowTriviaParseException() {
        // Arrange
        String invalidJson = "{\"response_code\":0}"; // Missing results field
        when(_restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(invalidJson);

        // Act & Assert
        assertThrows(TriviaParseException.class, () -> _triviaService.getQuestions(1));
    }

    @ParameterizedTest
    @ValueSource(strings = {"category", "type", "difficulty", "question", "correct_answer", "incorrect_answers"})
    void testValidateMissingRequiredFields_ShouldThrowTriviaParseException
            (String fieldToRemove) throws JsonProcessingException {
        // Arrange

        // Parse the base JSON and modify to remove specified field
        JsonNode rootNode = _objectMapper.readTree(VALID_QUESTION_JSON);
        JsonNode resultNode = rootNode.get("results").get(0);
        ((com.fasterxml.jackson.databind.node.ObjectNode) resultNode).remove(fieldToRemove);

        // Convert back to JSON string
        String modifiedJson = _objectMapper.writeValueAsString(rootNode);

        when(_restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(modifiedJson);

        // Act & Assert
        TriviaParseException exception = assertThrows(TriviaParseException.class,
                () -> _triviaService.getQuestions(1));

        // Verify the exception message mentions the correct field
        assertTrue(exception.getMessage().contains(fieldToRemove),
                "Exception message should mention the missing field: " + fieldToRemove);
    }
}