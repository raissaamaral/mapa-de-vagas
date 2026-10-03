package io.github.raissaamaral.mapadevagas.security;

import jakarta.servlet.http.Cookie;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Set;

@Configuration
public class SecurityConfig {

    public static final String TOKEN_COOKIE = "access_token";

    private static final Set<String> PUBLIC_ENDPOINTS =
            Set.of("/auth/register", "/auth/login", "/auth/logout");

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF tokens will be enabled together with the frontend (required
                // before deployment). Until then, the SameSite=Strict cookie keeps
                // cross-site requests from sending the token.
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS.toArray(String[]::new)).permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .bearerTokenResolver(cookieTokenResolver())
                        .jwt(Customizer.withDefaults()));

        return http.build();
    }

    private BearerTokenResolver cookieTokenResolver() {
        return request -> {
            // Public auth endpoints ignore the cookie, so an expired token
            // never prevents logging in again
            if (PUBLIC_ENDPOINTS.contains(request.getRequestURI())) {
                return null;
            }

            Cookie[] cookies = request.getCookies();
            if (cookies == null) {
                return null;
            }

            for (Cookie cookie : cookies) {
                if (TOKEN_COOKIE.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
            return null;
        };
    }
}