package com.yas.payment.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ConstantsTest {

    @Test
    void exposesExpectedErrorCodeAndMessageConstants() {
        assertThat(Constants.ErrorCode.PAYMENT_PROVIDER_NOT_FOUND).isEqualTo("PAYMENT_PROVIDER_NOT_FOUND");
        assertThat(Constants.Message.SUCCESS_MESSAGE).isEqualTo("SUCCESS");
    }
}
