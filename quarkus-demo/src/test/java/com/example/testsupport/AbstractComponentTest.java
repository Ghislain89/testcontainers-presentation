package com.example.testsupport;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

/**
 * Base class for component tests. Manages {@link TestCase} lifecycle
 * so that BDD steps (given/when/then) are tracked and logged automatically.
 */
public abstract class AbstractComponentTest {

    @BeforeEach
    void startTestCase(TestInfo testInfo) {
        TestCase.start(testInfo.getDisplayName());
    }

    @AfterEach
    void endTestCase(TestInfo testInfo) {
        TestCase.ExecutionLogEntry executionLog = TestCase.end();
        afterTestCase(testInfo, executionLog);
    }

    /**
     * Hook for subclasses to perform additional actions after each test case.
     */
    protected void afterTestCase(TestInfo testInfo, TestCase.ExecutionLogEntry executionLog) {
        // Override in subclasses for custom post-test actions
    }
}
