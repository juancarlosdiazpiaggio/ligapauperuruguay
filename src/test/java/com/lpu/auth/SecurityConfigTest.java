package com.lpu.auth;

import com.lpu.season.SeasonController;
import com.lpu.season.SeasonService;
import com.lpu.season.Season;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SeasonController.class)
@TestPropertySource(properties = {
        "lpu.jwt.secret=test-secret-key-must-be-at-least-32-chars-long",
        "lpu.jwt.expiration-ms=3600000",
        "lpu.google.client-id=test-client-id"
})
class SecurityConfigTest {

    @Autowired MockMvc mockMvc;
    @Autowired JwtService jwtService;
    @MockBean SeasonService seasonService;
    @MockBean AuthService authService;

    @Test
    void endpoint_protegido_sin_jwt_retorna_401() throws Exception {
        mockMvc.perform(post("/seasons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"year\":2026}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void endpoint_protegido_con_rol_insuficiente_retorna_403() throws Exception {
        String jwt = jwtService.generate(usuario(Role.ORGANIZER));

        mockMvc.perform(post("/seasons")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"year\":2026}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void endpoint_protegido_con_rol_correcto_pasa_seguridad() throws Exception {
        String jwt = jwtService.generate(usuario(Role.ADMIN));
        Season season = new Season();
        season.setYear(2026);
        when(seasonService.create(anyInt())).thenReturn(season);

        mockMvc.perform(post("/seasons")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"year\":2026}"))
                .andExpect(status().isCreated());
    }

    @Test
    void endpoint_publico_sin_jwt_retorna_200() throws Exception {
        Season season = new Season();
        season.setYear(2026);
        when(seasonService.getActive()).thenReturn(season);

        mockMvc.perform(get("/seasons/active"))
                .andExpect(status().isOk());
    }

    private AppUser usuario(Role role) {
        AppUser user = new AppUser();
        user.setId(1L);
        user.setRole(role);
        user.setEmail("test@lpu.uy");
        user.setName("Test");
        user.setGoogleSub("sub-1");
        return user;
    }
}
