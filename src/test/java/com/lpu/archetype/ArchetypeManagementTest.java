package com.lpu.archetype;

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

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ArchetypeManagementTest {

    @Autowired TestRestTemplate restTemplate;
    @Autowired JwtService jwtService;
    @Autowired ArchetypeRepository archetypeRepository;

    private String adminToken;
    private String organizerToken;

    @BeforeEach
    void setUp() {
        archetypeRepository.deleteAll();
        adminToken    = jwtService.generate(usuario(1L, Role.ADMIN));
        organizerToken = jwtService.generate(usuario(2L, Role.ORGANIZER));
    }

    @Test
    void organizer_crea_arquetipo_y_aparece_en_lista() {
        postArchetype("Affinity", organizerToken);

        ResponseEntity<List> response = restTemplate.getForEntity("/archetypes", List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .anyMatch(a -> "Affinity".equals(((Map<?, ?>) a).get("name")));
    }

    @Test
    void admin_tambien_puede_crear_arquetipo() {
        ResponseEntity<Map> response = postArchetype("Burn", adminToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().get("name")).isEqualTo("Burn");
    }

    @Test
    void arquetipo_inactivo_no_aparece_en_lista_publica() {
        Archetype inactivo = new Archetype();
        inactivo.setName("Walls");
        inactivo.setActive(false);
        archetypeRepository.save(inactivo);

        postArchetype("Faeries", adminToken);

        ResponseEntity<List> response = restTemplate.getForEntity("/archetypes", List.class);

        assertThat(response.getBody())
                .noneMatch(a -> "Walls".equals(((Map<?, ?>) a).get("name")))
                .anyMatch(a ->  "Faeries".equals(((Map<?, ?>) a).get("name")));
    }

    @Test
    void get_archetypes_es_publico_sin_jwt() {
        ResponseEntity<List> response = restTemplate.getForEntity("/archetypes", List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void post_archetypes_sin_jwt_retorna_401() {
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(Map.of("name", "Stompy"));

        ResponseEntity<Map> response = restTemplate.exchange("/archetypes", HttpMethod.POST, entity, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // helpers

    private ResponseEntity<Map> postArchetype(String name, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.exchange("/archetypes", HttpMethod.POST,
                new HttpEntity<>(Map.of("name", name), headers), Map.class);
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
