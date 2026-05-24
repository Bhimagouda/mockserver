package org.testing.mockserver.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@RestController
public class CheckoutController {

    @Value("${wiremock.port:9091}")
    private int wireMockPort;

    @PostMapping("/checkout")
    public ResponseEntity<String> checkout(
            @RequestHeader(value = "Authorization", required = false) String auth,
            HttpServletRequest request) throws Exception {

        byte[] body = request.getInputStream().readAllBytes();

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + wireMockPort + "/bank/payment"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofByteArray(body));

        if (auth != null) {
            builder.header("Authorization", auth);
        }

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(builder.build(), HttpResponse.BodyHandlers.ofString());

        return ResponseEntity.status(response.statusCode()).body(response.body());
    }
}
