package com.yas.location.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ConstantsTest {

    @Test
    void errorCode_constantsShouldMatchExpectedValues() {
        assertEquals("COUNTRY_NOT_FOUND", Constants.ErrorCode.COUNTRY_NOT_FOUND);
        assertEquals("NAME_ALREADY_EXITED", Constants.ErrorCode.NAME_ALREADY_EXITED);
        assertEquals("STATE_OR_PROVINCE_NOT_FOUND", Constants.ErrorCode.STATE_OR_PROVINCE_NOT_FOUND);
        assertEquals("ADDRESS_NOT_FOUND", Constants.ErrorCode.ADDRESS_NOT_FOUND);
        assertEquals("CODE_ALREADY_EXISTED", Constants.ErrorCode.CODE_ALREADY_EXISTED);
    }

    @Test
    void pageable_constantsShouldMatchExpectedValues() {
        assertEquals("10", Constants.PageableConstant.DEFAULT_PAGE_SIZE);
        assertEquals("0", Constants.PageableConstant.DEFAULT_PAGE_NUMBER);
    }

    @Test
    void api_constantsShouldMatchExpectedValues() {
        assertEquals("/backoffice/state-or-provinces", Constants.ApiConstant.STATE_OR_PROVINCES_URL);
        assertEquals("/storefront/state-or-provinces", Constants.ApiConstant.STATE_OR_PROVINCES_STOREFRONT_URL);
        assertEquals("200", Constants.ApiConstant.CODE_200);
        assertEquals("Ok", Constants.ApiConstant.OK);
        assertEquals("404", Constants.ApiConstant.CODE_404);
        assertEquals("Not found", Constants.ApiConstant.NOT_FOUND);
    }
}
