package com.example.testsupport.kafka;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.test.kafka.KafkaCompanionResource;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;

/**
 * Test utility that collects messages from Kafka topics in the background.
 * Messages accumulate and can be retrieved at any point during the test.
 */
public class KafkaMessageConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaMessageConsumer.class);

    private final KafkaCompanion companion;
    private final Map<String, List<String>> messagesByTopic = new ConcurrentHashMap<>();
    private final Map<String, Thread> collectors = new ConcurrentHashMap<>();
    private volatile boolean running = true;

    public KafkaMessageConsumer(KafkaCompanion companion) {
        this.companion = companion;
    }

    /**
     * Returns all messages received on the given topic as raw strings.
     * Starts a background collector on first access for each topic.
     */
    public List<String> getMessages(String topic) {
        ensureCollector(topic);
        return List.copyOf(messagesByTopic.getOrDefault(topic, List.of()));
    }

    private void ensureCollector(String topic) {
        collectors.computeIfAbsent(topic, t -> {
            messagesByTopic.putIfAbsent(t, new CopyOnWriteArrayList<>());
            Thread collector = new Thread(() -> collectMessages(t), "kafka-collector-" + t);
            collector.setDaemon(true);
            collector.start();
            return collector;
        });
    }

    private void collectMessages(String topic) {
        try (KafkaConsumer<String, String> consumer = createConsumer()) {
            var partition = new TopicPartition(topic, 0);
            consumer.assign(List.of(partition));
            consumer.seekToBeginning(List.of(partition));

            while (running) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                for (ConsumerRecord<String, String> record : records) {
                    messagesByTopic.get(topic).add(record.value());
                    LOG.debug("Collected message from topic '{}': {}", topic, record.value());
                }
            }
        } catch (Exception e) {
            if (running) {
                LOG.error("Error collecting messages from topic '{}'", topic, e);
            }
        }
    }

    private KafkaConsumer<String, String> createConsumer() {
        return new KafkaConsumer<>(Map.of(
                "bootstrap.servers", companion.getBootstrapServers(),
                "group.id", "test-consumer-" + System.nanoTime(),
                "key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer",
                "value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer",
                "auto.offset.reset", "earliest"
        ));
    }

    public void shutdown() {
        running = false;
        collectors.values().forEach(Thread::interrupt);
    }
}
