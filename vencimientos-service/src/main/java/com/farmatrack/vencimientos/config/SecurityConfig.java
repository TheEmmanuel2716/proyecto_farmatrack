package com.farmatrack.vencimientos.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> {}));
        return http.build();
    }

    @Bean
    JwtDecoder jwtDecoder(@Value("${security.jwt.secret}") String secret,
                          @Value("${security.jwt.issuer:}") String issuer,
                          @Value("${security.jwt.audience:}") String audience) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(
                new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256")).build();
        OAuth2TokenValidator<Jwt> timestampValidator = JwtValidators.createDefault();
        List<OAuth2TokenValidator<Jwt>> validators = new java.util.ArrayList<>();
        validators.add(timestampValidator);
        if (!issuer.isBlank()) {
            validators.add(new JwtClaimValidator<>("iss",
                    value -> issuer.equals(value)));
        }
        if (!audience.isBlank()) {
            validators.add(new JwtClaimValidator<List<String>>("aud",
                    value -> value != null && value.contains(audience)));
        }
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(validators));
        return decoder;
    }
}
