package com.yas.product.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ConstantsTest {

    @Test
    void errorCode_constantsShouldMatchExpectedValues() {
        assertEquals("PRODUCT_NOT_FOUND", Constants.ErrorCode.PRODUCT_NOT_FOUND);
        assertEquals("CATEGORY_NOT_FOUND", Constants.ErrorCode.CATEGORY_NOT_FOUND);
        assertEquals("BRAND_NOT_FOUND", Constants.ErrorCode.BRAND_NOT_FOUND);
        assertEquals("SLUG_IS_DUPLICATED", Constants.ErrorCode.SLUG_IS_DUPLICATED);
        assertEquals("PRODUCT_NOT_HAVE_VARIATION", Constants.ErrorCode.PRODUCT_NOT_HAVE_VARIATION);
        assertEquals("PRODUCT_COMBINATION_PROCESSING_FAILED", Constants.ErrorCode.PRODUCT_COMBINATION_PROCESSING_FAILED);
    }
}
