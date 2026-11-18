package com.farmatrack.inventario.controller;
import com.farmatrack.inventario.dto.*;
import com.farmatrack.inventario.service.ProveedorService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
@RestController @RequestMapping("/proveedores") @RequiredArgsConstructor @Validated
public class ProveedorController {
    private final ProveedorService service;
    @PostMapping public ResponseEntity<ProveedorResponse> create(@Valid @RequestBody ProveedorInput i) { return ResponseEntity.status(201).body(service.create(i)); }
    @PutMapping("/{proveedorId}")
    public ProveedorResponse update(@PathVariable Long proveedorId, @Valid @RequestBody ProveedorInput i) { return service.update(proveedorId, i); }
    @GetMapping public PageResponse<ProveedorResponse> list(@RequestParam(defaultValue="0") @Min(0) int page, @RequestParam(defaultValue="20") @Min(1) @Max(100) int size) {
        return PageResponse.from(service.findAll(PageRequest.of(page, size)));
    }
}
