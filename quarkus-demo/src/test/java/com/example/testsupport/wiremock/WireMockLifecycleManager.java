package com.example.testsupport.wiremock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

import jakarta.ws.rs.core.UriBuilder;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.eclipse.microprofile.config.ConfigProvider.getConfig;

/**
 * Quarkus test resource that manages a WireMock server lifecycle.
 * Starts WireMock on a dynamic port and injects the base URL into
 * Quarkus configuration via {@code wiremock.server.base-url}.
 */
public class WireMockLifecycleManager implements QuarkusTestResourceLifecycleManager {

    private WireMockServer wireMockServer;

    @Override
    public Map<String, String> start() {
        boolean consoleLogging = getConfig()
                .getOptionalValue("test.wiremock-console", Boolean.class)
                .orElse(false);

        wireMockServer = new WireMockServer(options()
                .notifier(new ConsoleNotifier(consoleLogging))
                .dynamicPort());
        wireMockServer.start();
        RestMock.setWireMockServer(wireMockServer);

        String wiremockEndpoint = UriBuilder.fromUri("http://localhost")
                .port(wireMockServer.port())
                .build()
                .toString();

        return Map.of(
                "wiremock.server.base-url", wiremockEndpoint,
                "quarkus.rest-client.nutrition-api.url", wiremockEndpoint
        );
    }

    @Override
    public void stop() {
        wireMockServer.stop();
        RestMock.setWireMockServer(null);
    }

    @Override
    public void inject(TestInjector testInjector) {
        testInjector.injectIntoFields(wireMockServer.port(),
                new TestInjector.AnnotatedAndMatchesType(WireMockPort.class, Integer.TYPE));
    }

    /**
     * Annotation for injecting the WireMock server port into test fields.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface WireMockPort {
    }
}
