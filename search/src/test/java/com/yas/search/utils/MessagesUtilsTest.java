package com.yas.search.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MessagesUtilsTest {

    @Test
    void getMessage_shouldReturnCodeWhenKeyDoesNotExist() {
        String result = MessagesUtils.getMessage("UNKNOWN_MESSAGE_KEY");
        assertEquals("UNKNOWN_MESSAGE_KEY", result);
    }

    @Test
    void getMessage_shouldFormatFallbackMessage() {
        String result = MessagesUtils.getMessage("Hello {}", "YAS");
        assertEquals("Hello YAS", result);
    }
}
