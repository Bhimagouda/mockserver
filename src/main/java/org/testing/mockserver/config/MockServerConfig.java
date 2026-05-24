package org.testing.mockserver.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MockServerConfig implements SmartLifecycle {

    private final WireMockServer wireMockServer;

    public MockServerConfig(@Value("${wiremock.port:9091}") int wireMockPort) {
        this.wireMockServer = new WireMockServer(
                WireMockConfiguration.wireMockConfig().port(wireMockPort)
        );
    }

    @Bean
    public WireMockServer wireMockServer() {
        return wireMockServer;
    }

    @Override
    public void start() {
        wireMockServer.start();
        log.info("WireMock server started on port {}", wireMockServer.port());
    }

    @Override
    public void stop() {
        if (wireMockServer.isRunning()) {
            wireMockServer.stop();
            log.info("WireMock server stopped");
        }
    }

    @Override
    public boolean isRunning() {
        return wireMockServer.isRunning();
    }
}
