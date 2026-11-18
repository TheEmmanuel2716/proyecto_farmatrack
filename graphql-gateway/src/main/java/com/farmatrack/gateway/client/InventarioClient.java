package com.farmatrack.gateway.client;

import tools.jackson.databind.json.JsonMapper;
import com.farmatrack.gateway.dto.Lote;
import com.farmatrack.gateway.dto.LoteInput;
import com.farmatrack.gateway.dto.Movimiento;
import com.farmatrack.gateway.dto.MovimientoInput;
import com.farmatrack.gateway.dto.PageResponse;
import com.farmatrack.gateway.dto.Producto;
import com.farmatrack.gateway.dto.ProductoInput;
import com.farmatrack.gateway.dto.Proveedor;
import com.farmatrack.gateway.dto.ProveedorInput;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class InventarioClient extends RestClientSupport {

    private static final String SERVICE = "inventario-service";
    private static final ParameterizedTypeReference<PageResponse<Proveedor>> PROVEEDORES_TYPE =
            new ParameterizedTypeReference<>() { };
    private static final ParameterizedTypeReference<PageResponse<Producto>> PRODUCTOS_TYPE =
            new ParameterizedTypeReference<>() { };
    private static final ParameterizedTypeReference<PageResponse<Lote>> LOTES_TYPE =
            new ParameterizedTypeReference<>() { };
    private static final ParameterizedTypeReference<PageResponse<Movimiento>> MOVIMIENTOS_TYPE =
            new ParameterizedTypeReference<>() { };

    private final WebClient webClient;

    public InventarioClient(
            @Qualifier("inventarioWebClient") WebClient webClient,
            JsonMapper jsonMapper) {
        super(jsonMapper);
        this.webClient = webClient;
    }

    public List<Proveedor> proveedores(int page, int size, String authorization) {
        PageResponse<Proveedor> response = execute(SERVICE, webClient.get()
                .uri(uri -> uri.path("/proveedores").queryParam("page", page)
                        .queryParam("size", size).build())
                .headers(headers -> addAuthorization(headers, authorization))
                .retrieve().bodyToMono(PROVEEDORES_TYPE));
        return response.content();
    }

    public Proveedor crearProveedor(ProveedorInput input, String authorization) {
        return execute(SERVICE, webClient.post().uri("/proveedores")
                .headers(headers -> addAuthorization(headers, authorization))
                .bodyValue(proveedorBody(input))
                .retrieve().bodyToMono(Proveedor.class));
    }

    public Proveedor actualizarProveedor(Long id, ProveedorInput input, String authorization) {
        return execute(SERVICE, webClient.put().uri("/proveedores/{id}", id)
                .headers(headers -> addAuthorization(headers, authorization))
                .bodyValue(proveedorBody(input))
                .retrieve().bodyToMono(Proveedor.class));
    }

    public List<Producto> productos(int page, int size, String authorization) {
        PageResponse<Producto> response = execute(SERVICE, webClient.get()
                .uri(uri -> uri.path("/productos").queryParam("page", page)
                        .queryParam("size", size).build())
                .headers(headers -> addAuthorization(headers, authorization))
                .retrieve().bodyToMono(PRODUCTOS_TYPE));
        return response.content();
    }

    public Producto producto(Long id, String authorization) {
        return execute(SERVICE, webClient.get().uri("/productos/{id}", id)
                .headers(headers -> addAuthorization(headers, authorization))
                .retrieve().bodyToMono(Producto.class));
    }

    public Producto crearProducto(ProductoInput input, String authorization) {
        return execute(SERVICE, webClient.post().uri("/productos")
                .headers(headers -> addAuthorization(headers, authorization))
                .bodyValue(productoBody(input))
                .retrieve().bodyToMono(Producto.class));
    }

    public Producto actualizarProducto(Long id, ProductoInput input, String authorization) {
        return execute(SERVICE, webClient.put().uri("/productos/{id}", id)
                .headers(headers -> addAuthorization(headers, authorization))
                .bodyValue(productoBody(input))
                .retrieve().bodyToMono(Producto.class));
    }

    public Proveedor proveedor(Long id, String authorization) {
        int page = 0;
        while (true) {
            int requestedPage = page;
            PageResponse<Proveedor> response = execute(SERVICE, webClient.get()
                    .uri(uri -> uri.path("/proveedores").queryParam("page", requestedPage)
                            .queryParam("size", 100).build())
                    .headers(headers -> addAuthorization(headers, authorization))
                    .retrieve().bodyToMono(PROVEEDORES_TYPE));
            Proveedor proveedor = response.content().stream()
                    .filter(item -> item.id().equals(id))
                    .findFirst()
                    .orElse(null);
            if (proveedor != null) {
                return proveedor;
            }
            if (!response.hasNext()) {
                throw new com.farmatrack.gateway.exception.DownstreamServiceException(
                        SERVICE, 404, "NOT_FOUND",
                        "No se encontró el proveedor " + id);
            }
            page++;
        }
    }

    public List<Lote> lotes(int page, int size, String authorization) {
        PageResponse<Lote> response = execute(SERVICE, webClient.get()
                .uri(uri -> uri.path("/lotes").queryParam("page", page)
                        .queryParam("size", size).build())
                .headers(headers -> addAuthorization(headers, authorization))
                .retrieve().bodyToMono(LOTES_TYPE));
        return response.content();
    }

    public Lote lote(Long id, String authorization) {
        return execute(SERVICE, webClient.get().uri("/lotes/{id}", id)
                .headers(headers -> addAuthorization(headers, authorization))
                .retrieve().bodyToMono(Lote.class));
    }

    public Lote crearLote(Long productoId, LoteInput input, String authorization) {
        Map<String, Object> body = Map.of(
                "numero_lote", input.numeroLote(),
                "fecha_vencimiento", input.fechaVencimiento(),
                "cantidad_inicial", input.cantidadInicial());
        return execute(SERVICE, webClient.post().uri("/productos/{id}/lotes", productoId)
                .headers(headers -> addAuthorization(headers, authorization))
                .bodyValue(body)
                .retrieve().bodyToMono(Lote.class));
    }

    public List<Movimiento> movimientos(Long loteId, int page, int size, String authorization) {
        PageResponse<Movimiento> response = execute(SERVICE, webClient.get()
                .uri(uri -> uri.path("/lotes/{id}/movimientos")
                        .queryParam("page", page).queryParam("size", size).build(loteId))
                .headers(headers -> addAuthorization(headers, authorization))
                .retrieve().bodyToMono(MOVIMIENTOS_TYPE));
        return response.content();
    }

    public Movimiento registrarVenta(Long loteId, MovimientoInput input, String authorization) {
        return registrarMovimiento(loteId, "ventas", input, authorization);
    }

    public Movimiento registrarDevolucion(Long loteId, MovimientoInput input, String authorization) {
        return registrarMovimiento(loteId, "devoluciones", input, authorization);
    }

    private Movimiento registrarMovimiento(
            Long loteId, String operation, MovimientoInput input, String authorization) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("cantidad", input.cantidad());
        body.put("observacion", input.observacion());
        return execute(SERVICE, webClient.post().uri("/lotes/{id}/{operation}", loteId, operation)
                .headers(headers -> addAuthorization(headers, authorization))
                .bodyValue(body)
                .retrieve().bodyToMono(Movimiento.class));
    }

    private Map<String, Object> proveedorBody(ProveedorInput input) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("nombre", input.nombre());
        body.put("nit", input.nit());
        body.put("contacto", input.contacto());
        return body;
    }

    private Map<String, Object> productoBody(ProductoInput input) {
        return Map.of(
                "nombre", input.nombre(),
                "principio_activo", input.principioActivo(),
                "proveedor_id", input.proveedorId());
    }
}
