package com.yas.payment.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.payment.service.PaymentProviderService;
import com.yas.payment.viewmodel.paymentprovider.CreatePaymentVm;
import com.yas.payment.viewmodel.paymentprovider.PaymentProviderVm;
import com.yas.payment.viewmodel.paymentprovider.UpdatePaymentVm;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

class PaymentProviderControllerTest {

    private PaymentProviderService paymentProviderService;
    private PaymentProviderController paymentProviderController;

    @BeforeEach
    void setUp() {
        paymentProviderService = mock(PaymentProviderService.class);
        paymentProviderController = new PaymentProviderController(paymentProviderService);
    }

    @Test
    void create_returnsCreatedResponse() {
        var request = new CreatePaymentVm();
        request.setId("paypal");
        request.setName("PayPal");
        request.setConfigureUrl("https://paypal.example");
        var expected = new PaymentProviderVm("paypal", "PayPal", "https://paypal.example", 1, 1L, "icon");
        when(paymentProviderService.create(request)).thenReturn(expected);

        var response = paymentProviderController.create(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(expected);
        verify(paymentProviderService).create(request);
    }

    @Test
    void update_returnsOkResponse() {
        var request = new UpdatePaymentVm();
        request.setId("paypal");
        request.setName("PayPal");
        request.setConfigureUrl("https://paypal.example");
        var expected = new PaymentProviderVm("paypal", "PayPal", "https://paypal.example", 2, 2L, "icon");
        when(paymentProviderService.update(request)).thenReturn(expected);

        var response = paymentProviderController.update(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
        verify(paymentProviderService).update(request);
    }

    @Test
    void getAll_returnsEnabledProviders() {
        Pageable pageable = PageRequest.of(0, 5);
        var providers = List.of(new PaymentProviderVm("paypal", "PayPal", "https://paypal.example", 1, 1L, "icon"));
        when(paymentProviderService.getEnabledPaymentProviders(pageable)).thenReturn(providers);

        var response = paymentProviderController.getAll(pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(providers);
        verify(paymentProviderService).getEnabledPaymentProviders(pageable);
    }
}
