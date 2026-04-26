package com.lpu.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@TestPropertySource(properties = {
        "lpu.jwt.secret=test-secret-key-must-be-at-least-32-chars-long",
        "lpu.jwt.expiration-ms=3600000",
        "lpu.google.client-id=test-client-id"
})
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean AuthService authService;
    @MockBean JwtService jwtService;

    @Test
    void login_con_google_token_valido_retorna_jwt() throws Exception {
        when(authService.authenticateWithGoogle("id-token-valido")).thenReturn("jwt-generado");

        mockMvc.perform(post("/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AuthController.GoogleTokenRequest("id-token-valido"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-generado"));
    }

    @Test
    void login_con_token_invalido_retorna_401() throws Exception {
        when(authService.authenticateWithGoogle("token-invalido"))
                .thenThrow(new UnauthorizedException("Token de Google invalido"));

        mockMvc.perform(post("/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AuthController.GoogleTokenRequest("token-invalido"))))
                .andExpect(status().isUnauthorized());
    }
}
