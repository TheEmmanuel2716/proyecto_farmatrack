package com.farmatrack.inventario.config;
import org.apache.kafka.common.serialization.LongSerializer;
import org.springframework.context.annotation.*;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import java.util.*;
@Configuration
public class KafkaProducerConfig {
    @Bean
    ProducerFactory<Long, Object> producerFactory(org.springframework.boot.kafka.autoconfigure.KafkaProperties properties) {
        Map<String, Object> config = new HashMap<>(properties.buildProducerProperties());
        config.put(org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        config.put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(config);
    }
    @Bean KafkaTemplate<Long, Object> kafkaTemplate(ProducerFactory<Long, Object> factory) { return new KafkaTemplate<>(factory); }
}
