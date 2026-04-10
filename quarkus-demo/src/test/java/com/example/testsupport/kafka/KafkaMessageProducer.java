package com.example.testsupport.kafka;

import java.time.Duration;
import java.util.UUID;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;

/**
 * Test utility for producing messages to Kafka topics.
 */
public class KafkaMessageProducer {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaMessageProducer.class);

    private final KafkaCompanion companion;

    public KafkaMessageProducer(KafkaCompanion companion) {
        this.companion = companion;
    }

    /**
     * Sends a message to the given topic with auto-generated key and message ID header.
     */
    public void send(String topic, String body) {
        send(topic, UUID.randomUUID().toString(), body);
    }

    /**
     * Sends a message to the given topic with the specified key.
     */
    public void send(String topic, String key, String body) {
        var headers = new RecordHeaders();
        headers.add("message-id", UUID.randomUUID().toString().getBytes());

        var record = new ProducerRecord<>(topic, null, key, body, headers);

        companion.produceStrings()
                .fromRecords(record)
                .awaitCompletion(Duration.ofSeconds(5));

        LOG.debug("Produced message to topic '{}': {}", topic, body);
    }
}
