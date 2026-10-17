package com.pawsclaws.petshop.auth;

import com.pawsclaws.petshop.auth.dto.AuthResponse;
import com.pawsclaws.petshop.auth.dto.LoginRequest;
import com.pawsclaws.petshop.auth.dto.RegisterRequest;
import com.pawsclaws.petshop.auth.dto.UserSummary;
import com.pawsclaws.petshop.exception.ForbiddenException;
import com.pawsclaws.petshop.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final boolean signupEnabled;

    public AuthController(AuthService authService,
                          @Value("${app.signup.enabled:false}") boolean signupEnabled) {
        this.authService = authService;
        this.signupEnabled = signupEnabled;
    }

    /** Public. Authenticates a user and returns a signed JWT. */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * Criação de um novo petshop (tenant) + admin.
     *
     * <p>Desabilitada por padrão (`app.signup.enabled=false`): as contas passam
     * a ser criadas por um administrador em {@code POST /api/users}. O código
     * permanece para reactivação via configuração.</p>
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (!signupEnabled) {
            throw new ForbiddenException(
                    "A criação pública de contas está desabilitada. Peça a um administrador para criar a sua conta.");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    /** Authenticated. Returns the profile (and tenant) of the current token. */
    @GetMapping("/me")
    public ResponseEntity<UserSummary> me() {
        return ResponseEntity.ok(authService.currentUserSummary());
    }
}
