package io.github.raissaamaral.mapadevagas.user;

import io.github.raissaamaral.mapadevagas.security.AuthCookieFactory;
import io.github.raissaamaral.mapadevagas.security.SecurityConfig;
import io.github.raissaamaral.mapadevagas.security.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, AuthCookieFactory.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService service;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private TokenService tokenService;

    @Test
    void registerShouldReturnBadRequestWhenPasswordIsTooShort() throws Exception {
        String body = """
                {
                  "email": "rai@example.com",
                  "password": "curta"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").exists());

        verify(service, never()).register(any());
    }

    @Test
    void registerShouldReturnBadRequestWhenEmailIsInvalid() throws Exception {
        String body = """
                {
                  "email": "nao-e-email",
                  "password": "senha-forte-123"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());

        verify(service, never()).register(any());
    }

    @Test
    void loginShouldSetHttpOnlyTokenCookie() throws Exception {
        when(service.authenticate(any())).thenReturn(new UserResponse(1L, "rai@example.com"));
        when(tokenService.generateToken(1L)).thenReturn("test-token");
        when(tokenService.getExpiration()).thenReturn(Duration.ofHours(1));

        String body = """
            {
              "email": "rai@example.com",
              "password": "senha-forte-123"
            }
            """;

        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("access_token=test-token")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("SameSite=Strict")))
                .andExpect(jsonPath("$.email").value("rai@example.com"));
    }

    @Test
    void loginShouldReturnUnauthorizedWhenCredentialsAreInvalid() throws Exception {
        when(service.authenticate(any())).thenThrow(new InvalidCredentialsException());

        String body = """
            {
              "email": "rai@example.com",
              "password": "senha-errada-123"
            }
            """;

        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"))
                .andExpect(cookie().doesNotExist(SecurityConfig.TOKEN_COOKIE));
    }

    @Test
    void meShouldReturnUnauthorizedWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginShouldReturnTooManyRequestsWhenEmailIsBlocked() throws Exception {
        when(service.authenticate(any())).thenThrow(new TooManyLoginAttemptsException());

        String body = """
            {
              "email": "rai@example.com",
              "password": "senha-forte-123"
            }
            """;

        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isTooManyRequests())
                .andExpect(cookie().doesNotExist(SecurityConfig.TOKEN_COOKIE));
    }

    @Test
    void loginShouldReturnForbiddenWithoutCsrfToken() throws Exception {
        String body = """
            {
              "email": "rai@example.com",
              "password": "senha-forte-123"
            }
            """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Access denied"));

        verify(service, never()).authenticate(any());
    }

    @Test
    void anyRequestShouldIssueReadableCsrfCookie() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(cookie().exists("XSRF-TOKEN"))
                .andExpect(cookie().httpOnly("XSRF-TOKEN", false));
    }
}