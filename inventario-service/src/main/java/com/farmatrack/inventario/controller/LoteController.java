package com.farmatrack.inventario.controller;
import com.farmatrack.inventario.dto.*;
import com.farmatrack.inventario.service.LoteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
@RestController @RequestMapping("/") @RequiredArgsConstructor @Validated
public class LoteController {
    private final LoteService service;
    @GetMapping("/lotes") public PageResponse<LoteResponse> list(@RequestParam(defaultValue="0") @Min(0) int page, @RequestParam(defaultValue="20") @Min(1) @Max(100) int size) {
        return PageResponse.from(service.findAll(PageRequest.of(page, size)));
    }
    @GetMapping("/lotes/{loteId}") public LoteResponse get(@PathVariable Long loteId) { return service.findById(loteId); }
    @PostMapping("/productos/{productoId}/lotes")
    public ResponseEntity<LoteResponse> create(@PathVariable Long productoId, @Valid @RequestBody LoteInput i) { return ResponseEntity.status(201).body(service.create(productoId, i)); }
}
