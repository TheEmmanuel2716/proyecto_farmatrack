package com.farmatrack.gateway.exception;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import java.util.Map;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.stereotype.Component;

@Component
public class GraphQlExceptionResolver extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(
            Throwable exception, DataFetchingEnvironment environment) {
        if (!(exception instanceof DownstreamServiceException downstream)) {
            return null;
        }
        String code = graphQlCode(downstream);
        return GraphqlErrorBuilder.newError(environment)
                .message(message(downstream))
                .extensions(Map.of(
                        "code", code,
                        "service", downstream.getService(),
                        "httpStatus", downstream.getHttpStatus()))
                .build();
    }

    private String graphQlCode(DownstreamServiceException exception) {
        if (exception.getRemoteCode() != null && !exception.getRemoteCode().isBlank()) {
            return exception.getRemoteCode();
        }
        return switch (exception.getHttpStatus()) {
            case 400 -> "INVALID_ARGUMENT";
            case 401 -> "UNAUTHORIZED";
            case 403 -> "FORBIDDEN";
            case 404 -> "NOT_FOUND";
            case 409 -> "CONFLICT";
            case 422 -> "UNPROCESSABLE_ENTITY";
            case 504 -> "GATEWAY_TIMEOUT";
            default -> exception.getHttpStatus() >= 500
                    ? "DOWNSTREAM_FAILURE" : "DOWNSTREAM_ERROR";
        };
    }

    private String message(DownstreamServiceException exception) {
        return switch (exception.getHttpStatus()) {
            case 401 -> "El token no fue aceptado por " + exception.getService();
            case 403 -> "No hay permisos para consultar " + exception.getService();
            case 404 -> "El recurso solicitado no existe en " + exception.getService();
            case 409 -> "La operación entra en conflicto con el estado del recurso";
            case 422 -> "La operación no cumple las reglas de " + exception.getService();
            case 503, 504 -> exception.getMessage();
            default -> exception.getHttpStatus() >= 500
                    ? "Error interno en " + exception.getService()
                    : "Solicitud rechazada por " + exception.getService();
        };
    }
}
