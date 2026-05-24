package org.testing.mockserver.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MockServerConfig implements SmartLifecycle {

    @Value("${wiremock.port:9091}")
    private int wireMockPort;

    private WireMockServer wireMockServer;
    private volatile boolean running = false;

    @Override
    public void start() {
        wireMockServer = new WireMockServer(
                WireMockConfiguration.wireMockConfig().port(wireMockPort)
        );
        wireMockServer.start();
        running = true;
        log.info("WireMock server started on port {}", wireMockPort);
    }

    @Override
    public void stop() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
            running = false;
            log.info("WireMock server stopped");
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    public WireMockServer getWireMockServer() {
        return wireMockServer;
    }
}
