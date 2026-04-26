package com.lpu.auth;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private static final String CLIENT_ID = "test-client-id";
    private static final String SECRET = "test-secret-key-must-be-at-least-32-chars-long";

    private MockWebServer mockServer;
    private AuthService authService;
    private AppUserRepository userRepository;
    private JwtService jwtService;

    @BeforeEach
    void setUp() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();

        userRepository = mock(AppUserRepository.class);
        jwtService = new JwtService(SECRET, 3_600_000L);

        WebClient webClient = WebClient.create(mockServer.url("/").toString());
        authService = new AuthService(userRepository, jwtService, webClient,
                CLIENT_ID, mockServer.url("/").toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        mockServer.shutdown();
    }

    @Test
    void token_valido_con_usuario_existente_retorna_jwt() {
        mockServer.enqueue(tokeninfoResponse(CLIENT_ID, "google-sub-123", "user@test.com"));
        AppUser user = usuario("google-sub-123", Role.ORGANIZER);
        when(userRepository.findByGoogleSub("google-sub-123")).thenReturn(Optional.of(user));

        String jwt = authService.authenticateWithGoogle("id-token-valido");

        assertThat(jwt).isNotBlank();
        assertThat(jwtService.parse(jwt).get("role", String.class)).isEqualTo("ORGANIZER");
    }

    @Test
    void token_google_invalido_lanza_unauthorized() {
        mockServer.enqueue(new MockResponse().setResponseCode(400).setBody("{\"error\":\"invalid_token\"}"));

        assertThatThrownBy(() -> authService.authenticateWithGoogle("token-invalido"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("invalido");
    }

    @Test
    void token_con_audience_incorrecta_lanza_unauthorized() {
        mockServer.enqueue(tokeninfoResponse("otro-cliente-id", "sub-123", "user@test.com"));

        assertThatThrownBy(() -> authService.authenticateWithGoogle("token-otro-cliente"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void usuario_no_registrado_en_lpu_lanza_unauthorized() {
        mockServer.enqueue(tokeninfoResponse(CLIENT_ID, "sub-desconocido", "nuevo@test.com"));
        when(userRepository.findByGoogleSub(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.authenticateWithGoogle("token-usuario-nuevo"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("no registrado");
    }

    // helpers

    private MockResponse tokeninfoResponse(String aud, String sub, String email) {
        String body = """
                {"aud":"%s","sub":"%s","email":"%s","name":"Test User"}
                """.formatted(aud, sub, email);
        return new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(body);
    }

    private AppUser usuario(String googleSub, Role role) {
        AppUser user = new AppUser();
        user.setId(1L);
        user.setGoogleSub(googleSub);
        user.setEmail("user@test.com");
        user.setName("Test User");
        user.setRole(role);
        return user;
    }
}
