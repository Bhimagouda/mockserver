package org.testing.mockserver.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class MockResponseLoader {

    private static final String BASE_PATH = "mock-responses/";

    public String load(String relativePath) {
        String fullPath = BASE_PATH + relativePath;
        try {
            ClassPathResource resource = new ClassPathResource(fullPath);
            String body = resource.getContentAsString(StandardCharsets.UTF_8);
            log.debug("Loaded mock response from {}", fullPath);
            return body;
        } catch (IOException e) {
            throw new IllegalStateException("Mock response file not found: " + fullPath, e);
        }
    }
}
