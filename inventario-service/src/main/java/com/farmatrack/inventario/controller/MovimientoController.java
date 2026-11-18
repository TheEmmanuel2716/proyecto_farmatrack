package com.farmatrack.inventario.controller;
import com.farmatrack.inventario.dto.*;
import com.farmatrack.inventario.service.MovimientoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
@RestController @RequestMapping("/lotes/{loteId}") @RequiredArgsConstructor @Validated
public class MovimientoController {
    private final MovimientoService service;
    @GetMapping("/movimientos") public PageResponse<MovimientoResponse> list(@PathVariable Long loteId, @RequestParam(defaultValue="0") @Min(0) int page, @RequestParam(defaultValue="20") @Min(1) @Max(100) int size) {
        return PageResponse.from(service.findByLote(loteId, PageRequest.of(page, size)));
    }
    @PostMapping("/ventas") public ResponseEntity<MovimientoResponse> sell(@PathVariable Long loteId, @Valid @RequestBody MovimientoInput i) { return ResponseEntity.status(201).body(service.sell(loteId, i)); }
    @PostMapping("/devoluciones") public ResponseEntity<MovimientoResponse> refund(@PathVariable Long loteId, @Valid @RequestBody MovimientoInput i) { return ResponseEntity.status(201).body(service.returnToSupplier(loteId, i)); }
}
