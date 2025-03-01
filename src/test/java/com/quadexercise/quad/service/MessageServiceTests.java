package com.quadexercise.quad.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

import static com.quadexercise.quad.testUtils.TestConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceTests {


    @Mock
    private MessageSource _messageSource;

    private MessageService _messageService;

    @BeforeEach
    void setUp() {
        _messageService = new MessageService(_messageSource);
    }

    @Test
    void testGetMessageWithNoArguments() {
        // Arrange
        Locale currentLocale = LocaleContextHolder.getLocale();
        when(_messageSource.getMessage(eq(TEST_KEY), any(), eq(currentLocale)))
                .thenReturn(TEST_MESSAGE);

        // Act
        String result = _messageService.getMessage(TEST_KEY);

        // Assert
        assertEquals(TEST_MESSAGE, result);
        verify(_messageSource).getMessage(eq(TEST_KEY), any(), eq(currentLocale));
    }

    @Test
    void testGetMessageWithArguments() {
        // Arrange
        Object[] args = {"arg1", "arg2"};
        Locale currentLocale = LocaleContextHolder.getLocale();
        when(_messageSource.getMessage(TEST_KEY_WITH_ARGS, args, currentLocale))
                .thenReturn(TEST_MESSAGE_WITH_ARGS);

        // Act
        String result = _messageService.getMessage(TEST_KEY_WITH_ARGS, args);

        // Assert
        assertEquals(TEST_MESSAGE_WITH_ARGS, result);
        verify(_messageSource).getMessage(TEST_KEY_WITH_ARGS, args, currentLocale);
    }
}