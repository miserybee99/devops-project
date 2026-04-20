package com.yas.payment.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.yas.payment.model.PaymentProvider;
import com.yas.payment.viewmodel.paymentprovider.CreatePaymentVm;
import com.yas.payment.viewmodel.paymentprovider.PaymentProviderVm;
import com.yas.payment.viewmodel.paymentprovider.UpdatePaymentVm;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class PaymentProviderMapperTest {

    private final PaymentProviderMapper paymentProviderMapper = Mappers.getMapper(PaymentProviderMapper.class);
    private final CreatePaymentProviderMapper createPaymentProviderMapper = Mappers.getMapper(CreatePaymentProviderMapper.class);
    private final UpdatePaymentProviderMapper updatePaymentProviderMapper = Mappers.getMapper(UpdatePaymentProviderMapper.class);

    @Test
    void paymentProviderMapper_mapsBothDirections() {
        var model = PaymentProvider.builder()
            .id("paypal")
            .name("PayPal")
            .enabled(true)
            .configureUrl("https://paypal.example")
            .landingViewComponentName("paypal-component")
            .additionalSettings("{\"a\":1}")
            .mediaId(7L)
            .version(2)
            .build();

        PaymentProviderVm vm = paymentProviderMapper.toVm(model);
        PaymentProvider roundTrip = paymentProviderMapper.toModel(vm);

        assertThat(vm.getId()).isEqualTo("paypal");
        assertThat(vm.getName()).isEqualTo("PayPal");
        assertThat(vm.getConfigureUrl()).isEqualTo("https://paypal.example");
        assertThat(vm.getMediaId()).isEqualTo(7L);
        assertThat(roundTrip.getId()).isEqualTo("paypal");
        assertThat(roundTrip.getName()).isEqualTo("PayPal");
    }

    @Test
    void createMapper_setsIsNewAndMapsFields() {
        var vm = new CreatePaymentVm();
        vm.setId("stripe");
        vm.setEnabled(true);
        vm.setName("Stripe");
        vm.setConfigureUrl("https://stripe.example");
        vm.setLandingViewComponentName("stripe-component");
        vm.setAdditionalSettings("{\"b\":2}");
        vm.setMediaId(8L);

        var model = createPaymentProviderMapper.toModel(vm);
        var response = createPaymentProviderMapper.toVmResponse(model);

        assertThat(model.getId()).isEqualTo("stripe");
        assertThat(model.isEnabled()).isTrue();
        assertThat(model.isNew()).isTrue();
        assertThat(response.getId()).isEqualTo("stripe");
        assertThat(response.getName()).isEqualTo("Stripe");
    }

    @Test
    void updateMapper_partialUpdate_ignoresNulls() {
        var existing = PaymentProvider.builder()
            .id("paypal")
            .name("Old Name")
            .configureUrl("https://old.example")
            .enabled(false)
            .mediaId(1L)
            .build();
        var vm = new UpdatePaymentVm();
        vm.setId("paypal");
        vm.setName("New Name");
        vm.setConfigureUrl(null);
        vm.setEnabled(true);
        vm.setMediaId(9L);

        updatePaymentProviderMapper.partialUpdate(existing, vm);
        var response = updatePaymentProviderMapper.toVmResponse(existing);

        assertThat(existing.getName()).isEqualTo("New Name");
        assertThat(existing.getConfigureUrl()).isEqualTo("https://old.example");
        assertThat(existing.isEnabled()).isTrue();
        assertThat(existing.getMediaId()).isEqualTo(9L);
        assertThat(response.getName()).isEqualTo("New Name");
    }
}
