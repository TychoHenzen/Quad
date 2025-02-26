package com.quadexercise.quad;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

class ApplicationTest {

    @Test
    void testMain() {
        try (MockedStatic<SpringApplication> applicationMock = mockStatic(SpringApplication.class)) {
            // Arrange
            applicationMock.when(() -> SpringApplication.run(eq(Application.class), any(String[].class)))
                    .thenReturn(null);

            // Act
            String[] args = new String[0];
            Application.main(args);

            // Assert
            applicationMock.verify(() -> SpringApplication.run(Application.class, args));
        }
    }
}