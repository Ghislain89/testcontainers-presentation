package com.example.testsupport.kafka;

import java.util.Map;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import io.quarkus.test.kafka.KafkaCompanionResource;

/**
 * Test resource extending {@link KafkaCompanionResource} that provides
 * injectable {@link KafkaMessageConsumer} and {@link KafkaMessageProducer} beans.
 */
public class KafkaMessagingResource extends KafkaCompanionResource {

    private KafkaMessageConsumer kafkaMessageConsumer;
    private KafkaMessageProducer kafkaMessageProducer;

    @Override
    public Map<String, String> start() {
        Map<String, String> props = super.start();
        this.kafkaMessageConsumer = new KafkaMessageConsumer(this.kafkaCompanion);
        this.kafkaMessageProducer = new KafkaMessageProducer(this.kafkaCompanion);
        return props;
    }

    @Override
    public void stop() {
        if (kafkaMessageConsumer != null) {
            kafkaMessageConsumer.shutdown();
        }
        super.stop();
    }

    @Override
    public void inject(QuarkusTestResourceLifecycleManager.TestInjector testInjector) {
        super.inject(testInjector);
        testInjector.injectIntoFields(this.kafkaMessageConsumer,
                new QuarkusTestResourceLifecycleManager.TestInjector.AnnotatedAndMatchesType(
                        InjectKafkaMessageConsumer.class, KafkaMessageConsumer.class));
        testInjector.injectIntoFields(this.kafkaMessageProducer,
                new QuarkusTestResourceLifecycleManager.TestInjector.AnnotatedAndMatchesType(
                        InjectKafkaMessageProducer.class, KafkaMessageProducer.class));
    }
}
