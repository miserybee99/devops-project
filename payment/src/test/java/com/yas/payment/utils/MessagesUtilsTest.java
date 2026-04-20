package com.yas.payment.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MessagesUtilsTest {

    private Locale originalLocale;

    @BeforeEach
    void setUp() {
        originalLocale = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
    }

    @AfterEach
    void tearDown() {
        Locale.setDefault(originalLocale);
    }

    @Test
    void getMessage_returnsResolvedMessageWithArgs() {
        var result = MessagesUtils.getMessage("PAYMENT_PROVIDER_NOT_FOUND", "100");

        assertThat(result).contains("100");
    }

    @Test
    void getMessage_returnsErrorCodeWhenMessageIsMissing() {
        var result = MessagesUtils.getMessage("NOT_EXIST_CODE");

        assertThat(result).isEqualTo("NOT_EXIST_CODE");
    }
}
