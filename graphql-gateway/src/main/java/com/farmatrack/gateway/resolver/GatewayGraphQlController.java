package com.farmatrack.gateway.resolver;

import com.farmatrack.gateway.client.InventarioClient;
import com.farmatrack.gateway.client.VencimientosClient;
import com.farmatrack.gateway.config.GraphQlConfig;
import com.farmatrack.gateway.dto.Alerta;
import com.farmatrack.gateway.dto.Lote;
import com.farmatrack.gateway.dto.LoteConAlertas;
import com.farmatrack.gateway.dto.LoteEstado;
import com.farmatrack.gateway.dto.LoteInput;
import com.farmatrack.gateway.dto.Movimiento;
import com.farmatrack.gateway.dto.MovimientoInput;
import com.farmatrack.gateway.dto.Producto;
import com.farmatrack.gateway.dto.ProductoInput;
import com.farmatrack.gateway.dto.Proveedor;
import com.farmatrack.gateway.dto.ProveedorInput;
import com.farmatrack.gateway.exception.DownstreamServiceException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class GatewayGraphQlController {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private final InventarioClient inventarioClient;
    private final VencimientosClient vencimientosClient;

    @QueryMapping
    public List<Proveedor> proveedores(
            @Argument Integer page,
            @Argument Integer size,
            @ContextValue(name = GraphQlConfig.AUTHORIZATION_CONTEXT_KEY, required = false)
                    String authorization) {
        return inventarioClient.proveedores(page(page), size(size), authorization);
    }

    @QueryMapping
    public List<Producto> productos(
            @Argument Integer page,
            @Argument Integer size,
            @ContextValue(name = GraphQlConfig.AUTHORIZATION_CONTEXT_KEY, required = false)
                    String authorization) {
        return inventarioClient.productos(page(page), size(size), authorization);
    }

    @QueryMapping
    public Producto producto(
            @Argument String id,
            @ContextValue(name = GraphQlConfig.AUTHORIZATION_CONTEXT_KEY, required = false)
                    String authorization) {
        return inventarioClient.producto(id(id), authorization);
    }

    @QueryMapping
    public List<Lote> lotes(
            @Argument Integer page,
            @Argument Integer size,
            @ContextValue(name = GraphQlConfig.AUTHORIZATION_CONTEXT_KEY, required = false)
                    String authorization) {
        return inventarioClient.lotes(page(page), size(size), authorization);
    }

    @QueryMapping
    public Lote lote(
            @Argument String id,
            @ContextValue(name = GraphQlConfig.AUTHORIZATION_CONTEXT_KEY, required = false)
                    String authorization) {
        return inventarioClient.lote(id(id), authorization);
    }

    @QueryMapping
    public LoteEstado estado(
            @Argument String loteId,
            @ContextValue(name = GraphQlConfig.AUTHORIZATION_CONTEXT_KEY, required = false)
                    String authorization) {
        return vencimientosClient.estado(id(loteId), authorization);
    }

    @QueryMapping
    public List<Alerta> alertas(
            @Argument Boolean atendida,
            @Argument Integer page,
            @Argument Integer size,
            @ContextValue(name = GraphQlConfig.AUTHORIZATION_CONTEXT_KEY, required = false)
                    String authorization) {
        return vencimientosClient.alertas(atendida, page(page), size(size), authorization);
    }

    @QueryMapping
    public List<Movimiento> movimientos(
            @Argument String loteId,
            @Argument Integer page,
            @Argument Integer size,
            @ContextValue(name = GraphQlConfig.AUTHORIZATION_CONTEXT_KEY, required = false)
                    String authorization) {
        return inventarioClient.movimientos(id(loteId), page(page), size(size), authorization);
    }

    @QueryMapping
    public LoteConAlertas loteConAlertas(
            @Argument String loteId,
            @ContextValue(name = GraphQlConfig.AUTHORIZATION_CONTEXT_KEY, required = false)
                    String authorization) {
        Lote lote = inventarioClient.lote(id(loteId), authorization);
        return new LoteConAlertas(lote);
    }

    @MutationMapping
    public Alerta marcarAlertaAtendida(
            @Argument String alertaId,
            @Argument boolean atendida,
            @ContextValue(name = GraphQlConfig.AUTHORIZATION_CONTEXT_KEY, required = false)
                    String authorization) {
        return vencimientosClient.marcarAlertaAtendida(
                id(alertaId), atendida, authorization);
    }

    @MutationMapping
    public Proveedor crearProveedor(
            @Argument ProveedorInput input,
            @ContextValue(name = GraphQlConfig.AUTHORIZATION_CONTEXT_KEY, required = false)
                    String authorization) {
        return inventarioClient.crearProveedor(input, authorization);
    }

    @MutationMapping
    public Proveedor actualizarProveedor(
            @Argument String id,
            @Argument ProveedorInput input,
            @ContextValue(name = GraphQlConfig.AUTHORIZATION_CONTEXT_KEY, required = false)
                    String authorization) {
        return inventarioClient.actualizarProveedor(id(id), input, authorization);
    }

    @MutationMapping
    public Producto crearProducto(
            @Argument ProductoInput input,
            @ContextValue(name = GraphQlConfig.AUTHORIZATION_CONTEXT_KEY, required = false)
                    String authorization) {
        return inventarioClient.crearProducto(input, authorization);
    }

    @MutationMapping
    public Producto actualizarProducto(
            @Argument String id,
            @Argument ProductoInput input,
            @ContextValue(name = GraphQlConfig.AUTHORIZATION_CONTEXT_KEY, required = false)
                    String authorization) {
        return inventarioClient.actualizarProducto(id(id), input, authorization);
    }

    @MutationMapping
    public Lote crearLote(
            @Argument String productoId,
            @Argument LoteInput input,
            @ContextValue(name = GraphQlConfig.AUTHORIZATION_CONTEXT_KEY, required = false)
                    String authorization) {
        return inventarioClient.crearLote(id(productoId), input, authorization);
    }

    @MutationMapping
    public Movimiento registrarVenta(
            @Argument String loteId,
            @Argument MovimientoInput input,
            @ContextValue(name = GraphQlConfig.AUTHORIZATION_CONTEXT_KEY, required = false)
                    String authorization) {
        return inventarioClient.registrarVenta(id(loteId), input, authorization);
    }

    @MutationMapping
    public Movimiento registrarDevolucion(
            @Argument String loteId,
            @Argument MovimientoInput input,
            @ContextValue(name = GraphQlConfig.AUTHORIZATION_CONTEXT_KEY, required = false)
                    String authorization) {
        return inventarioClient.registrarDevolucion(id(loteId), input, authorization);
    }

    @SchemaMapping(typeName = "Producto", field = "proveedor")
    public Proveedor proveedor(
            Producto producto,
            @ContextValue(name = GraphQlConfig.AUTHORIZATION_CONTEXT_KEY, required = false)
                    String authorization) {
        return inventarioClient.proveedor(producto.proveedorId(), authorization);
    }

    @SchemaMapping(typeName = "Lote", field = "producto")
    public Producto productoDeLote(
            Lote lote,
            @ContextValue(name = GraphQlConfig.AUTHORIZATION_CONTEXT_KEY, required = false)
                    String authorization) {
        return inventarioClient.producto(lote.productoId(), authorization);
    }

    @SchemaMapping(typeName = "Lote", field = "estado")
    public LoteEstado estado(
            Lote lote,
            @ContextValue(name = GraphQlConfig.AUTHORIZATION_CONTEXT_KEY, required = false)
                    String authorization) {
        return estadoOrNull(lote.id(), authorization);
    }

    @SchemaMapping(typeName = "LoteConAlertas", field = "estado")
    public LoteEstado estado(
            LoteConAlertas resultado,
            @ContextValue(name = GraphQlConfig.AUTHORIZATION_CONTEXT_KEY, required = false)
                    String authorization) {
        return estadoOrNull(resultado.lote().id(), authorization);
    }

    @SchemaMapping(typeName = "LoteConAlertas", field = "alertas")
    public List<Alerta> alertas(
            LoteConAlertas resultado,
            @ContextValue(name = GraphQlConfig.AUTHORIZATION_CONTEXT_KEY, required = false)
                    String authorization) {
        return vencimientosClient.alertas(null, 0, MAX_SIZE, authorization).stream()
                .filter(alerta -> alerta.loteId().equals(resultado.lote().id()))
                .toList();
    }

    private LoteEstado estadoOrNull(Long loteId, String authorization) {
        try {
            return vencimientosClient.estado(loteId, authorization);
        } catch (DownstreamServiceException exception) {
            if (exception.getHttpStatus() == 404) {
                // Kafka may not have synchronized a newly created inventory lot yet.
                return null;
            }
            throw exception;
        }
    }

    private int page(Integer value) {
        int page = value == null ? DEFAULT_PAGE : value;
        if (page < 0) {
            throw new IllegalArgumentException("page debe ser mayor o igual a cero");
        }
        return page;
    }

    private int size(Integer value) {
        int size = value == null ? DEFAULT_SIZE : value;
        if (size < 1 || size > MAX_SIZE) {
            throw new IllegalArgumentException("size debe estar entre 1 y " + MAX_SIZE);
        }
        return size;
    }

    private Long id(String value) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("El identificador debe ser numérico", exception);
        }
    }
}
