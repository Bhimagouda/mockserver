package org.testing.mockserver.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.testing.mockserver.service.PaymentMockService;

import java.util.Map;

@RestController
@RequestMapping("/admin/mocks")
@RequiredArgsConstructor
public class MockAdminController {

    private final PaymentMockService paymentMockService;

    @PostMapping("/payment/success")
    public ResponseEntity<Map<String, String>> registerPaymentSuccess() {
        paymentMockService.registerSuccess();
        return ResponseEntity.ok(Map.of("registered", "POST /bank/payment -> 200 SUCCESS"));
    }

    @PostMapping("/payment/failure")
    public ResponseEntity<Map<String, String>> registerPaymentFailure() {
        paymentMockService.registerFailure();
        return ResponseEntity.ok(Map.of("registered", "POST /bank/payment -> 500 FAILED"));
    }

    @PostMapping("/payment/timeout")
    public ResponseEntity<Map<String, String>> registerPaymentTimeout() {
        paymentMockService.registerTimeout();
        return ResponseEntity.ok(Map.of("registered", "POST /bank/payment -> 200 with 10s delay"));
    }

    @PostMapping("/payment/unauthorized")
    public ResponseEntity<Map<String, String>> registerPaymentUnauthorized() {
        paymentMockService.registerUnauthorized();
        return ResponseEntity.ok(Map.of("registered", "POST /bank/payment -> 401 UNAUTHORIZED"));
    }

    @PostMapping("/payment/rate-limit")
    public ResponseEntity<Map<String, String>> registerPaymentRateLimit() {
        paymentMockService.registerRateLimit();
        return ResponseEntity.ok(Map.of("registered", "POST /bank/payment -> 429 RATE_LIMITED"));
    }

    @DeleteMapping("/reset")
    public ResponseEntity<Map<String, String>> resetAllStubs() {
        paymentMockService.resetAll();
        return ResponseEntity.ok(Map.of("reset", "all stubs cleared"));
    }
}
