package com.farmatrack.gateway.resolver;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

import com.farmatrack.gateway.client.InventarioClient;
import com.farmatrack.gateway.client.VencimientosClient;
import com.farmatrack.gateway.dto.Alerta;
import com.farmatrack.gateway.dto.Lote;
import com.farmatrack.gateway.dto.LoteEstado;
import com.farmatrack.gateway.dto.Producto;
import com.farmatrack.gateway.dto.ProductoInput;
import com.farmatrack.gateway.dto.Proveedor;
import com.farmatrack.gateway.exception.DownstreamServiceException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.graphql.ExecutionGraphQlService;
import org.springframework.graphql.test.tester.ExecutionGraphQlServiceTester;
import org.springframework.graphql.test.tester.GraphQlTester;

@SpringBootTest
class GatewayGraphQlControllerTest {

    @MockitoBean
    private InventarioClient inventarioClient;

    @MockitoBean
    private VencimientosClient vencimientosClient;

    @Autowired
    private ExecutionGraphQlService graphQlService;

    private GraphQlTester graphQlTester() {
        return ExecutionGraphQlServiceTester.create(graphQlService);
    }

    @Test
    void returnsProducts() {
        given(inventarioClient.productos(0, 20, null))
                .willReturn(List.of(new Producto(10L, "Acetaminofen", "Acetaminofen", 3L)));

        graphQlTester().document("""
                query {
                  productos {
                    id
                    nombre
                    principioActivo
                  }
                }
                """)
                .execute()
                .path("productos[0].nombre").entity(String.class).isEqualTo("Acetaminofen")
                .path("productos[0].principioActivo").entity(String.class).isEqualTo("Acetaminofen");
    }

    @Test
    void resolvesNestedLotState() {
        given(inventarioClient.lote(501L, null)).willReturn(lote());
        given(vencimientosClient.estado(501L, null)).willReturn(estado());

        graphQlTester().document("""
                query {
                  lote(id: "501") {
                    numeroLote
                    estado {
                      loteId
                      estado
                      diasParaVencer
                    }
                  }
                }
                """)
                .execute()
                .path("lote.estado.estado").entity(String.class).isEqualTo("VIGENTE")
                .path("lote.estado.diasParaVencer").entity(Integer.class).isEqualTo(120);
    }

    @Test
    void returnsNullStateWhenExpirationServiceHasNotSynchronizedLot() {
        given(inventarioClient.lote(501L, null)).willReturn(lote());
        given(vencimientosClient.estado(501L, null)).willThrow(
                new DownstreamServiceException(
                        "vencimientos-service", 404, "LOTE_NO_SINCRONIZADO",
                        "Lote aún no sincronizado"));

        graphQlTester().document("""
                query {
                  lote(id: "501") {
                    estado {
                      estado
                    }
                  }
                }
                """)
                .execute()
                .path("lote.estado").valueIsNull();
    }

    @Test
    void marksAlertAsAttended() {
        Alerta alerta = new Alerta(
                77L, 501L, "VENCIDO", -2, "2026-10-29T10:00:00Z", true);
        given(vencimientosClient.marcarAlertaAtendida(77L, true, null)).willReturn(alerta);

        graphQlTester().document("""
                mutation {
                  marcarAlertaAtendida(alertaId: "77", atendida: true) {
                    id
                    atendida
                  }
                }
                """)
                .execute()
                .path("marcarAlertaAtendida.id").entity(String.class).isEqualTo("77")
                .path("marcarAlertaAtendida.atendida").entity(Boolean.class).isEqualTo(true);
    }

    @Test
    void createsProductThroughGraphQlMutation() {
        given(inventarioClient.crearProducto(any(ProductoInput.class), isNull()))
                .willReturn(new Producto(11L, "Ibuprofeno", "Ibuprofeno", 3L));

        graphQlTester().document("""
                mutation CrearProducto($input: ProductoInput!) {
                  crearProducto(input: $input) {
                    id
                    nombre
                    principioActivo
                    proveedorId
                  }
                }
                """)
                .variable("input", Map.of(
                        "nombre", "Ibuprofeno",
                        "principioActivo", "Ibuprofeno",
                        "proveedorId", "3"))
                .execute()
                .path("crearProducto.id").entity(String.class).isEqualTo("11")
                .path("crearProducto.proveedorId").entity(String.class).isEqualTo("3");
    }

    private Lote lote() {
        return new Lote(501L, "L-2026-0456", "2027-12-01",
                200, 10L, 195, "2026-10-29T10:00:00Z");
    }

    private LoteEstado estado() {
        return new LoteEstado(850L, 501L, 195, "VIGENTE", 120,
                "2026-10-29T10:00:00Z");
    }
}
