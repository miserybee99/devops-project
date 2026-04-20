package com.yas.payment.service.provider.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.payment.model.enumeration.PaymentMethod;
import com.yas.payment.model.enumeration.PaymentStatus;
import com.yas.payment.paypal.service.PaypalService;
import com.yas.payment.paypal.viewmodel.PaypalCapturePaymentResponse;
import com.yas.payment.paypal.viewmodel.PaypalCreatePaymentResponse;
import com.yas.payment.service.PaymentProviderService;
import com.yas.payment.viewmodel.CapturePaymentRequestVm;
import com.yas.payment.viewmodel.InitPaymentRequestVm;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PaypalHandlerTest {

    private PaymentProviderService paymentProviderService;
    private PaypalService paypalService;
    private PaypalHandler paypalHandler;

    @BeforeEach
    void setUp() {
        paymentProviderService = mock(PaymentProviderService.class);
        paypalService = mock(PaypalService.class);
        paypalHandler = new PaypalHandler(paymentProviderService, paypalService);
    }

    @Test
    void initPayment_mapsInputAndResponse() {
        var request = InitPaymentRequestVm.builder()
            .paymentMethod(PaymentMethod.PAYPAL.name())
            .checkoutId("checkout-1")
            .totalPrice(BigDecimal.valueOf(50))
            .build();
        when(paymentProviderService.getAdditionalSettingsByPaymentProviderId(eq(PaymentMethod.PAYPAL.name())))
            .thenReturn("{\"clientId\":\"abc\"}");
        var paypalResponse = PaypalCreatePaymentResponse.builder()
            .status("PENDING")
            .paymentId("pid-1")
            .redirectUrl("https://paypal.example/redirect")
            .build();
        when(paypalService.createPayment(org.mockito.ArgumentMatchers.any())).thenReturn(paypalResponse);

        var result = paypalHandler.initPayment(request);

        assertThat(result.getStatus()).isEqualTo("PENDING");
        assertThat(result.getPaymentId()).isEqualTo("pid-1");
        assertThat(result.getRedirectUrl()).isEqualTo("https://paypal.example/redirect");
        verify(paymentProviderService).getAdditionalSettingsByPaymentProviderId(PaymentMethod.PAYPAL.name());
        verify(paypalService).createPayment(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void capturePayment_mapsPaypalResponse() {
        var request = CapturePaymentRequestVm.builder()
            .token("token-1")
            .paymentMethod(PaymentMethod.PAYPAL.name())
            .build();
        when(paymentProviderService.getAdditionalSettingsByPaymentProviderId(eq(PaymentMethod.PAYPAL.name())))
            .thenReturn("{\"clientId\":\"abc\"}");
        var paypalResponse = PaypalCapturePaymentResponse.builder()
            .checkoutId("checkout-1")
            .amount(BigDecimal.valueOf(30))
            .paymentFee(BigDecimal.ONE)
            .gatewayTransactionId("tx-1")
            .paymentMethod(PaymentMethod.PAYPAL.name())
            .paymentStatus(PaymentStatus.COMPLETED.name())
            .failureMessage(null)
            .build();
        when(paypalService.capturePayment(org.mockito.ArgumentMatchers.any())).thenReturn(paypalResponse);

        var result = paypalHandler.capturePayment(request);

        assertThat(result.getCheckoutId()).isEqualTo("checkout-1");
        assertThat(result.getAmount()).isEqualByComparingTo("30");
        assertThat(result.getPaymentFee()).isEqualByComparingTo("1");
        assertThat(result.getGatewayTransactionId()).isEqualTo("tx-1");
        assertThat(result.getPaymentMethod()).isEqualTo(PaymentMethod.PAYPAL);
        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
        verify(paymentProviderService).getAdditionalSettingsByPaymentProviderId(PaymentMethod.PAYPAL.name());
        verify(paypalService).capturePayment(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void getProviderId_returnsPaypal() {
        assertThat(paypalHandler.getProviderId()).isEqualTo(PaymentMethod.PAYPAL.name());
    }
}
