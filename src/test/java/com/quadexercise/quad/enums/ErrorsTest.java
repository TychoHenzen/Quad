package com.quadexercise.quad.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErrorsTest {

    @Test
    void testErrorConstants() {
        // Test ERR_UNAVAILABLE constant value
        assertEquals(503, Errors.ERR_UNAVAILABLE);
    }
}