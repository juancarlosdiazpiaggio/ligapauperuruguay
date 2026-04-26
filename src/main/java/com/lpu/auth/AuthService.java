package com.lpu.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class AuthService {

    private final AppUserRepository userRepository;
    private final JwtService jwtService;
    private final WebClient webClient;
    private final String googleClientId;
    private final String tokeninfoUrl;

    public AuthService(
            AppUserRepository userRepository,
            JwtService jwtService,
            WebClient webClient,
            @Value("${lpu.google.client-id}") String googleClientId,
            @Value("${lpu.google.tokeninfo-url:https://oauth2.googleapis.com/tokeninfo}") String tokeninfoUrl) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.webClient = webClient;
        this.googleClientId = googleClientId;
        this.tokeninfoUrl = tokeninfoUrl;
    }

    public String authenticateWithGoogle(String idToken) {
        Map<String, Object> claims = verifyGoogleToken(idToken);
        String googleSub = (String) claims.get("sub");
        AppUser user = userRepository.findByGoogleSub(googleSub)
                .orElseThrow(() -> new UnauthorizedException("Usuario no registrado en LPU"));
        return jwtService.generate(user);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> verifyGoogleToken(String idToken) {
        try {
            return webClient.get()
                    .uri(tokeninfoUrl + "?id_token={token}", idToken)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError,
                              resp -> Mono.error(new UnauthorizedException("Token de Google invalido")))
                    .bodyToMono(Map.class)
                    .map(body -> {
                        if (!googleClientId.equals(body.get("aud"))) {
                            throw new UnauthorizedException("Token de Google invalido");
                        }
                        return (Map<String, Object>) body;
                    })
                    .block();
        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            throw new UnauthorizedException("Token de Google invalido");
        }
    }
}
