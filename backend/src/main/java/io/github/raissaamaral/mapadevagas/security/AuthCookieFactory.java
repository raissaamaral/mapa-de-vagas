package io.github.raissaamaral.mapadevagas.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class AuthCookieFactory {

    private final boolean secure;

    public AuthCookieFactory(@Value("${app.cookie.secure}") boolean secure) {
        this.secure = secure;
    }

    public ResponseCookie tokenCookie(String token, Duration maxAge) {
        return baseCookie(token).maxAge(maxAge).build();
    }

    public ResponseCookie expiredCookie() {
        return baseCookie("").maxAge(0).build();
    }

    private ResponseCookie.ResponseCookieBuilder baseCookie(String value) {
        return ResponseCookie.from(SecurityConfig.TOKEN_COOKIE, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
                .path("/");
    }
}