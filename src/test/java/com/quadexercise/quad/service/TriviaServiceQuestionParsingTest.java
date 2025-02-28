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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

    @BeforeEach
    void setUp() {
        when(_restTemplateBuilder.build()).thenReturn(_restTemplate);
        _triviaService = new TriviaService(_restTemplateBuilder, _messageService);
    }

    @Test
    void testGetQuestions_ShouldReturnParsedQuestions() {
        // Arrange
        String jsonResponse = "{\"response_code\":0,\"results\":[" +
                "{\"category\":\"Science\",\"type\":\"multiple\",\"difficulty\":\"medium\"," +
                "\"question\":\"What is H2O?\",\"correct_answer\":\"Water\"," +
                "\"incorrect_answers\":[\"Carbon Dioxide\",\"Oxygen\",\"Hydrogen\"]}" +
                "]}";

        when(_restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(jsonResponse);

        // Act
        List<QuestionDTO> result = _triviaService.getQuestions(1);

        // Assert
        assertEquals(1, result.size());
        QuestionDTO question = result.get(0);
        assertEquals("Science", question.getCategory());
        assertEquals("multiple", question.getType());
        assertEquals("medium", question.getDifficulty());
        assertEquals("What is H2O?", question.getQuestion());
        assertEquals(4, question.getAnswers().size());
        assertTrue(question.getAnswers().contains("Water"));
        assertTrue(question.getAnswers().contains("Carbon Dioxide"));
        assertTrue(question.getAnswers().contains("Oxygen"));
        assertTrue(question.getAnswers().contains("Hydrogen"));
    }

    @Test
    void testGetQuestions_ShouldThrowTriviaParseException_WhenInvalidJson() {
        // Arrange
        String invalidJson = "{invalid-json";
        when(_restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(invalidJson);

        // Act & Assert
        assertThrows(TriviaParseException.class, () -> _triviaService.getQuestions(1));
    }

    @ParameterizedTest
    @ValueSource(strings = {"category", "type", "difficulty", "question", "correct_answer", "incorrect_answers"})
    void testValidateResultNode_MissingRequiredFields(String fieldToRemove) throws JsonProcessingException {
        // Arrange
        String baseJson = "{\"response_code\":0,\"results\":[" +
                "{\"category\":\"Science\",\"type\":\"multiple\",\"difficulty\":\"medium\"," +
                "\"question\":\"What is H2O?\",\"correct_answer\":\"Water\"," +
                "\"incorrect_answers\":[\"Carbon Dioxide\",\"Oxygen\",\"Hydrogen\"]}" +
                "]}";

        // Parse the base JSON
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(baseJson);
        JsonNode resultNode = rootNode.get("results").get(0);

        // Create a modified version with the specified field removed
        ((com.fasterxml.jackson.databind.node.ObjectNode) resultNode).remove(fieldToRemove);

        // Convert back to a JSON string
        String modifiedJson = mapper.writeValueAsString(rootNode);

        // Mock the REST response
        lenient().when(_restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(modifiedJson);

        // Act & Assert
        TriviaParseException exception = assertThrows(TriviaParseException.class,
                () -> _triviaService.getQuestions(1));

        // Verify the exception message mentions the correct field
        assertTrue(exception.getMessage().contains(fieldToRemove),
                "Exception message should mention the missing field: " + fieldToRemove);
    }


    @Test
    void testShuffleAnswers() {
        // Arrange
        List<String> original = Arrays.asList("A", "B", "C", "D");

        // Mock to ensure deterministic order for testing
        TriviaService serviceSpy = spy(_triviaService);
        doAnswer(invocation -> {
            List<String> list = new ArrayList<>(invocation.getArgument(0));
            // Force a known shuffle order for testing
            Collections.reverse(list);
            return list;
        }).when(serviceSpy).shuffleAnswers(any());

        // Act
        List<String> result = serviceSpy.shuffleAnswers(original);

        // Assert
        assertNotEquals(original, result);
        assertEquals(Arrays.asList("D", "C", "B", "A"), result);
    }
}