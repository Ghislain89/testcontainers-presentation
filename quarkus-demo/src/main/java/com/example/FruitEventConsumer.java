package com.example;

import java.util.concurrent.atomic.AtomicInteger;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Consumes fruit domain events from Kafka.
 * Tracks the event count for observability.
 */
@ApplicationScoped
public class FruitEventConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(FruitEventConsumer.class);

    private final AtomicInteger eventCount = new AtomicInteger(0);

    @Incoming("fruit-events-in")
    public void consume(String message) {
        eventCount.incrementAndGet();
        LOG.info("Received fruit event: {}", message);
    }

    public int getEventCount() {
        return eventCount.get();
    }
}
