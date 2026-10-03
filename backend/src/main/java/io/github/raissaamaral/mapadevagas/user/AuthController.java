package io.github.raissaamaral.mapadevagas.user;

import io.github.raissaamaral.mapadevagas.security.AuthCookieFactory;
import io.github.raissaamaral.mapadevagas.security.TokenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService service;
    private final TokenService tokenService;
    private final AuthCookieFactory cookieFactory;

    public AuthController(UserService service, TokenService tokenService, AuthCookieFactory cookieFactory) {
        this.service = service;
        this.tokenService = tokenService;
        this.cookieFactory = cookieFactory;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request) {
        UserResponse user = service.authenticate(request);
        String token = tokenService.generateToken(user.id());
        ResponseCookie cookie = cookieFactory.tokenCookie(token, tokenService.getExpiration());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(user);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookieFactory.expiredCookie().toString())
                .build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(service.findById(Long.valueOf(jwt.getSubject())));
    }
}