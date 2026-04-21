package com.yas.delivery.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class DeliveryServiceTest {

    private final DeliveryService deliveryService = new DeliveryService();

    @Test
    void statusForOrder_returnsPending_whenValidId() {
        assertThat(deliveryService.statusForOrder("ord-1")).isEqualTo("PENDING");
    }

    @Test
    void statusForOrder_rejectsNullOrderId() {
        assertThatThrownBy(() -> deliveryService.statusForOrder(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("orderId");
    }

    @Test
    void statusForOrder_rejectsBlankOrderId() {
        assertThatThrownBy(() -> deliveryService.statusForOrder("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
