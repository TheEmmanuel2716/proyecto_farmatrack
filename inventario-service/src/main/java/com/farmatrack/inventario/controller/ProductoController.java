package com.farmatrack.inventario.controller;
import com.farmatrack.inventario.dto.*;
import com.farmatrack.inventario.service.ProductoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.ResponseEntity;
@RestController @RequestMapping("/productos") @RequiredArgsConstructor @Validated
public class ProductoController {
    private final ProductoService service;
    @PostMapping public ResponseEntity<ProductoResponse> create(@Valid @RequestBody ProductoInput i) { return ResponseEntity.status(201).body(service.create(i)); }
    @PutMapping("/{productoId}")
    public ProductoResponse update(@PathVariable Long productoId, @Valid @RequestBody ProductoInput i) { return service.update(productoId, i); }
    @GetMapping public PageResponse<ProductoResponse> list(@RequestParam(defaultValue="0") @Min(0) int page, @RequestParam(defaultValue="20") @Min(1) @Max(100) int size) {
        return PageResponse.from(service.findAll(PageRequest.of(page, size)));
    }
    @GetMapping("/{productoId}") public ProductoResponse get(@PathVariable Long productoId) { return service.findById(productoId); }
}
