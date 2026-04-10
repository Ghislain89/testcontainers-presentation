package com.example.testsupport;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BDD-style test case utility providing given/when/then steps with
 * structured execution logging. On failure, prints a tree of executed
 * steps with ✓/✘ markers for quick diagnosis.
 */
public class TestCase {

    private static final Logger log = LoggerFactory.getLogger(TestCase.class);

    private static final ThreadLocal<TestCase> CURRENT = new ThreadLocal<>();

    private final ExecutionLogEntry scenarioExecutionLog;
    private ExecutionLogEntry currentExecutionLogEntry;

    public TestCase(String scenarioName) {
        this.scenarioExecutionLog = new ExecutionLogEntry("Scenario " + scenarioName);
        this.currentExecutionLogEntry = this.scenarioExecutionLog;
    }

    public static void start(String scenarioName) {
        if (CURRENT.get() != null) {
            throw new IllegalStateException("A TestCase is already active for this thread.");
        }
        CURRENT.set(new TestCase(scenarioName));
    }

    public static ExecutionLogEntry end() {
        TestCase tc = current();
        tc.scenarioExecutionLog.endMoment = Instant.now();
        CURRENT.remove();
        return tc.scenarioExecutionLog;
    }

    public static void given(String description, Runnable step) {
        runStep("Given  ", description, step);
    }

    public static void and(String description, Runnable step) {
        runStep("and    ", description, step);
    }

    public static void when(String description, Runnable step) {
        runStep("When   ", description, step);
    }

    public static void then(String description, Runnable step) {
        runStep("Then   ", description, step);
    }

    @SuppressWarnings("java:S1181")
    private static void runStep(String keyword, String description, Runnable step) {
        TestCase tc = current();
        String decorated = keyword + description;
        var parentExecutionLogEntry = tc.currentExecutionLogEntry;
        try {
            tc.currentExecutionLogEntry = parentExecutionLogEntry.createChildEntry(decorated);
            log.debug("Starting step: {}", decorated);
            step.run();
            tc.currentExecutionLogEntry.setSuccessfullyEnded();
            log.debug("Finished step: {}", decorated);
        } catch (Throwable t) {
            tc.currentExecutionLogEntry.setNotSuccessfullyEnded(t);
            tc.logTestExecution();
            throw t;
        } finally {
            tc.currentExecutionLogEntry = parentExecutionLogEntry;
        }
    }

    private void logTestExecution() {
        var executionLogText = new StringBuilder();
        addEntryToExecutionLogText(scenarioExecutionLog, 0, executionLogText);
        var failed = !scenarioExecutionLog.isSuccessfullyEnded();
        if (failed) {
            log.error("Test case failed. Execution log:\n{}", executionLogText);
        } else {
            log.info("Test case executed. Execution log:\n{}", executionLogText);
        }
    }

    private void addEntryToExecutionLogText(ExecutionLogEntry entry, int level, StringBuilder executionLogText) {
        var sign = entry.isSuccessfullyEnded() ? "✓" : "✘";
        executionLogText.append("  ".repeat(level));
        executionLogText.append(sign);
        executionLogText.append(" ");
        executionLogText.append(entry.description);
        for (var child : entry.children) {
            executionLogText.append("\n");
            addEntryToExecutionLogText(child, level + 1, executionLogText);
        }
    }

    private static TestCase current() {
        TestCase tc = CURRENT.get();
        if (tc == null) {
            throw new IllegalStateException("No active TestCase – did the extension start it?");
        }
        return tc;
    }

    public static class ExecutionLogEntry {
        private final String description;
        private final Instant startMoment;
        private Instant endMoment;
        private Boolean success;
        private Throwable failure;
        private final List<ExecutionLogEntry> children = new LinkedList<>();

        private ExecutionLogEntry(String description) {
            this.description = description;
            this.startMoment = Instant.now();
        }

        public String getDescription() {
            return description;
        }

        public Instant getStartMoment() {
            return startMoment;
        }

        public Instant getEndMoment() {
            return endMoment;
        }

        public List<ExecutionLogEntry> getChildren() {
            return Collections.unmodifiableList(children);
        }

        public long getDurationMs() {
            return endMoment.toEpochMilli() - startMoment.toEpochMilli();
        }

        public Throwable getFailure() {
            return failure;
        }

        public boolean isSuccessfullyEnded() {
            if (success != null) {
                return success;
            }
            return !children.isEmpty() && children.getLast().isSuccessfullyEnded();
        }

        private ExecutionLogEntry createChildEntry(String description) {
            var child = new ExecutionLogEntry(description);
            children.add(child);
            return child;
        }

        private void setSuccessfullyEnded() {
            endMoment = Instant.now();
            success = true;
        }

        private void setNotSuccessfullyEnded(Throwable cause) {
            endMoment = Instant.now();
            success = false;
            failure = cause;
        }
    }
}
