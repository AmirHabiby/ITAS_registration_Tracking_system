package com.aronalvarenga.rtts.config;

import com.aronalvarenga.rtts.identity.application.JwtProperties;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {

        http
            // JWT-authenticated API requests are stateless and do not use browser sessions.
            .csrf(csrf ->
                csrf.ignoringRequestMatchers("/h2-console/**", "/api/**"))

            // Use stateless sessions because authentication is handled with JWT
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Enable CORS configuration
            .cors(Customizer.withDefaults())

            // Configure authorization rules
            .authorizeHttpRequests(auth -> auth

                // Allow unauthenticated access to H2 Console
                .requestMatchers("/h2-console/**").permitAll()

                // Allow health/info endpoints
                .requestMatchers(
                    HttpMethod.GET,
                    "/actuator/health",
                    "/actuator/info"
                ).permitAll()

                // Authentication endpoints
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/auth/login",
                    "/api/auth/logout"
                ).permitAll()

                // Everything else requires authentication
                .anyRequest().authenticated())

            // H2 Console uses frames
            .headers(headers ->
                headers.frameOptions(frame -> frame.disable()))

            // Configure JWT authentication
            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwt ->
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(
            List.of("http://localhost:5173")
        );

        configuration.setAllowedMethods(
            List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
            )
        );

        configuration.setAllowedHeaders(
            List.of(
                "Authorization",
                "Content-Type"
            )
        );

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
            "/**",
            configuration
        );

        return source;
    }

    @Bean
    JwtEncoder jwtEncoder(JwtProperties jwtProperties) {

        return new NimbusJwtEncoder(
            new ImmutableSecret<>(
                jwtProperties
                    .getSecret()
                    .getBytes(StandardCharsets.UTF_8)
            )
        );
    }

    @Bean
    JwtDecoder jwtDecoder(JwtProperties jwtProperties) {

        SecretKey key = new SecretKeySpec(
            jwtProperties
                .getSecret()
                .getBytes(StandardCharsets.UTF_8),
            "HmacSHA256"
        );

        return NimbusJwtDecoder
            .withSecretKey(key)
            .macAlgorithm(MacAlgorithm.HS256)
            .build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtAuthenticationConverter converter =
            new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {

            Collection<GrantedAuthority> authorities =
                new ArrayList<>();

            Object roles = jwt.getClaims().get("roles");

            if (roles instanceof List<?> roleList &&
                !roleList.isEmpty()) {

                for (Object roleValue : roleList) {

                    authorities.add(
                        new SimpleGrantedAuthority(
                            "ROLE_" + roleValue
                        )
                    );
                }

                return authorities;
            }

            String role = jwt.getClaimAsString("role");

            if (role != null && !role.isBlank()) {

                authorities.add(
                    new SimpleGrantedAuthority(
                        "ROLE_" + role
                    )
                );
            }

            return authorities;
        });

        return converter;
    }
}