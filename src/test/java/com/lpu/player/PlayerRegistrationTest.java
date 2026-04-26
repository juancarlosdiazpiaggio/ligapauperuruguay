package com.lpu.player;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PlayerRegistrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void jugador_registrado_es_recuperable_por_id() {
        Map<String, String> request = Map.of(
                "displayName", "Nico Gomez",
                "meleeUsername", "nicogomez_" + UUID.randomUUID()
        );

        ResponseEntity<Map> created = restTemplate.postForEntity("/players", request, Map.class);

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Integer id = (Integer) created.getBody().get("id");
        assertThat(id).isNotNull();

        ResponseEntity<Map> found = restTemplate.getForEntity("/players/" + id, Map.class);

        assertThat(found.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(found.getBody().get("displayName")).isEqualTo("Nico Gomez");
    }

    @Test
    void registrar_con_melee_username_duplicado_retorna_400() {
        String username = "duplicado_" + UUID.randomUUID();
        Map<String, String> request = Map.of("displayName", "Jugador A", "meleeUsername", username);

        restTemplate.postForEntity("/players", request, Map.class);
        ResponseEntity<Map> second = restTemplate.postForEntity("/players", request, Map.class);

        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void buscar_jugador_inexistente_retorna_404() {
        ResponseEntity<Map> response = restTemplate.getForEntity("/players/999999", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void listar_jugadores_incluye_al_registrado() {
        String username = "listado_" + UUID.randomUUID();
        restTemplate.postForEntity("/players",
                Map.of("displayName", "Jugador Lista", "meleeUsername", username), Map.class);

        ResponseEntity<java.util.List> response = restTemplate.getForEntity("/players", java.util.List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .anyMatch(p -> username.equals(((Map<?, ?>) p).get("meleeUsername")));
    }

    @Test
    void registrar_con_campos_vacios_retorna_400() {
        ResponseEntity<Map> response = restTemplate.postForEntity("/players",
                Map.of("displayName", "", "meleeUsername", ""), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
