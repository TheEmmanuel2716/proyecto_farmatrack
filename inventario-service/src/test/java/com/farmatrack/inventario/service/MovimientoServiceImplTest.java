package com.farmatrack.inventario.service;

import com.farmatrack.inventario.dto.MovimientoInput;
import com.farmatrack.inventario.dto.MovimientoResponse;
import com.farmatrack.inventario.entity.*;
import com.farmatrack.inventario.event.KafkaEventPublisher;
import com.farmatrack.inventario.exception.*;
import com.farmatrack.inventario.repository.MovimientoRepository;
import com.farmatrack.inventario.service.impl.MovimientoServiceImpl;
import com.farmatrack.inventario.service.impl.LoteServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.data.domain.*;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class MovimientoServiceImplTest {
    @Mock LoteServiceImpl loteService;
    @Mock MovimientoRepository movimientoRepository;
    @Mock KafkaEventPublisher publisher;
    @InjectMocks MovimientoServiceImpl service;

    private Lote lote;

    @BeforeEach
    void setUp() {
        lote = Lote.builder().id(10L).fechaVencimiento(LocalDate.now().plusDays(30))
                .stockActual(10).cantidadInicial(10)
                .producto(Producto.builder().id(2L).build()).build();
        when(loteService.get(10L)).thenReturn(lote);
        lenient().when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(inv -> {
            Movimiento m = inv.getArgument(0);
            m.setId(99L);
            if (m.getEventoUuid() == null) m.setEventoUuid(java.util.UUID.randomUUID());
            if (m.getFechaHora() == null) m.setFechaHora(java.time.OffsetDateTime.now());
            return m;
        });
    }

    @Test
    void ventaRechazadaPorVencimientoNoModificaNiPublica() {
        lote.setFechaVencimiento(LocalDate.now().minusDays(1));
        assertThrows(LoteVencidoException.class, () -> service.sell(10L, new MovimientoInput(1, null)));
        assertEquals(10, lote.getStockActual());
        verifyNoInteractions(movimientoRepository, publisher);
    }

    @Test
    void ventaRechazadaPorStockNoModificaNiPublica() {
        assertThrows(StockInsuficienteException.class, () -> service.sell(10L, new MovimientoInput(11, null)));
        assertEquals(10, lote.getStockActual());
        verifyNoInteractions(movimientoRepository, publisher);
    }

    @Test
    void ventaExitosaDescuentaYPublicaEvento() {
        MovimientoResponse response = service.sell(10L, new MovimientoInput(4, "mostrador"));
        assertEquals(6, lote.getStockActual());
        assertEquals("VENTA", response.tipo());
        verify(movimientoRepository).save(argThat(m -> m.getTipo() == Movimiento.Tipo.VENTA && m.getCantidad() == 4));
        verify(publisher).publish(eq(10L), any());
    }

    @Test
    void devolucionExitosaDescuentaYRegistraMovimiento() {
        MovimientoResponse response = service.returnToSupplier(10L, new MovimientoInput(3, "proveedor"));
        assertEquals(7, lote.getStockActual());
        assertEquals("DEVOLUCION", response.tipo());
        verify(movimientoRepository).save(argThat(m -> m.getTipo() == Movimiento.Tipo.DEVOLUCION));
        verify(publisher).publish(eq(10L), any());
    }
}
