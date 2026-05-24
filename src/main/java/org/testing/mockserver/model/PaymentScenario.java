package org.testing.mockserver.model;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public enum PaymentScenario {
    SUCCESS, FAILURE, TIMEOUT, UNAUTHORIZED, RATE_LIMIT;

    public static Optional<PaymentScenario> from(String value) {
        try {
            return Optional.of(valueOf(value.toUpperCase().replace("-", "_")));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public static String supportedValues() {
        return Arrays.stream(values())
                .map(PaymentScenario::toSlug)
                .collect(Collectors.joining(", "));
    }

    public String toSlug() {
        return name().toLowerCase().replace("_", "-");
    }
}
