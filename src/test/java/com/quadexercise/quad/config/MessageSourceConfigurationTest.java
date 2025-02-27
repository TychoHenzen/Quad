package com.quadexercise.quad.config;

import org.junit.jupiter.api.Test;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class MessageSourceConfigurationTest {

    @Test
    void testMessageSourceShouldBeConfiguredCorrectly() {
        // Arrange
        MessageSourceConfiguration configuration = new MessageSourceConfiguration();

        // Act
        ResourceBundleMessageSource messageSource =
                (ResourceBundleMessageSource) configuration.messageSource();

        // Assert
        assertNotNull(messageSource);
    }

    @Test
    void testMessageSourceShouldUseCodeAsDefaultMessage() {
        // Arrange
        ResourceBundleMessageSource messageSource =
                (ResourceBundleMessageSource) new MessageSourceConfiguration().messageSource();
        messageSource.setUseCodeAsDefaultMessage(true);

        // Act
        String message = messageSource.getMessage("test.key", null, Locale.ENGLISH);

        // Assert
        assertEquals("test.key", message);
    }

    @Test
    void testMessageSourceShouldThrowExceptionForMissingKeys() {
        // Arrange
        ResourceBundleMessageSource messageSource =
                (ResourceBundleMessageSource) new MessageSourceConfiguration().messageSource();
        messageSource.setUseCodeAsDefaultMessage(false);

        // Act & Assert
        assertThrows(NoSuchMessageException.class, () ->
                messageSource.getMessage("nonexistent.key", null, Locale.ENGLISH));
    }

    @Test
    void testMessageSourceShouldRespectBasenameChanges() {
        // Arrange
        ResourceBundleMessageSource messageSource =
                (ResourceBundleMessageSource) new MessageSourceConfiguration().messageSource();
        messageSource.setUseCodeAsDefaultMessage(true);

        // Act
        messageSource.setBasenames("non-existent-basename");
        String message = messageSource.getMessage("test.key", null, Locale.ENGLISH);

        // Assert
        assertEquals("test.key", message);
    }
}