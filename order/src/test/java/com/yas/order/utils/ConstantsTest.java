package com.yas.order.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ConstantsTest {

    @Test
    void errorCode_constantsShouldMatchExpectedValues() {
        assertEquals("ORDER_NOT_FOUND", Constants.ErrorCode.ORDER_NOT_FOUND);
        assertEquals("CHECKOUT_NOT_FOUND", Constants.ErrorCode.CHECKOUT_NOT_FOUND);
        assertEquals("CHECKOUT_ITEM_NOT_EMPTY", Constants.ErrorCode.CHECKOUT_ITEM_NOT_EMPTY);
        assertEquals("SIGN_IN_REQUIRED", Constants.ErrorCode.SIGN_IN_REQUIRED);
    }

    @Test
    void messageCode_constantsShouldMatchExpectedValues() {
        assertEquals("Create checkout {} by user {}", Constants.MessageCode.CREATE_CHECKOUT);
        assertEquals("Update checkout {} STATUS from {} to {}", Constants.MessageCode.UPDATE_CHECKOUT_STATUS);
        assertEquals("Update checkout {} PAYMENT from {} to {}", Constants.MessageCode.UPDATE_CHECKOUT_PAYMENT);
    }

    @Test
    void column_constantsShouldMatchExpectedValues() {
        assertEquals("id", Constants.Column.ID_COLUMN);
        assertEquals("createdOn", Constants.Column.CREATE_ON_COLUMN);
        assertEquals("createdBy", Constants.Column.CREATE_BY_COLUMN);
        assertEquals("orderId", Constants.Column.ORDER_ORDER_ID_COLUMN);
        assertEquals("productId", Constants.Column.ORDER_ITEM_PRODUCT_ID_COLUMN);
    }
}
