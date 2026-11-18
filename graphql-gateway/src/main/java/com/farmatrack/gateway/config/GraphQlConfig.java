package com.farmatrack.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.server.WebGraphQlInterceptor;

@Configuration
public class GraphQlConfig {

    public static final String AUTHORIZATION_CONTEXT_KEY = "authorization";

    @Bean
    WebGraphQlInterceptor authorizationForwardingInterceptor() {
        return (request, chain) -> {
            String authorization = request.getHeaders().getFirst("Authorization");
            if (authorization != null) {
                request.configureExecutionInput((input, builder) ->
                        builder.graphQLContext(context ->
                                context.put(AUTHORIZATION_CONTEXT_KEY, authorization)).build());
            }
            return chain.next(request);
        };
    }
}
