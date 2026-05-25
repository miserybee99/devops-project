package com.yas.inventory.constants;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ConstantsTest {

    @Test
    void messageCode_constantsShouldMatchExpectedValues() {
        assertEquals("WAREHOUSE_NOT_FOUND", MessageCode.WAREHOUSE_NOT_FOUND);
        assertEquals("PRODUCT_NOT_FOUND", MessageCode.PRODUCT_NOT_FOUND);
        assertEquals("NAME_ALREADY_EXITED", MessageCode.NAME_ALREADY_EXITED);
        assertEquals("STOCK_ALREADY_EXISTED", MessageCode.STOCK_ALREADY_EXISTED);
    }

    @Test
    void pageable_constantsShouldMatchExpectedValues() {
        assertEquals("10", PageableConstant.DEFAULT_PAGE_SIZE);
        assertEquals("0", PageableConstant.DEFAULT_PAGE_NUMBER);
    }

    @Test
    void api_constantsShouldMatchExpectedValues() {
        assertEquals("/backoffice/warehouses", ApiConstant.WAREHOUSE_URL);
        assertEquals("/backoffice/stocks/histories", ApiConstant.STOCK_HISTORY_URL);
        assertEquals("/backoffice/stocks", ApiConstant.STOCK_URL);
        assertEquals("ACCESS_DENIED", ApiConstant.ACCESS_DENIED);
        assertEquals("INVALID_ADJUSTED_QUANTITY", ApiConstant.INVALID_ADJUSTED_QUANTITY);
    }
}
