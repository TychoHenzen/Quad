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
    void testCssResourcesAreAvailable() throws Exception {
        _mockMvc.perform(get("/css/custom.css"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/css"));
    }

    @Test
    void testJsResourcesAreAvailable() throws Exception {
        // Test trivia.js
        _mockMvc.perform(get("/js/trivia.js"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/javascript"));

        // Test results.js
        _mockMvc.perform(get("/js/results.js"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/javascript"));
    }
}