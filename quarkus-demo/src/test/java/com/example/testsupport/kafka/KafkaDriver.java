package com.example.testsupport.kafka;

import java.util.List;
import java.util.function.Predicate;

import static org.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Test utility for awaiting specific Kafka messages with predicate matching.
 */
public class KafkaDriver {

    private KafkaDriver() {
    }

    /**
     * Waits until a message matching the given predicate appears on the topic.
     * Polls the consumer with exponential backoff up to a configurable timeout.
     *
     * @return the first matching message
     * @throws org.awaitility.core.ConditionTimeoutException if no match within timeout
     */
    public static String awaitMessage(KafkaMessageConsumer consumer, String topic,
                                      Predicate<String> messagePredicate) {
        var result = new String[1];

        await().atMost(10, SECONDS)
                .pollInterval(1, SECONDS)
                .untilAsserted(() -> {
                    List<String> messages = consumer.getMessages(topic);
                    var match = messages.stream()
                            .filter(messagePredicate)
                            .findFirst()
                            .orElse(null);
                    if (match == null) {
                        throw new AssertionError(
                                "No matching message found on topic '" + topic
                                        + "'. Messages so far: " + messages);
                    }
                    result[0] = match;
                });

        return result[0];
    }

    /**
     * Predicate that checks if a message contains the expected substring.
     */
    public static Predicate<String> messageContains(String expected) {
        return message -> message != null && message.contains(expected);
    }
}
