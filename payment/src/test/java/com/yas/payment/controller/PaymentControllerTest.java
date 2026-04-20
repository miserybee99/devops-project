package com.yas.payment.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.payment.model.enumeration.PaymentMethod;
import com.yas.payment.model.enumeration.PaymentStatus;
import com.yas.payment.service.PaymentService;
import com.yas.payment.viewmodel.CapturePaymentRequestVm;
import com.yas.payment.viewmodel.CapturePaymentResponseVm;
import com.yas.payment.viewmodel.InitPaymentRequestVm;
import com.yas.payment.viewmodel.InitPaymentResponseVm;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class PaymentControllerTest {

    private PaymentService paymentService;
    private PaymentController paymentController;

    @BeforeEach
    void setUp() {
        paymentService = mock(PaymentService.class);
        paymentController = new PaymentController(paymentService);
    }

    @Test
    void initPayment_delegatesToService() {
        var request = InitPaymentRequestVm.builder()
            .paymentMethod(PaymentMethod.PAYPAL.name())
            .checkoutId("checkout-1")
            .totalPrice(BigDecimal.TEN)
            .build();
        var expected = InitPaymentResponseVm.builder()
            .status("PENDING")
            .paymentId("pid-1")
            .redirectUrl("https://example.com")
            .build();
        when(paymentService.initPayment(request)).thenReturn(expected);

        var result = paymentController.initPayment(request);

        assertThat(result).isEqualTo(expected);
        verify(paymentService).initPayment(request);
    }

    @Test
    void capturePayment_delegatesToService() {
        var request = CapturePaymentRequestVm.builder()
            .paymentMethod(PaymentMethod.PAYPAL.name())
            .token("token-1")
            .build();
        var expected = CapturePaymentResponseVm.builder()
            .orderId(10L)
            .checkoutId("checkout-1")
            .amount(BigDecimal.TEN)
            .paymentFee(BigDecimal.ONE)
            .gatewayTransactionId("tx-1")
            .paymentMethod(PaymentMethod.PAYPAL)
            .paymentStatus(PaymentStatus.COMPLETED)
            .failureMessage(null)
            .build();
        when(paymentService.capturePayment(request)).thenReturn(expected);

        var result = paymentController.capturePayment(request);

        assertThat(result).isEqualTo(expected);
        verify(paymentService).capturePayment(request);
    }

    @Test
    void cancelPayment_returnsOkMessage() {
        var response = paymentController.cancelPayment();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Payment cancelled");
    }
}
