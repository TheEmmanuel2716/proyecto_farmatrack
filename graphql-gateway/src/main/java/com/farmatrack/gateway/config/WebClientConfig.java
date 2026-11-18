package com.farmatrack.gateway.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {

    @Bean
    WebClient inventarioWebClient(
            @Value("${services.inventario.url}") String baseUrl) {
        return client(baseUrl);
    }

    @Bean
    WebClient vencimientosWebClient(
            @Value("${services.vencimientos.url}") String baseUrl) {
        return client(baseUrl);
    }

    private WebClient client(String baseUrl) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(5));
        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
