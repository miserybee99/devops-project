package com.yas.cart.web;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RetryTestController {
    @GetMapping("/cart/debug/retry-once")
    public ResponseEntity<String> retryOnce() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("first call failed intentionally");
    }
}
