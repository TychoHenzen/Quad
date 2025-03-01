package com.quadexercise.quad.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for trivia service components.
 * Provides beans needed by the trivia services.
 */
@Configuration
public class TriviaServiceConfiguration {

    /**
     * Creates a RestTemplate bean for HTTP requests.
     *
     * @return Configured RestTemplate
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * Creates an ObjectMapper bean for JSON processing.
     *
     * @return Configured ObjectMapper
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
