package com.farmatrack.vencimientos.service;

import tools.jackson.databind.ObjectMapper;
import com.farmatrack.vencimientos.dto.AlertaResponse;
import com.farmatrack.vencimientos.entity.*;
import com.farmatrack.vencimientos.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class VencimientoServiceTest {
    @Mock LoteEstadoRepository loteEstadoRepository;
    @Mock AlertaRepository alertaRepository;
    @Mock EventoProcesadoRepository eventoProcesadoRepository;
    @InjectMocks VencimientoService service;

    private final Clock clock = Clock.fixed(Instant.parse("2026-09-18T12:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void configure() {
        service = new VencimientoService(loteEstadoRepository, alertaRepository,
                eventoProcesadoRepository, new VencimientoCalculator(), new ObjectMapper(), clock);
        lenient().when(alertaRepository.findByLoteIdAndTipoAlerta(anyLong(), any())).thenReturn(Optional.empty());
        lenient().when(loteEstadoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void generaAlertasAlCruzarUmbrales() {
        when(eventoProcesadoRepository.existsById(any())).thenReturn(false);
        when(loteEstadoRepository.findByLoteId(501L)).thenReturn(Optional.empty());

        service.processEvent("""
                {"evento_uuid":"11111111-1111-1111-1111-111111111111",
                 "lote_id":501,"fecha_vencimiento":"2026-10-03",
                 "stock_actual":10,"tipo":"LOTE_INGRESADO"}
                """);

        verify(alertaRepository).save(argThat(alert -> alert.getTipoAlerta() == TipoAlerta.PROXIMO_A_VENCER_30));
        verify(alertaRepository).save(argThat(alert -> alert.getTipoAlerta() == TipoAlerta.PROXIMO_A_VENCER_15));
        verify(alertaRepository, never()).save(argThat(alert -> alert.getTipoAlerta() == TipoAlerta.PROXIMO_A_VENCER_7));
    }

    @Test
    void eventoRepetidoEsIdempotente() {
        UUID uuid = UUID.fromString("22222222-2222-2222-2222-222222222222");
        when(eventoProcesadoRepository.existsById(uuid)).thenReturn(false, true);
        when(loteEstadoRepository.findByLoteId(12L)).thenReturn(Optional.empty());

        String event = """
                {"evento_uuid":"22222222-2222-2222-2222-222222222222",
                 "lote_id":12,"fecha_vencimiento":"2027-01-01",
                 "stock_actual":4,"tipo":"LOTE_INGRESADO"}
                """;
        service.processEvent(event);
        service.processEvent(event);

        verify(loteEstadoRepository, times(1)).save(any(LoteEstado.class));
        verify(eventoProcesadoRepository, times(2)).existsById(uuid);
        verify(eventoProcesadoRepository, times(1)).save(any(EventoProcesado.class));
    }

    @Test
    void marcaAlertaAtendida() {
        Alerta alert = Alerta.builder().id(77L).loteId(501L)
                .tipoAlerta(TipoAlerta.PROXIMO_A_VENCER_15)
                .diasRestantes(15).generadaEn(OffsetDateTime.now(clock))
                .atendida(false).build();
        when(alertaRepository.findById(77L)).thenReturn(Optional.of(alert));
        when(alertaRepository.save(alert)).thenReturn(alert);

        AlertaResponse response = service.markAlert(77L, true);

        assertTrue(response.atendida());
        assertTrue(alert.isAtendida());
        verify(alertaRepository).save(alert);
    }
}
