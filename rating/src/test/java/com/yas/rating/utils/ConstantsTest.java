package com.yas.rating.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ConstantsTest {

    @Test
    void errorCode_constantsShouldMatchExpectedValues() {
        assertEquals("RATING_NOT_FOUND", Constants.ErrorCode.RATING_NOT_FOUND);
        assertEquals("PRODUCT_NOT_FOUND", Constants.ErrorCode.PRODUCT_NOT_FOUND);
        assertEquals("CUSTOMER_NOT_FOUND", Constants.ErrorCode.CUSTOMER_NOT_FOUND);
        assertEquals("RESOURCE_ALREADY_EXISTED", Constants.ErrorCode.RESOURCE_ALREADY_EXISTED);
        assertEquals("ACCESS_DENIED", Constants.ErrorCode.ACCESS_DENIED);
    }

    @Test
    void message_constantsShouldMatchExpectedValues() {
        assertEquals("SUCCESS", Constants.Message.SUCCESS_MESSAGE);
    }
}
