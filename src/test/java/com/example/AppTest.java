package com.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppTest {

    @Test
    void testMessage() {
        assertEquals(
            "Hello from Maven Nexus Demo",
            App.message()
        );
    }
}
