package com.farmatrack.gateway.client;

import com.farmatrack.gateway.dto.Alerta;
import com.farmatrack.gateway.dto.LoteEstado;
import java.util.List;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class VencimientosClient extends RestClientSupport {

    private static final String SERVICE = "vencimientos-service";
    private static final ParameterizedTypeReference<List<Alerta>> ALERTAS_TYPE =
            new ParameterizedTypeReference<>() { };

    private final WebClient webClient;

    public VencimientosClient(
            @Qualifier("vencimientosWebClient") WebClient webClient,
            JsonMapper jsonMapper) {
        super(jsonMapper);
        this.webClient = webClient;
    }

    public LoteEstado estado(Long loteId, String authorization) {
        return execute(SERVICE, webClient.get().uri("/lotes/{id}/estado", loteId)
                .headers(headers -> addAuthorization(headers, authorization))
                .retrieve().bodyToMono(LoteEstado.class));
    }

    public List<Alerta> alertas(Boolean atendida, int page, int size, String authorization) {
        return execute(SERVICE, webClient.get()
                .uri(uri -> uri.path("/alertas")
                        .queryParamIfPresent("atendida", java.util.Optional.ofNullable(atendida))
                        .queryParam("page", page).queryParam("size", size).build())
                .headers(headers -> addAuthorization(headers, authorization))
                .retrieve().bodyToMono(ALERTAS_TYPE));
    }

    public Alerta marcarAlertaAtendida(Long alertaId, boolean atendida, String authorization) {
        return execute(SERVICE, webClient.patch().uri("/alertas/{id}", alertaId)
                .headers(headers -> addAuthorization(headers, authorization))
                .bodyValue(java.util.Map.of("atendida", atendida))
                .retrieve().bodyToMono(Alerta.class));
    }
}
