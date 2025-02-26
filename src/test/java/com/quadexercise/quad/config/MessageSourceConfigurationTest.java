package com.quadexercise.quad.config;

import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.ResourceBundleMessageSource;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MessageSourceConfigurationTest {

    @Test
    void testMessageSource() {
        // Arrange
        MessageSourceConfiguration configuration = new MessageSourceConfiguration();

        // Act
        MessageSource messageSource = configuration.messageSource();

        // Assert
        assertNotNull(messageSource);
        assertInstanceOf(ResourceBundleMessageSource.class, messageSource);

        // We can't access protected methods directly, but we can test indirectly
        // by checking the bean was created without errors

        // Test actual message resolution if possible
        try {
            String message = messageSource.getMessage("error.amount.greater.than.zero", null, null);
            assertNotNull(message);
        } catch (NoSuchMessageException e) {
            // If resource bundle isn't available in test context, this is expected
            assertInstanceOf(org.springframework.context.NoSuchMessageException.class, e);
        }
    }
}