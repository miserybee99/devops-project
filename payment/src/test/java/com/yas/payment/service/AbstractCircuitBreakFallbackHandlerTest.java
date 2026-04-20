package com.yas.payment.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class AbstractCircuitBreakFallbackHandlerTest {

    private final TestFallbackHandler handler = new TestFallbackHandler();

    @Test
    void handleBodilessFallback_rethrowsOriginalException() {
        RuntimeException ex = new RuntimeException("boom");

        assertThatThrownBy(() -> handler.handleBodilessFallback(ex))
            .isSameAs(ex);
    }

    @Test
    void handleTypedFallback_rethrowsOriginalException() {
        IllegalStateException ex = new IllegalStateException("typed boom");

        assertThatThrownBy(() -> handler.handleTypedFallback(ex))
            .isSameAs(ex);
    }

    private static final class TestFallbackHandler extends AbstractCircuitBreakFallbackHandler {
    }
}
