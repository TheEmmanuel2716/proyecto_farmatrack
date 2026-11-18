package com.farmatrack.vencimientos.controller;

import com.farmatrack.vencimientos.dto.*;
import com.farmatrack.vencimientos.service.VencimientoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@RestController
@RequestMapping("/alertas")
@RequiredArgsConstructor
@Validated
public class AlertaController {
    private final VencimientoService service;

    @GetMapping
    public List<AlertaResponse> list(
            @RequestParam(required = false) Boolean atendida,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.findAlerts(atendida, PageRequest.of(page, size)).getContent();
    }

    @GetMapping("/{alertaId}")
    public AlertaResponse get(@PathVariable Long alertaId) {
        return service.findAlert(alertaId);
    }

    @PatchMapping("/{alertaId}")
    public AlertaResponse markAttended(@PathVariable Long alertaId,
                                       @Valid @RequestBody AtenderAlertaRequest request) {
        return service.markAlert(alertaId, request.atendida());
    }
}
