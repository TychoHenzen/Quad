package com.quadexercise.quad.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

/**
 * Service for internationalized message handling.
 */
@Service
public class MessageService {

    private final MessageSource _messageSource;

    @Autowired
    public MessageService(MessageSource messageSource) {
        _messageSource = messageSource;
    }

    /**
     * Get a message from the message source.
     *
     * @param key  the message key
     * @param args the message arguments
     * @return the localized message
     */
    public String getMessage(String key, Object... args) {
        return _messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }
}
