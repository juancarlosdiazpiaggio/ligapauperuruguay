package com.lpu.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/google")
    public ResponseEntity<TokenResponse> loginWithGoogle(@RequestBody GoogleTokenRequest request) {
        String token = authService.authenticateWithGoogle(request.idToken());
        return ResponseEntity.ok(new TokenResponse(token));
    }

    public record GoogleTokenRequest(String idToken) {}
    public record TokenResponse(String token) {}
}
