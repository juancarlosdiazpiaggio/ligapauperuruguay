package com.lpu.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-must-be-at-least-32-chars-long";
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 3_600_000L);
    }

    @Test
    void generate_y_parse_roundtrip() {
        AppUser user = usuario(42L, Role.ADMIN, "admin@lpu.uy");

        String token = jwtService.generate(user);
        Claims claims = jwtService.parse(token);

        assertThat(claims.getSubject()).isEqualTo("42");
        assertThat(claims.get("role", String.class)).isEqualTo("ADMIN");
        assertThat(claims.get("email", String.class)).isEqualTo("admin@lpu.uy");
    }

    @Test
    void generate_organizer_tiene_rol_correcto() {
        String token = jwtService.generate(usuario(1L, Role.ORGANIZER, "org@lpu.uy"));

        assertThat(jwtService.parse(token).get("role", String.class)).isEqualTo("ORGANIZER");
    }

    @Test
    void parse_token_invalido_lanza_excepcion() {
        assertThatThrownBy(() -> jwtService.parse("token.invalido.aqui"))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void parse_token_firmado_con_otra_clave_lanza_excepcion() {
        JwtService otroServicio = new JwtService("otra-clave-completamente-diferente-32chars", 3_600_000L);
        String tokenAjeno = otroServicio.generate(usuario(1L, Role.ADMIN, "x@x.com"));

        assertThatThrownBy(() -> jwtService.parse(tokenAjeno))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void parse_token_expirado_lanza_excepcion() throws InterruptedException {
        JwtService servicioConExpiracionCorta = new JwtService(SECRET, 1L);
        String token = servicioConExpiracionCorta.generate(usuario(1L, Role.ADMIN, "x@x.com"));

        Thread.sleep(10);

        assertThatThrownBy(() -> jwtService.parse(token))
                .isInstanceOf(JwtException.class);
    }

    private AppUser usuario(Long id, Role role, String email) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setRole(role);
        user.setEmail(email);
        user.setName("Test");
        user.setGoogleSub("sub-" + id);
        return user;
    }
}
