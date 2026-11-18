package com.farmatrack.inventario.event;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaEventPublisher {
    public static final String TOPIC = "inventario-eventos";
    private final KafkaTemplate<Long, Object> kafkaTemplate;
    public void publish(Long loteId, Object event) {
        kafkaTemplate.send(TOPIC, loteId, event);
    }
}
