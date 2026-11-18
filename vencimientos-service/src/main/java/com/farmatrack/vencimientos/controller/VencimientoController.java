package com.farmatrack.vencimientos.controller;

import com.farmatrack.vencimientos.dto.LoteEstadoResponse;
import com.farmatrack.vencimientos.service.VencimientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/lotes")
@RequiredArgsConstructor
public class VencimientoController {
    private final VencimientoService service;

    @GetMapping("/{loteId}/estado")
    public LoteEstadoResponse getEstado(@PathVariable Long loteId) {
        return service.findEstado(loteId);
    }
}
