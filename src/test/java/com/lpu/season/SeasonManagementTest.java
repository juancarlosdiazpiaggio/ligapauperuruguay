package com.lpu.season;

import com.lpu.auth.AppUser;
import com.lpu.auth.JwtService;
import com.lpu.auth.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SeasonManagementTest {

    @Autowired TestRestTemplate restTemplate;
    @Autowired JwtService jwtService;
    @Autowired SeasonRepository seasonRepository;

    private String adminToken;
    private String organizerToken;

    @BeforeEach
    void setUp() {
        seasonRepository.deleteAll();
        adminToken = jwtService.generate(usuario(1L, Role.ADMIN));
        organizerToken = jwtService.generate(usuario(2L, Role.ORGANIZER));
    }

    @Test
    void admin_crea_temporada_y_es_recuperable() {
        ResponseEntity<Map> created = postSeason(2026, adminToken);

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody().get("year")).isEqualTo(2026);

        ResponseEntity<Map> active = restTemplate.getForEntity("/seasons/active", Map.class);

        assertThat(active.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(active.getBody().get("year")).isEqualTo(2026);
    }

    @Test
    void temporada_tiene_fechas_inicio_y_fin_correctas() {
        ResponseEntity<Map> response = postSeason(2026, adminToken);

        assertThat(response.getBody().get("startDate")).isEqualTo("2026-01-01");
        assertThat(response.getBody().get("endDate")).isEqualTo("2026-12-31");
    }

    @Test
    void crear_segunda_temporada_activa_retorna_409() {
        postSeason(2025, adminToken);

        ResponseEntity<Map> second = postSeason(2026, adminToken);

        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void crear_temporada_sin_jwt_retorna_401() {
        HttpEntity<Map<String, Integer>> entity = new HttpEntity<>(Map.of("year", 2026));

        ResponseEntity<Map> response = restTemplate.exchange("/seasons", HttpMethod.POST, entity, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void crear_temporada_con_rol_organizer_retorna_403() {
        ResponseEntity<Map> response = postSeason(2026, organizerToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void consultar_temporada_activa_sin_ninguna_creada_retorna_409() {
        ResponseEntity<Map> response = restTemplate.getForEntity("/seasons/active", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    // helpers

    private ResponseEntity<Map> postSeason(int year, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Integer>> entity = new HttpEntity<>(Map.of("year", year), headers);
        return restTemplate.exchange("/seasons", HttpMethod.POST, entity, Map.class);
    }

    private AppUser usuario(Long id, Role role) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setRole(role);
        user.setEmail("test@lpu.uy");
        user.setName("Test");
        user.setGoogleSub("sub-" + id);
        return user;
    }
}
