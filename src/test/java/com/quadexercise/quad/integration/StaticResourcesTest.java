package com.quadexercise.quad.integration;

import com.quadexercise.quad.Application;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("DuplicateStringLiteralInspection")
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
class StaticResourcesTest {

    @Autowired
    private MockMvc _mockMvc;

    @Test
    void testCssResource_IsAvailableWithCorrectContentType() throws Exception {
        // Arrange
        String cssPath = "/css/custom.css";
        String expectedContentType = "text/css";

        // Act & Assert
        _mockMvc.perform(get(cssPath))
                .andExpect(status().isOk())
                .andExpect(content().contentType(expectedContentType));
    }

    @Test
    void testTriviaJsResource_IsAvailableWithCorrectContentType() throws Exception {
        // Arrange
        String jsPath = "/js/trivia.js";
        String expectedContentType = "text/javascript";

        // Act & Assert
        _mockMvc.perform(get(jsPath))
                .andExpect(status().isOk())
                .andExpect(content().contentType(expectedContentType));
    }

    @Test
    void testResultsJsResource_IsAvailableWithCorrectContentType() throws Exception {
        // Arrange
        String jsPath = "/js/results.js";
        String expectedContentType = "text/javascript";

        // Act & Assert
        _mockMvc.perform(get(jsPath))
                .andExpect(status().isOk())
                .andExpect(content().contentType(expectedContentType));
    }
}