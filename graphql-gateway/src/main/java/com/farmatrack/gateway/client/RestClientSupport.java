package com.farmatrack.gateway.client;

import com.farmatrack.gateway.exception.DownstreamServiceException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

abstract class RestClientSupport {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);

    private final JsonMapper jsonMapper;

    RestClientSupport(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    protected <T> T execute(String service, Mono<T> request) {
        try {
            return request.block(REQUEST_TIMEOUT);
        } catch (WebClientResponseException exception) {
            throw new DownstreamServiceException(
                    service,
                    exception.getStatusCode().value(),
                    remoteCode(exception),
                    "Respuesta de error recibida desde " + service,
                    exception);
        } catch (RuntimeException exception) {
            if (hasCause(exception, TimeoutException.class)) {
                throw new DownstreamServiceException(
                        service, 504, "DEPENDENCY_TIMEOUT",
                        "Tiempo de espera agotado al consultar " + service, exception);
            }
            if (hasCause(exception, WebClientRequestException.class)) {
                throw new DownstreamServiceException(
                        service, 503, "DEPENDENCY_UNAVAILABLE",
                        "No fue posible conectar con " + service, exception);
            }
            throw exception;
        }
    }

    protected void addAuthorization(HttpHeaders headers, String authorization) {
        if (authorization != null && !authorization.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, authorization);
        }
    }

    private String remoteCode(WebClientResponseException exception) {
        String responseBody = exception.getResponseBodyAsString();
        if (responseBody.isBlank()) {
            return null;
        }
        try {
            JsonNode code = jsonMapper.readTree(responseBody).get("codigo");
            return code != null && code.isTextual() ? code.asText() : null;
        } catch (JacksonException ignored) {
            return null;
        }
    }

    private boolean hasCause(Throwable exception, Class<? extends Throwable> type) {
        Throwable current = exception;
        while (current != null) {
            if (type.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
