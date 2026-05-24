package org.testing.mockserver.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.testing.mockserver.model.PaymentScenario;
import org.testing.mockserver.util.MockResponseLoader;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentMockService {

    private static final String PAYMENT_URL  = "/bank/payment";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APP_JSON     = "application/json";

    private final WireMockServer wireMockServer;
    private final MockResponseLoader responseLoader;

    private final AtomicReference<PaymentScenario> activeScenario = new AtomicReference<>();

    public PaymentScenario activate(PaymentScenario scenario) {
        wireMockServer.resetAll();
        activeScenario.set(scenario);
        switch (scenario) {
            case SUCCESS      -> registerSuccess();
            case FAILURE      -> registerFailure();
            case TIMEOUT      -> registerTimeout();
            case UNAUTHORIZED -> registerUnauthorized();
            case RATE_LIMIT   -> registerRateLimit();
        }
        log.info("Active payment scenario switched to: {}", scenario.toSlug());
        return scenario;
    }

    public Optional<PaymentScenario> getActiveScenario() {
        return Optional.ofNullable(activeScenario.get());
    }

    public void resetAll() {
        wireMockServer.resetAll();
        activeScenario.set(null);
        log.info("All WireMock stubs reset");
    }

    public List<Map<String, Object>> getPaymentRequests() {
        return wireMockServer.findAll(postRequestedFor(urlEqualTo(PAYMENT_URL)))
                .stream()
                .map(req -> {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("method", req.getMethod().getName());
                    entry.put("url", req.getUrl());
                    entry.put("body", req.getBodyAsString());
                    Map<String, String> headers = new LinkedHashMap<>();
                    req.getHeaders().all().forEach(h -> headers.put(h.key(), h.firstValue()));
                    entry.put("headers", headers);
                    return entry;
                })
                .toList();
    }

    public int getPaymentRequestCount() {
        return wireMockServer.findAll(postRequestedFor(urlEqualTo(PAYMENT_URL))).size();
    }

    public void clearRequests() {
        wireMockServer.resetRequests();
        log.info("Request history cleared");
    }

    private void registerSuccess() {
        wireMockServer.stubFor(
                post(urlEqualTo(PAYMENT_URL))
                        .withRequestBody(matchingJsonPath("$[?(@.amount > 0)]"))
                        .atPriority(3)
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader(CONTENT_TYPE, APP_JSON)
                                .withBody(responseLoader.load("payment/success.json")))
        );
        log.info("Stub registered: POST {} (amount > 0) -> 200 SUCCESS", PAYMENT_URL);
    }

    private void registerFailure() {
        wireMockServer.stubFor(
                post(urlEqualTo(PAYMENT_URL))
                        .withRequestBody(matchingJsonPath("$[?(@.amount == 0)]"))
                        .atPriority(2)
                        .willReturn(aResponse()
                                .withStatus(500)
                                .withHeader(CONTENT_TYPE, APP_JSON)
                                .withBody(responseLoader.load("payment/failure.json")))
        );
        log.info("Stub registered: POST {} (amount == 0) -> 500 FAILED", PAYMENT_URL);
    }

    private void registerTimeout() {
        wireMockServer.stubFor(
                post(urlEqualTo(PAYMENT_URL))
                        .withRequestBody(matchingJsonPath("$[?(@.amount > 0)]"))
                        .atPriority(3)
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader(CONTENT_TYPE, APP_JSON)
                                .withFixedDelay(10_000)
                                .withBody(responseLoader.load("payment/success.json")))
        );
        log.info("Stub registered: POST {} (amount > 0) -> 200 with 10s delay (timeout simulation)", PAYMENT_URL);
    }

    private void registerUnauthorized() {
        wireMockServer.stubFor(
                post(urlEqualTo(PAYMENT_URL))
                        .withHeader("Authorization", absent())
                        .atPriority(1)
                        .willReturn(aResponse()
                                .withStatus(401)
                                .withHeader(CONTENT_TYPE, APP_JSON)
                                .withBody(responseLoader.load("payment/unauthorized.json")))
        );
        wireMockServer.stubFor(
                post(urlEqualTo(PAYMENT_URL))
                        .withHeader("Authorization", not(matching("Bearer .+")))
                        .atPriority(1)
                        .willReturn(aResponse()
                                .withStatus(401)
                                .withHeader(CONTENT_TYPE, APP_JSON)
                                .withBody(responseLoader.load("payment/unauthorized.json")))
        );
        log.info("Stub registered: POST {} (absent or invalid Authorization) -> 401 UNAUTHORIZED", PAYMENT_URL);
    }

    private void registerRateLimit() {
        wireMockServer.stubFor(
                post(urlEqualTo(PAYMENT_URL))
                        .willReturn(aResponse()
                                .withStatus(429)
                                .withHeader(CONTENT_TYPE, APP_JSON)
                                .withHeader("Retry-After", "60")
                                .withBody(responseLoader.load("payment/rate-limit.json")))
        );
        log.info("Stub registered: POST {} -> 429 RATE_LIMITED", PAYMENT_URL);
    }
}
