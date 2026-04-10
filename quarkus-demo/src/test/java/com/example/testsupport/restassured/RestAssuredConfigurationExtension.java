package com.example.testsupport.restassured;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.eclipse.microprofile.config.ConfigProvider;

import io.quarkus.test.junit.callback.QuarkusTestAfterConstructCallback;
import io.restassured.RestAssured;
import io.restassured.config.JsonConfig;
import io.restassured.config.LogConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.path.json.config.JsonPathConfig;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

/**
 * Auto-discovered Quarkus test callback that configures RestAssured:
 * <ul>
 *   <li>JSON number parsing uses {@code BIG_DECIMAL} for precision</li>
 *   <li>Logging mode configurable via {@code test.restassured.logging}:
 *       off, on-failure (default), all</li>
 * </ul>
 */
public class RestAssuredConfigurationExtension implements QuarkusTestAfterConstructCallback {

    static final String LOGGING_MODE_CONFIG_KEY = "test.restassured.logging";

    private final Filter requestLoggingFilter = new ManagedRequestLoggingFilter();
    private final Filter responseLoggingFilter = new ManagedResponseLoggingFilter();

    @Override
    @SuppressWarnings("java:S2696")
    public void afterConstruct(Object testInstance) {
        LoggingMode loggingMode = LoggingMode.fromConfig(
                ConfigProvider.getConfig().getOptionalValue(LOGGING_MODE_CONFIG_KEY, String.class));

        RestAssured.config = RestAssuredConfig.config()
                .jsonConfig(JsonConfig.jsonConfig()
                        .numberReturnType(JsonPathConfig.NumberReturnType.BIG_DECIMAL))
                .logConfig(createLogConfig(loggingMode));

        applyLoggingFilters(loggingMode);
    }

    private LogConfig createLogConfig(LoggingMode loggingMode) {
        LogConfig logConfig = LogConfig.logConfig().enablePrettyPrinting(true);
        if (loggingMode == LoggingMode.ON_FAILURE) {
            return logConfig.enableLoggingOfRequestAndResponseIfValidationFails();
        }
        return logConfig;
    }

    private void applyLoggingFilters(LoggingMode loggingMode) {
        List<Filter> filters = new ArrayList<>(RestAssured.filters().stream()
                .filter(filter -> !(filter instanceof ManagedLoggingFilter))
                .toList());

        if (loggingMode == LoggingMode.ALL) {
            filters.add(requestLoggingFilter);
            filters.add(responseLoggingFilter);
        }

        RestAssured.replaceFiltersWith(filters);
    }

    private interface ManagedLoggingFilter {
    }

    private static final class ManagedRequestLoggingFilter extends RequestLoggingFilter
            implements ManagedLoggingFilter {
    }

    private static final class ManagedResponseLoggingFilter extends ResponseLoggingFilter
            implements ManagedLoggingFilter {
    }

    enum LoggingMode {
        OFF("off"),
        ON_FAILURE("on-failure"),
        ALL("all");

        private final String configValue;

        LoggingMode(String configValue) {
            this.configValue = configValue;
        }

        static LoggingMode fromConfig(Optional<String> rawValue) {
            if (rawValue.isEmpty()) {
                return ON_FAILURE;
            }
            String normalised = rawValue.get().trim().toLowerCase(Locale.ROOT);
            for (LoggingMode mode : values()) {
                if (mode.configValue.equals(normalised)) {
                    return mode;
                }
            }
            throw new IllegalStateException(
                    "Unsupported value for " + LOGGING_MODE_CONFIG_KEY + ": '" + rawValue.get()
                            + "'. Supported values are: off, on-failure, all.");
        }
    }
}
