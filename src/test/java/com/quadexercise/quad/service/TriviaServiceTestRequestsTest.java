package com.quadexercise.quad.service;

import com.quadexercise.quad.dto.AnswerDTO;
import com.quadexercise.quad.exceptions.QuestionNotFoundException;
import com.quadexercise.quad.exceptions.TriviaParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

import static com.quadexercise.quad.testUtils.TestConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SuppressWarnings("DuplicateStringLiteralInspection")
@ExtendWith(MockitoExtension.class)
class TriviaServiceTestRequestsTest {

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
    void testGetTrivia_ShouldHandleMultipleRequests() {
        // Arrange
        when(_restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(EMPTY_JSON_RESPONSE);

        // Act & Assert
        // Due to rate limiting, this test will take time proportional to the number of calls
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 3; i++) {
                _triviaService.getTrivia(1);
            }
        });

        // Verify exact number of calls
        verify(_restTemplate, times(3))
                .getForObject(anyString(), eq(String.class));
    }

    @Test
    void testRateLimit_ThreadWaitsAsExpected() throws InterruptedException {
        // Arrange
        when(_restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(EMPTY_JSON_RESPONSE);
        _triviaService.getTrivia(1);

        // Act
        long startTime = System.currentTimeMillis();
        CompletableFuture.runAsync(() -> assertDoesNotThrow(() -> _triviaService.getTrivia(1)));

        final long halfTimeout = RATE_LIMIT_MS / 2L;
        Thread.sleep(halfTimeout);

        // Assert
        long midTime = System.currentTimeMillis() - startTime;
        assertTrue(halfTimeout <= midTime, "Should still be waiting at halfway point");
    }

    @Test
    void testGetQuestions_ShouldThrowTriviaParseException_WhenMalformedResults() {
        // Arrange
        String invalidResponse = String.format(
                "{\"response_code\":0,\"results\":[\"%s\"]}",
                "This is not a valid result object");

        when(_restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(invalidResponse);

        // Act & Assert
        assertThrows(TriviaParseException.class, () -> _triviaService.getQuestions(1));
    }

    @Test
    void testCheckAnswers_ShouldThrowQuestionNotFoundException() {
        // Arrange
        AnswerDTO answerDTO = new AnswerDTO();
        answerDTO.setQuestionId("non-existent-id");
        answerDTO.setSelectedAnswer("Some Answer");
        List<AnswerDTO> answers = List.of(answerDTO);

        // Act & Assert
        QuestionNotFoundException exception = assertThrows(
                QuestionNotFoundException.class,
                () -> _triviaService.checkAnswers(answers));

        assertEquals("non-existent-id", exception.getQuestionId());
    }

    @Test
    void testParseQuestionsFromResponse_InvalidJsonStructure() {
        // Arrange
        String invalidJson = "{\"response_code\":0,\"results\":\"not-an-array\"}";
        setupMocksForInvalidJson(invalidJson);

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
        // Arrange
        setupMessageServiceForAmountValidation();

        // Act & Assert
        Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> _triviaService.getTrivia(-5));

        assertTrue(exception.getMessage().contains("greater than zero"));
    }

    @Test
    void testValidateAmount_ZeroAmount() {
        // Arrange
        setupMessageServiceForAmountValidation();

        // Act & Assert
        Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> _triviaService.getTrivia(0));

        assertTrue(exception.getMessage().contains("greater than zero"));
    }

    @Test
    void testGetTrivia_ReturnsExpectedResponse() {
        // Arrange
        String expectedResponse = "{\"response_code\":0,\"results\":[]}";
        when(_restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(expectedResponse);

        // Act
        String result = _triviaService.getTrivia(1);

        // Assert
        assertEquals(expectedResponse, result);
    }

    @Test
    void testGetQuestions_ValidatesAmountParameter() {
        // Arrange
        setupMessageServiceForAmountValidation();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> _triviaService.getQuestions(-1));
        assertThrows(IllegalArgumentException.class, () -> _triviaService.getQuestions(0));
    }

    @Test
    void testRateLimit_InterruptWhileWaiting() throws Exception {
        // Arrange - make a first request to set lastRequestTime
        when(_restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(EMPTY_JSON_RESPONSE);
        _triviaService.getTrivia(1);

        Thread testThread = createThreadForInterruptionTest();

        // Act - start the thread and interrupt it during waiting
        testThread.start();
        Thread.sleep(SMALL_DELAY_MS); // Give the thread time to enter the wait state
        testThread.interrupt();
        testThread.join(LARGE_DELAY_MS); // Wait for thread to complete

        // Assert
        assertFalse(testThread.isAlive(), "Thread should have terminated");
    }

    @Test
    void testRateLimit_EnforcesWaitingTime() {
        // Arrange
        TriviaService serviceSpy = spy(_triviaService);
        AtomicLong currentTime = new AtomicLong(LARGE_DELAY_MS);
        doAnswer(inv -> currentTime.get()).when(serviceSpy).getCurrentTimeMillis();

        when(_restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(EMPTY_JSON_RESPONSE);

        // Act
        serviceSpy.getTrivia(1); // First call - sets lastRequestTime
        currentTime.set(LARGE_DELAY_MS * 2L); // Advance time slightly
        serviceSpy.getTrivia(1); // Second call - should wait

        // Assert
        verify(serviceSpy, atLeastOnce()).waitForRateLimit(anyLong(), any());
    }

    // Helper methods

    private Thread createThreadForInterruptionTest() {
        return new Thread(() -> {
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
    }

    private void setupMessageServiceForAmountValidation() {
        lenient().when(_messageService.getMessage(anyString()))
                .thenReturn("Amount must be greater than zero");
    }

    private void setupMocksForInvalidJson(String invalidJson) {
        // Reset mocks to clear any previous stubs
        reset(_restTemplate, _messageService);

        // Use lenient stubbing for both mocks
        lenient().when(_messageService.getMessage(anyString())).thenReturn(TEST_MESSAGE);
        lenient().when(_restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(invalidJson);
    }
}