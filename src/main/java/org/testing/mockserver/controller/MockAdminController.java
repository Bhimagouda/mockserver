package org.testing.mockserver.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.testing.mockserver.model.PaymentScenario;
import org.testing.mockserver.service.PaymentMockService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/mocks")
@RequiredArgsConstructor
public class MockAdminController {

    private final PaymentMockService paymentMockService;

    @PostMapping("/payment/{scenario}")
    public ResponseEntity<Map<String, String>> activatePaymentScenario(@PathVariable String scenario) {
        return PaymentScenario.from(scenario)
                .map(s -> {
                    paymentMockService.activate(s);
                    return ResponseEntity.ok(Map.of("active", s.toSlug()));
                })
                .orElseGet(() -> ResponseEntity.badRequest().body(
                        Map.of("error", "Unknown scenario: " + scenario
                                + ". Supported: " + PaymentScenario.supportedValues())
                ));
    }

    @GetMapping("/payment/active")
    public ResponseEntity<Map<String, String>> getActiveScenario() {
        String active = paymentMockService.getActiveScenario()
                .map(PaymentScenario::toSlug)
                .orElse("none");
        return ResponseEntity.ok(Map.of("active", active));
    }

    @GetMapping("/payment/requests")
    public ResponseEntity<Map<String, Object>> getPaymentRequests() {
        List<Map<String, Object>> requests = paymentMockService.getPaymentRequests();
        return ResponseEntity.ok(Map.of("requests", requests));
    }

    @GetMapping("/payment/count")
    public ResponseEntity<Map<String, Integer>> getPaymentRequestCount() {
        return ResponseEntity.ok(Map.of("count", paymentMockService.getPaymentRequestCount()));
    }

    @DeleteMapping("/requests")
    public ResponseEntity<Map<String, Boolean>> clearRequestHistory() {
        paymentMockService.clearRequests();
        return ResponseEntity.ok(Map.of("cleared", true));
    }

    @DeleteMapping("/reset")
    public ResponseEntity<Map<String, String>> resetAllStubs() {
        paymentMockService.resetAll();
        return ResponseEntity.ok(Map.of("active", "none"));
    }
}
