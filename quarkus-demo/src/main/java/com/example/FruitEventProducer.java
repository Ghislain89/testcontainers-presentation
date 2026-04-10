package com.example;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Produces fruit domain events to Kafka.
 */
@ApplicationScoped
public class FruitEventProducer {

    private static final Logger LOG = LoggerFactory.getLogger(FruitEventProducer.class);

    @Inject
    ObjectMapper objectMapper;

    @Inject
    @Channel("fruit-events-out")
    Emitter<String> emitter;

    public void send(FruitEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            emitter.send(json);
            LOG.info("Published event: {}", json);
        } catch (JsonProcessingException e) {
            LOG.error("Failed to serialize fruit event", e);
        }
    }
}
