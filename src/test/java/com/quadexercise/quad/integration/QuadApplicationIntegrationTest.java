package com.quadexercise.quad.integration;

import com.quadexercise.quad.Application;
import com.quadexercise.quad.controller.TriviaController;
import com.quadexercise.quad.service.TriviaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@SuppressWarnings("DuplicateStringLiteralInspection")
class QuadApplicationIntegrationTest {

    private static final String HOME_TEMPLATE_NAME = "homeTemplate";
    private static final String HOME_EXPECTED_CONTENT = "Test your knowledge";
    private static final String RESPONSE_CODE_KEY = "response_code";
    private static final String TEXT_PLAIN_UTF8 = "text/plain;charset=UTF-8";

    @Autowired
    private MockMvc _mockMvc;

    @Autowired
    private ApplicationContext _applicationContext;

    @Test
    void testContextLoadsVerifyRequiredBeans() {
        // Arrange
        // Spring context is autowired

        // Act & Assert
        assertNotNull(_applicationContext, "Application context should not be null");
        assertNotNull(_applicationContext.getBean(TriviaService.class), "TriviaService should be loaded");
        assertNotNull(_applicationContext.getBean(TriviaController.class), "TriviaController should be loaded");
    }

    @Test
    void testHomePageLoadsSuccessfully() throws Exception {
        // Arrange
        // MockMvc is autowired

        // Act & Assert (combined in Spring MVC testing)
        _mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name(HOME_TEMPLATE_NAME))
                .andExpect(content().string(containsString(HOME_EXPECTED_CONTENT)));
    }

    @Test
    void testTriviaEndpointReturnsValidJson() throws Exception {
        // Arrange
        // MockMvc is autowired

        // Act & Assert (combined in Spring MVC testing)
        _mockMvc.perform(get("/test"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(TEXT_PLAIN_UTF8))
                .andExpect(content().string(containsString(RESPONSE_CODE_KEY)));
    }
}