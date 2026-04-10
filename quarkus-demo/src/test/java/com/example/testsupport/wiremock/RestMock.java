package com.example.testsupport.wiremock;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.CountMatchingStrategy;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

/**
 * Static utility wrapping a {@link WireMockServer} for simplified
 * stubbing and verification in tests. Must be used with
 * {@link WireMockLifecycleManager} which manages the server lifecycle.
 *
 * <p>Parallel test execution is not supported — the WireMock server
 * is shared across all tests.</p>
 */
public class RestMock {

    private static WireMockServer wireMockServer;
    private static WireMock wireMock;
    private static List<RegisteredStub> registeredStubs;

    private RestMock() {
    }

    // --- Request pattern builders ---

    public static RequestPatternBuilder get(UrlPattern urlPattern) {
        return WireMock.getRequestedFor(urlPattern);
    }

    public static RequestPatternBuilder get(String url) {
        return WireMock.getRequestedFor(urlEqualTo(url));
    }

    public static RequestPatternBuilder post(UrlPattern urlPattern) {
        return WireMock.postRequestedFor(urlPattern);
    }

    public static RequestPatternBuilder post(String url) {
        return WireMock.postRequestedFor(urlEqualTo(url));
    }

    public static RequestPatternBuilder put(UrlPattern urlPattern) {
        return WireMock.putRequestedFor(urlPattern);
    }

    public static RequestPatternBuilder put(String url) {
        return WireMock.putRequestedFor(urlEqualTo(url));
    }

    public static RequestPatternBuilder delete(UrlPattern urlPattern) {
        return WireMock.deleteRequestedFor(urlPattern);
    }

    public static RequestPatternBuilder delete(String url) {
        return WireMock.deleteRequestedFor(urlEqualTo(url));
    }

    public static RequestPatternBuilder patch(UrlPattern urlPattern) {
        return WireMock.patchRequestedFor(urlPattern);
    }

    public static RequestPatternBuilder patch(String url) {
        return WireMock.patchRequestedFor(urlEqualTo(url));
    }

    // --- Stub registration ---

    public static void stubFor(RequestPatternBuilder requestPatternBuilder,
                               ResponseDefinitionBuilder responseDefinitionBuilder) {
        stubFor(requestPatternBuilder, responseDefinitionBuilder, null);
    }

    public static void stubFor(RequestPatternBuilder requestPatternBuilder,
                               ResponseDefinitionBuilder responseDefinitionBuilder,
                               StubSettings.Builder stubSettingsBuilder) {
        RequestPattern requestPattern = requestPatternBuilder.build();
        ResponseDefinition responseDefinition = responseDefinitionBuilder.build();
        StubMapping stubMapping = new StubMapping(requestPattern, responseDefinition);

        Optional.ofNullable(stubSettingsBuilder)
                .map(StubSettings.Builder::build)
                .ifPresent(settings -> {
                    Optional.ofNullable(settings.getScenarioName()).ifPresent(stubMapping::setScenarioName);
                    Optional.ofNullable(settings.getRequiredScenarioState()).ifPresent(stubMapping::setRequiredScenarioState);
                    Optional.ofNullable(settings.getNewScenarioState()).ifPresent(stubMapping::setNewScenarioState);
                });

        wireMock.register(stubMapping);
        registeredStubs.add(new RegisteredStub(requestPattern, stubMapping));
    }

    public static void removeStub(RequestPatternBuilder requestPatternBuilder) {
        RequestPattern requestPattern = requestPatternBuilder.build();
        Iterator<RegisteredStub> stubIterator = registeredStubs.iterator();
        while (stubIterator.hasNext()) {
            RegisteredStub registeredStub = stubIterator.next();
            if (registeredStub.requestPattern().equals(requestPattern)) {
                wireMock.removeStubMapping(registeredStub.stubMapping());
                stubIterator.remove();
            }
        }
    }

    // --- Verification ---

    public static void verify(RequestPatternBuilder requestPatternBuilder) {
        wireMock.verifyThat(requestPatternBuilder);
    }

    public static void verify(int count, RequestPatternBuilder requestPatternBuilder) {
        wireMock.verifyThat(count, requestPatternBuilder);
    }

    public static void verify(CountMatchingStrategy countMatchingStrategy,
                              RequestPatternBuilder requestPatternBuilder) {
        wireMock.verifyThat(countMatchingStrategy, requestPatternBuilder);
    }

    // --- Utilities ---

    static boolean isActive() {
        return wireMockServer != null;
    }

    public static void resetRestMock() {
        registeredStubs.clear();
        wireMock.resetMappings();
    }

    public static List<StubMapping> getUnusedStubMappings() {
        return wireMockServer.findUnmatchedStubs().getMappings();
    }

    public static List<ServeEvent> getUnmatchedServeEvents() {
        return wireMock.getServeEvents()
                .stream()
                .filter(serveEvent -> !serveEvent.getWasMatched())
                .toList();
    }

    public static List<ServeEvent> getAllServeEvents() {
        return wireMock.getServeEvents().stream().toList();
    }

    public static List<LoggedRequest> findAll(RequestPatternBuilder requestPatternBuilder) {
        return wireMock.find(requestPatternBuilder);
    }

    static void setWireMockServer(WireMockServer wireMockServer) {
        RestMock.wireMockServer = wireMockServer;
        RestMock.wireMock = wireMockServer != null ? new WireMock(wireMockServer.port()) : null;
        registeredStubs = new LinkedList<>();
        if (wireMockServer != null) {
            configureFor(wireMockServer.port());
            RestMock.wireMock.resetMappings();
        }
    }

    private record RegisteredStub(RequestPattern requestPattern, StubMapping stubMapping) {
    }
}
