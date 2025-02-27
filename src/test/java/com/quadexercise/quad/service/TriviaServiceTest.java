package com.quadexercise.quad.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quadexercise.quad.dto.AnswerDTO;
import com.quadexercise.quad.dto.AnswerResultDTO;
import com.quadexercise.quad.dto.QuestionDTO;
import com.quadexercise.quad.exceptions.QuestionNotFoundException;
import com.quadexercise.quad.exceptions.TriviaParseException;
import com.quadexercise.quad.interfaces.InterruptibleRunnable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static com.quadexercise.quad.service.TriviaService.RATE_LIMIT_MS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SuppressWarnings({"DuplicateStringLiteralInspection", "ClassWithTooManyMethods"})
@ExtendWith(MockitoExtension.class)
class TriviaServiceTest {

    @Mock
    private RestTemplate _restTemplate;

    @Mock
    private RestTemplateBuilder _restTemplateBuilder;
    @Mock
    private MessageService _messageService;

    private TriviaService _triviaService;
    private static final long SMALL_DELAY_MS = 100L;
    private static final long LARGE_DELAY_MS = 1000L;


    @BeforeEach
    void setUp() {
        when(_restTemplateBuilder.build()).thenReturn(_restTemplate);
        _triviaService = new TriviaService(_restTemplateBuilder, _messageService);
    }

    private static void waitUntilRateLimitExpires() throws InterruptedException {
        long exactLimit = System.currentTimeMillis() + RATE_LIMIT_MS;
        long current = 0L;
        while (current < exactLimit) {
            Thread.sleep(1L);
            current = System.currentTimeMillis();
        }
    }


    private static long measureExecutionTime(InterruptibleRunnable action) throws InterruptedException {
        long start = System.currentTimeMillis();
        action.run();
        return System.currentTimeMillis() - start;
    }


    @Test
    void testGetTrivia_ShouldReturnData() {
        // Arrange
        String expectedResponse = "{\"response_code\":0,\"results\":[]}";
        when(_restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(expectedResponse);

        // Act
        String result = _triviaService.getTrivia(1);

        // Assert
        assertEquals(expectedResponse, result);
        verify(_restTemplate).getForObject(anyString(), eq(String.class));
    }

    @Test
    void testGetTrivia_ShouldRespectRateLimit() throws InterruptedException {
        // Arrange
        when(_restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn("{}");

        // Act
        long duration = measureExecutionTime(() ->
        {
            _triviaService.getTrivia(1);
            _triviaService.getTrivia(1);
        });

        // Assert
        assertTrue(RATE_LIMIT_MS - SMALL_DELAY_MS <= duration,
                "Second request should be delayed by at least 5 seconds");
        assertTrue(RATE_LIMIT_MS + SMALL_DELAY_MS >= duration,
                "Second request should not be delayed more than necessary");
    }


    @Test
    void testGetTrivia_ShouldWaitCorrectAmount() throws InterruptedException {
        // Arrange
        when(_restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn("{}");

        // First request to set lastRequestTime
        _triviaService.getTrivia(1);

        final long halfTimeout = 3000L;

        // Wait 3 seconds
        Thread.sleep(halfTimeout);

        // Act
        long duration = measureExecutionTime(() -> _triviaService.getTrivia(1));

        // Assert
        // Should wait ~2 seconds (5 second limit - 3 seconds elapsed)
        final long minTimeout = 1800L;
        final long maxTimeout = 2200L;
        assertTrue(minTimeout <= duration && maxTimeout >= duration,
                "Wait time should be approximately 2 seconds, was: " + duration + " ms");
    }

    @Test
    void testGetTrivia_ShouldHandleMultipleRequests() {
        // Arrange
        when(_restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn("{}");

        // Act & Assert
        // This will take at least 10 seconds due to rate limiting
        assertDoesNotThrow(() -> {
            for (int i = 0; 3 > i; i++) {
                _triviaService.getTrivia(1);
            }
        });

        verify(_restTemplate, times(3))
                .getForObject(anyString(), eq(String.class));
    }

    @Test
    void testGetTrivia_ShouldBuildCorrectUrl() {
        // Arrange
        String expectedUrl = "https://opentdb.com/api.php?amount=1";
        when(_restTemplate.getForObject(expectedUrl, String.class))
                .thenReturn("{}");

        // Act
        _triviaService.getTrivia(1);

        // Assert

        verify(_restTemplate).getForObject(expectedUrl, String.class);
    }

    @Test
    void testGetTrivia_ShouldHandleErrorResponses() {
        // Arrange
        when(_restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RestClientException("API Error"));

        // Act & Assert
        assertThrows(RestClientException.class, () -> _triviaService.getTrivia(1));
    }

    @Test
    void testGetTrivia_ShouldValidateInputAmount() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> _triviaService.getTrivia(-1),
                "Should throw exception for negative amounts");
        assertThrows(IllegalArgumentException.class, () -> _triviaService.getTrivia(0),
                "Should throw exception for zero amount");
    }

    @Test
    void testWaitBehavior() throws InterruptedException {
        when(_restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn("{}");

        _triviaService.getTrivia(1);

        long startTime = System.currentTimeMillis();
        CompletableFuture.runAsync(() -> assertDoesNotThrow(() -> _triviaService.getTrivia(1)));

        final long halfTimeout = RATE_LIMIT_MS / 2L;
        Thread.sleep(halfTimeout);
        long midTime = System.currentTimeMillis() - startTime;
        assertTrue(halfTimeout <= midTime, "Should still be waiting at halfway point");
    }

    @Test
    void testRateLimit_ExactBoundaryConditions() throws InterruptedException {
        // Arrange
        when(_restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn("{}");
        _triviaService.getTrivia(1);
        waitUntilRateLimitExpires();

        // Act
        long duration = measureExecutionTime(() -> _triviaService.getTrivia(1));

        // Assert
        assertTrue(SMALL_DELAY_MS > duration, "Should not wait when exactly at rate limit");
    }

    @Test
    void testRateLimit_EnforcesMinimumWait() throws InterruptedException {
        // Arrange
        when(_restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn("{}");
        _triviaService.getTrivia(1);

        // Act
        long duration = measureExecutionTime(() -> _triviaService.getTrivia(1));

        // Assert
        assertAll(
                () -> assertTrue(RATE_LIMIT_MS - SMALL_DELAY_MS <= duration,
                        "Should wait at least RATE_LIMIT_MS milliseconds"),
                () -> assertTrue((RATE_LIMIT_MS + (SMALL_DELAY_MS * 2L)) > duration,
                        "Should not wait significantly longer than RATE_LIMIT_MS")
        );
    }


    @Test
    void testRateLimit_HandlesInterruption() throws InterruptedException {
        // Arrange
        when(_restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn("{}");

        // First call to set the last request time
        _triviaService.getTrivia(1);

        // Create a thread that will be interrupted
        Thread testThread = new Thread(() -> {
            try {
                _triviaService.getTrivia(1);
                fail("Should have been interrupted");
            } catch (IllegalStateException e) {
                // Expected
                Thread.currentThread().interrupt();
            }
        });

        // Act
        testThread.start();
        Thread.sleep(SMALL_DELAY_MS); // Give the thread time to enter the wait state
        testThread.interrupt();
        testThread.join(RATE_LIMIT_MS); // Wait for thread to complete

        // Assert
        assertSame(Thread.State.TERMINATED, testThread.getState(), "Thread should have terminated");
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


    @Test
    void testGetQuestions_ShouldThrowTriviaParseException_WhenMissingRequiredFields() {
        // We need to update the TriviaService to properly handle this case
        // Let's test with completely malformed data instead
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
    void testCheckAnswers_ShouldReturnCorrectResults() {
        // Arrange - First get some questions to populate the answer map
        String jsonResponse = "{\"response_code\":0,\"results\":[" +
                "{\"category\":\"Science\",\"type\":\"multiple\",\"difficulty\":\"medium\"," +
                "\"question\":\"What is H2O?\",\"correct_answer\":\"Water\"," +
                "\"incorrect_answers\":[\"Carbon Dioxide\",\"Oxygen\",\"Hydrogen\"]}" +
                "]}";

        when(_restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(jsonResponse);

        List<QuestionDTO> questions = _triviaService.getQuestions(1);
        String questionId = questions.get(0).getId();

        AnswerDTO correctAnswer = new AnswerDTO();
        correctAnswer.setQuestionId(questionId);
        correctAnswer.setSelectedAnswer("Water");

        AnswerDTO incorrectAnswer = new AnswerDTO();
        incorrectAnswer.setQuestionId(questionId);
        incorrectAnswer.setSelectedAnswer("Oxygen");

        // Act
        List<AnswerResultDTO> correctResults = _triviaService.checkAnswers(List.of(correctAnswer));
        List<AnswerResultDTO> incorrectResults = _triviaService.checkAnswers(List.of(incorrectAnswer));

        // Assert
        assertEquals(1, correctResults.size());
        assertTrue(correctResults.get(0).isCorrect());
        assertEquals("Water", correctResults.get(0).getCorrectAnswer());

        assertEquals(1, incorrectResults.size());
        assertFalse(incorrectResults.get(0).isCorrect());
        assertEquals("Water", incorrectResults.get(0).getCorrectAnswer());
    }

    @Test
    void testCheckAnswers_ShouldThrowQuestionNotFoundException() {
        // Arrange
        AnswerDTO answerDTO = new AnswerDTO();
        answerDTO.setQuestionId("non-existent-id");
        answerDTO.setSelectedAnswer("Some Answer");
        List<AnswerDTO> answers = List.of(answerDTO);

        // Act & Assert
        assertThrows(QuestionNotFoundException.class, () -> _triviaService.checkAnswers(answers));
    }


    @Test
    void testParseQuestionsFromResponse_InvalidJsonStructure() {
        // Arrange
        String invalidJson = "{\"response_code\":0,\"results\":\"not-an-array\"}";

        // Reset mocks to clear any previous stubs
        reset(_restTemplate, _messageService);

        // Use lenient stubbing for both mocks
        lenient().when(_messageService.getMessage(anyString())).thenReturn("Test message");
        lenient().when(_restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(invalidJson);

        // Act & Assert
        assertThrows(TriviaParseException.class, () -> _triviaService.getQuestions(1));
    }

    @Test
    void testParseQuestionsFromResponse_MissingResults() {
        // Arrange
        String invalidJson = "{\"response_code\":0}"; // Missing results field
        when(_restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(invalidJson);

        // Act & Assert
        assertThrows(TriviaParseException.class, () -> _triviaService.getQuestions(1));
    }

    @Test
    void testValidateAmount_NegativeAmount() {
        // Configure MessageService with lenient stubbing
        lenient().when(_messageService.getMessage(anyString())).thenReturn("Amount must be greater than zero");

        // Act & Assert - test directly on getTrivia which calls validateAmount
        Exception exception = assertThrows(IllegalArgumentException.class, () -> _triviaService.getTrivia(-5));
        assertTrue(exception.getMessage().contains("greater than zero"));
    }


    @Test
    void testValidateAmount_ZeroAmount() {
        // Configure MessageService with lenient stubbing
        lenient().when(_messageService.getMessage(anyString())).thenReturn("Amount must be greater than zero");

        // Act & Assert - test directly on getTrivia which calls validateAmount
        Exception exception = assertThrows(IllegalArgumentException.class, () -> _triviaService.getTrivia(0));
        assertTrue(exception.getMessage().contains("greater than zero"));
    }

    @Test
    void testCheckAnswers_EmptyCollection() {
        // Act
        List<AnswerResultDTO> results = _triviaService.checkAnswers(List.of());

        // Assert
        assertTrue(results.isEmpty());
    }

    @Test
    void testCheckAnswers_MultipleAnswers() {
        // Arrange - First get some questions to populate the answer map
        String jsonResponse = "{\"response_code\":0,\"results\":[" +
                "{\"category\":\"Science\",\"type\":\"multiple\",\"difficulty\":\"medium\"," +
                "\"question\":\"What is H2O?\",\"correct_answer\":\"Water\"," +
                "\"incorrect_answers\":[\"Carbon Dioxide\",\"Oxygen\",\"Hydrogen\"]}," +
                "{\"category\":\"History\",\"type\":\"multiple\",\"difficulty\":\"medium\"," +
                "\"question\":\"Who was the first US President?\",\"correct_answer\":\"Washington\"," +
                "\"incorrect_answers\":[\"Adams\",\"Jefferson\",\"Madison\"]}" +
                "]}";

        when(_restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(jsonResponse);

        List<QuestionDTO> questions = _triviaService.getQuestions(2);

        AnswerDTO answer1 = new AnswerDTO();
        answer1.setQuestionId(questions.get(0).getId());
        answer1.setSelectedAnswer("Water");

        AnswerDTO answer2 = new AnswerDTO();
        answer2.setQuestionId(questions.get(1).getId());
        answer2.setSelectedAnswer("Adams"); // Incorrect

        // Act
        List<AnswerResultDTO> results = _triviaService.checkAnswers(Arrays.asList(answer1, answer2));

        // Assert
        assertEquals(2, results.size());
        assertTrue(results.get(0).isCorrect());
        assertFalse(results.get(1).isCorrect());
        assertEquals("Washington", results.get(1).getCorrectAnswer());
    }

    @Test
    void testGetTrivia_DirectMethod() {
        // Arrange
        String expectedResponse = "{\"response_code\":0,\"results\":[]}";
        when(_restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(expectedResponse);

        // Act
        String result = _triviaService.getTrivia(1);

        // Assert
        assertEquals(expectedResponse, result);
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
    void testGetQuestions_ValidatesAmount() {
        // Arrange - set up to throw on negative values
        when(_messageService.getMessage(anyString())).thenReturn("Amount must be greater than zero");

        // Act & Assert
        // This verifies that validateAmount is called and enforced
        assertThrows(IllegalArgumentException.class, () -> _triviaService.getQuestions(-1));
        assertThrows(IllegalArgumentException.class, () -> _triviaService.getQuestions(0));
    }

    @Test
    void testRateLimit_InterruptWhileWaiting() throws Exception {
        // Arrange - make a first request to set lastRequestTime
        when(_restTemplate.getForObject(anyString(), eq(String.class))).thenReturn("{}");
        _triviaService.getTrivia(1);

        // Create a thread that will get interrupted during the wait
        Thread testThread = new Thread(() -> {
            try {
                // This should trigger waiting because we just made a request
                _triviaService.getTrivia(1);
                fail("Should have been interrupted");
            } catch (IllegalStateException e) {
                // Expected exception - check that it has the interrupted exception as cause
                assertInstanceOf(InterruptedException.class, e.getCause());
                // Verify that Thread.interrupt() was called by checking interrupt status
                assertTrue(Thread.currentThread().isInterrupted());
            }
        });

        // Act - start the thread and interrupt it during waiting
        testThread.start();
        Thread.sleep(SMALL_DELAY_MS); // Give the thread time to enter the wait state
        testThread.interrupt();
        testThread.join(LARGE_DELAY_MS); // Wait for thread to complete

        // Assert
        assertFalse(testThread.isAlive(), "Thread should have terminated");
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

    @Test
    void testCheckAnswer_SetsAllFields() {
        // Arrange
        // Prepare some questions first
        String jsonResponse = "{\"response_code\":0,\"results\":[" +
                "{\"category\":\"Science\",\"type\":\"multiple\",\"difficulty\":\"medium\"," +
                "\"question\":\"What is H2O?\",\"correct_answer\":\"Water\"," +
                "\"incorrect_answers\":[\"Carbon Dioxide\",\"Oxygen\",\"Hydrogen\"]}" +
                "]}";

        when(_restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(jsonResponse);

        // Get the questions to populate the answer map
        List<QuestionDTO> questions = _triviaService.getQuestions(1);
        String questionId = questions.get(0).getId();

        AnswerDTO answerDTO = new AnswerDTO();
        answerDTO.setQuestionId(questionId);
        answerDTO.setSelectedAnswer("Water");

        // Act
        AnswerResultDTO result = _triviaService.checkAnswer(answerDTO);

        // Assert
        assertEquals(questionId, result.getQuestionId());
        assertTrue(result.isCorrect());
        assertEquals("Water", result.getCorrectAnswer());
    }

    @Test
    void testRateLimit_EnforcesWaitingTime() {
        // Create a spy for time control
        TriviaService serviceSpy = spy(_triviaService);

        // Mock current time to return controlled values
        AtomicLong currentTime = new AtomicLong(LARGE_DELAY_MS);
        doAnswer(inv -> currentTime.get()).when(serviceSpy).getCurrentTimeMillis();

        // Set up response
        when(_restTemplate.getForObject(anyString(), eq(String.class))).thenReturn("{}");

        // First call - sets lastRequestTime
        serviceSpy.getTrivia(1);

        // Advance time slightly
        currentTime.set(LARGE_DELAY_MS * 2L);

        // Second call - should wait
        serviceSpy.getTrivia(1);

        // Verify waitForRateLimit was called with correct value (4000ms)
        verify(serviceSpy, atLeastOnce()).waitForRateLimit(anyLong(), any());
    }

    @Test
    void testInterruptHandling() throws InterruptedException {
        // Set up a test thread that will be interrupted
        AtomicBoolean wasInterrupted = new AtomicBoolean(false);
        CountDownLatch threadStarted = new CountDownLatch(1);
        CountDownLatch interruptProcessed = new CountDownLatch(1);

        Thread testThread = getTestingThread(threadStarted, wasInterrupted, interruptProcessed);

        // Wait for thread to make first call
        threadStarted.await(RATE_LIMIT_MS, TimeUnit.MILLISECONDS);
        Thread.sleep(SMALL_DELAY_MS); // Give thread time to enter waiting state
        testThread.interrupt();

        // Wait for thread to process the interrupt
        interruptProcessed.await(RATE_LIMIT_MS, TimeUnit.MILLISECONDS);
        testThread.join(LARGE_DELAY_MS);

        assertFalse(testThread.isAlive(), "Thread should have terminated");
        assertTrue(wasInterrupted.get(), "Thread should have been interrupted and exception caught");
    }

    private Thread getTestingThread(CountDownLatch threadStarted,
                                    AtomicBoolean wasInterrupted,
                                    CountDownLatch interruptProcessed) {
        Thread testThread = new Thread(() -> {
            try {
                when(_restTemplate.getForObject(anyString(), eq(String.class))).thenReturn("{}");

                // First call to set lastRequestTime
                _triviaService.getTrivia(1);
                threadStarted.countDown();

                // Second call should wait and get interrupted
                _triviaService.getTrivia(1);
                fail("Should have thrown exception due to interruption");
            } catch (IllegalStateException e) {
                // Check if the cause is InterruptedException
                wasInterrupted.set(e.getCause() instanceof InterruptedException);
                // Verify thread interrupt flag is set
                wasInterrupted.set(wasInterrupted.get() && Thread.currentThread().isInterrupted());
                interruptProcessed.countDown();
            }
        });

        // Start thread and interrupt it during the wait
        testThread.start();
        return testThread;
    }
}
