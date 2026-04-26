package com.lpu.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository userRepository;
    private final JwtService jwtService;
    private final WebClient webClient = WebClient.create();

    @Value("${lpu.google.client-id}")
    private String googleClientId;

    public String authenticateWithGoogle(String idToken) {
        Map<String, Object> claims = verifyGoogleToken(idToken);

        String googleSub = (String) claims.get("sub");
        String email = (String) claims.get("email");
        String name = (String) claims.get("name");

        AppUser user = userRepository.findByGoogleSub(googleSub)
                .orElseThrow(() -> new UnauthorizedException("Usuario no registrado en LPU"));

        return jwtService.generate(user);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> verifyGoogleToken(String idToken) {
        // Verifica el ID token contra el endpoint de Google
        return webClient.get()
                .uri("https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken)
                .retrieve()
                .bodyToMono(Map.class)
                .map(body -> {
                    if (!googleClientId.equals(body.get("aud"))) {
                        throw new UnauthorizedException("Token de Google invalido");
                    }
                    return (Map<String, Object>) body;
                })
                .block();
    }
}
