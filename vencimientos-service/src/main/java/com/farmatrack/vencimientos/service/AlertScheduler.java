package com.farmatrack.vencimientos.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AlertScheduler {
    private final VencimientoService service;

    @Scheduled(cron = "${vencimientos.alertas.cron:0 0 * * * *}")
    public void processAlerts() {
        service.processScheduledAlerts();
    }
}
