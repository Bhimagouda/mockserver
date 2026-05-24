package org.testing.mockserver.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.testing.mockserver.util.MockResponseLoader;

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

    public void registerSuccess() {
        wireMockServer.stubFor(
                post(urlEqualTo(PAYMENT_URL))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader(CONTENT_TYPE, APP_JSON)
                                .withBody(responseLoader.load("payment/success.json")))
        );
        log.info("Stub registered: POST {} -> 200 SUCCESS", PAYMENT_URL);
    }

    public void registerFailure() {
        wireMockServer.stubFor(
                post(urlEqualTo(PAYMENT_URL))
                        .willReturn(aResponse()
                                .withStatus(500)
                                .withHeader(CONTENT_TYPE, APP_JSON)
                                .withBody(responseLoader.load("payment/failure.json")))
        );
        log.info("Stub registered: POST {} -> 500 FAILED", PAYMENT_URL);
    }

    public void registerTimeout() {
        wireMockServer.stubFor(
                post(urlEqualTo(PAYMENT_URL))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader(CONTENT_TYPE, APP_JSON)
                                .withFixedDelay(10_000)
                                .withBody(responseLoader.load("payment/success.json")))
        );
        log.info("Stub registered: POST {} -> 200 with 10s delay (timeout simulation)", PAYMENT_URL);
    }

    public void registerUnauthorized() {
        wireMockServer.stubFor(
                post(urlEqualTo(PAYMENT_URL))
                        .willReturn(aResponse()
                                .withStatus(401)
                                .withHeader(CONTENT_TYPE, APP_JSON)
                                .withBody(responseLoader.load("payment/unauthorized.json")))
        );
        log.info("Stub registered: POST {} -> 401 UNAUTHORIZED", PAYMENT_URL);
    }

    public void registerRateLimit() {
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

    public void resetAll() {
        wireMockServer.resetAll();
        log.info("All WireMock stubs reset");
    }
}
