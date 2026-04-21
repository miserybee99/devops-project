package com.yas.delivery.service;

import org.springframework.stereotype.Service;

@Service
public class DeliveryService {

    /**
     * Resolves a simple delivery status for an order. Extend with real logistics later.
     */
    public String statusForOrder(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId is required");
        }
        return "PENDING";
    }
}
