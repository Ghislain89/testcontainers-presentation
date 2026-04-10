package com.example.testsupport.wiremock;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import io.quarkus.test.junit.callback.QuarkusTestAfterEachCallback;
import io.quarkus.test.junit.callback.QuarkusTestMethodContext;

/**
 * Quarkus test callback that warns about unused WireMock stubs and
 * unmatched requests after each test. Helps catch mocking issues early.
 */
public class MockUsageWarningExtension implements QuarkusTestAfterEachCallback {

    private static final Logger LOG = LoggerFactory.getLogger(MockUsageWarningExtension.class);

    @Override
    public void afterEach(QuarkusTestMethodContext context) {
        if (!RestMock.isActive()) {
            return;
        }
        String testMethod = context.getTestMethod().toGenericString();

        List<StubMapping> unusedStubs = RestMock.getUnusedStubMappings();
        unusedStubs.forEach(unusedStub ->
                LOG.warn("Unused stub mapping in test method `{}`: {}", testMethod, unusedStub));

        List<ServeEvent> unmatchedRequests = RestMock.getUnmatchedServeEvents();
        unmatchedRequests.forEach(event ->
                LOG.warn("In test `{}` a request was not matched with a stub. Request: {}",
                        testMethod, event.getRequest()));
    }
}
