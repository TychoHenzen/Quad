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
class QuadApplicationIntegrationTest {

    @Autowired
    private MockMvc _mockMvc;
    @Autowired
    private ApplicationContext _applicationContext;

    @Test
    void testContextLoads() {
        assertNotNull(_applicationContext, "Application context should not be null");
        assertNotNull(_applicationContext.getBean(TriviaService.class), "TriviaService should be loaded");
        assertNotNull(_applicationContext.getBean(TriviaController.class), "TriviaController should be loaded");
    }

    @Test
    void testHomePage_ShouldLoadSuccessfully() throws Exception {
        _mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(content().string(containsString("Welcome to my Spring Application!")));
    }

    @Test
    void testTriviaEndpoint_ShouldReturnValidJson() throws Exception {
        _mockMvc.perform(get("/test"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                .andExpect(content().string(containsString("response_code")));
    }
}