package com.example.testsupport.wiremock;

import java.util.Objects;

/**
 * Builder for WireMock stateful scenario settings.
 *
 * @see <a href="https://wiremock.org/docs/stateful-behaviour/">WireMock Scenarios</a>
 */
public class StubSettings {

    private final String scenarioName;
    private final String requiredScenarioState;
    private final String newScenarioState;

    public static Builder builder() {
        return new Builder();
    }

    public StubSettings(String scenarioName, String requiredScenarioState, String newScenarioState) {
        this.scenarioName = scenarioName;
        this.requiredScenarioState = requiredScenarioState;
        this.newScenarioState = newScenarioState;
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public String getRequiredScenarioState() {
        return requiredScenarioState;
    }

    public String getNewScenarioState() {
        return newScenarioState;
    }

    public static class Builder {
        private String scenarioName;
        private String requiredScenarioState;
        private String newScenarioState;

        public Builder inScenario(String scenarioName) {
            Objects.requireNonNull(scenarioName, "Scenario name must not be null");
            this.scenarioName = scenarioName;
            return this;
        }

        public Builder whenScenarioStateIs(String requiredScenarioState) {
            Objects.requireNonNull(requiredScenarioState, "Required scenario state must not be null");
            this.requiredScenarioState = requiredScenarioState;
            return this;
        }

        public Builder willSetStateTo(String newScenarioState) {
            Objects.requireNonNull(newScenarioState, "New scenario state must not be null");
            this.newScenarioState = newScenarioState;
            return this;
        }

        public StubSettings build() {
            return new StubSettings(scenarioName, requiredScenarioState, newScenarioState);
        }
    }
}
