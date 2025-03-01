package com.quadexercise.quad.interfaces;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("DuplicateStringLiteralInspection")
class IInterruptibleRunnableTest {

    @SuppressWarnings("ConstantValue")
    @Test
    void testRun_ExecutesSuccessfully() throws InterruptedException {
        // Arrange
        boolean[] wasExecuted = {false};
        IInterruptibleRunnable runnable = () -> wasExecuted[0] = true;

        // Act
        runnable.run();

        // Assert
        assertTrue(wasExecuted[0], "The runnable should have executed");
    }

    @Test
    void testRun_PropagatesInterruptedException() {
        // Arrange
        IInterruptibleRunnable runnable = () -> {
            throw new InterruptedException("Test interruption");
        };

        // Act & Assert
        Exception exception = assertThrows(InterruptedException.class, runnable::run);

        // Additional Assert
        assertEquals("Test interruption", exception.getMessage());
    }

    @SuppressWarnings("DuplicateStringLiteralInspection")
    @Test
    void testRun_WorksInFunctionalContext() throws InterruptedException {
        // Arrange
        StringBuilder result = new StringBuilder(0);
        TestExecutor executor = new TestExecutor();

        // Act
        executor.execute(() -> result.append("executed"));

        // Assert
        assertEquals("executed", result.toString(), "Lambda should be executed by the TestExecutor");
    }

    // Helper class for the functional context test
    private static class TestExecutor {
        @SuppressWarnings("MethodMayBeStatic")
        void execute(IInterruptibleRunnable action) throws InterruptedException {
            action.run();
        }
    }
}