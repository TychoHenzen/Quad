package com.quadexercise.quad.interfaces;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InterruptibleRunnableTest {

    @SuppressWarnings("ConstantValue")
    @Test
    void testInterruptibleRunnable_SuccessfulExecution() throws InterruptedException {
        // Arrange
        boolean[] wasExecuted = {false};
        InterruptibleRunnable runnable = () -> wasExecuted[0] = true;

        // Act
        runnable.run();

        // Assert
        assertTrue(wasExecuted[0], "The runnable should have executed");
    }

    @Test
    void testInterruptibleRunnable_ThrowsInterruptedException() {
        // Arrange
        InterruptibleRunnable runnable = () -> {
            throw new InterruptedException("Test interruption");
        };

        // Act & Assert
        assertThrows(InterruptedException.class, runnable::run);
    }

    @SuppressWarnings("DuplicateStringLiteralInspection")
    @Test
    void testInterruptibleRunnable_UsedInContext() throws InterruptedException {
        // Arrange
        StringBuilder result = new StringBuilder(0);

        // Create a method that accepts our interface
        class TestExecutor {
            void execute(InterruptibleRunnable action) throws InterruptedException {
                action.run();
            }
        }

        TestExecutor executor = new TestExecutor();

        // Act
        executor.execute(() -> result.append("executed"));

        // Assert
        assertEquals("executed", result.toString());
    }
}