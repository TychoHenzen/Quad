package com.quadexercise.quad.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

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
    void testGetMessage_ShouldUseCurrentLocale() {
        // Arrange
        String key = "test.key";
        String expectedMessage = "Test Message";
        Locale currentLocale = LocaleContextHolder.getLocale();

        when(_messageSource.getMessage(eq(key), any(), eq(currentLocale)))
                .thenReturn(expectedMessage);

        // Act
        String result = _messageService.getMessage(key);

        // Assert
        assertEquals(expectedMessage, result);
        verify(_messageSource).getMessage(eq(key), any(), eq(currentLocale));
    }

    @Test
    void testGetMessage_ShouldPassArgumentsToMessageSource() {
        // Arrange
        String key = "test.key.with.args";
        String expectedMessage = "Test Message with arg1 and arg2";
        Object[] args = {"arg1", "arg2"};
        Locale currentLocale = LocaleContextHolder.getLocale();

        when(_messageSource.getMessage(key, args, currentLocale))
                .thenReturn(expectedMessage);

        // Act
        String result = _messageService.getMessage(key, args);

        // Assert
        assertEquals(expectedMessage, result);
        verify(_messageSource).getMessage(key, args, currentLocale);
    }
}