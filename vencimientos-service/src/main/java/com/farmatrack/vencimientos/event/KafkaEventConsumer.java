package com.farmatrack.vencimientos.event;

import com.farmatrack.vencimientos.service.VencimientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaEventConsumer {
    private final VencimientoService service;

    @KafkaListener(topics = "${spring.kafka.topic:inventario-eventos}",
            groupId = "${spring.kafka.consumer.group-id:vencimientos-service}",
            containerFactory = "kafkaListenerContainerFactory")
    public void consume(String payload) {
        service.processEvent(payload);
    }
}
