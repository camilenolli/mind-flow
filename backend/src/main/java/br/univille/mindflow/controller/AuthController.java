package br.univille.mindflow.controller;

import br.univille.mindflow.dto.AuthResponse;
import br.univille.mindflow.dto.LoginRequest;
import br.univille.mindflow.dto.RegisterRequest;
import br.univille.mindflow.dto.UserDTO;
import br.univille.mindflow.security.UserPrincipal;
import br.univille.mindflow.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Cadastro, login e usuário atual (JWT stateless)")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) { this.auth = auth; }

    @PostMapping("/register")
    @Operation(summary = "Cria conta nova e retorna o token JWT")
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        return auth.register(req);
    }

    @PostMapping("/login")
    @Operation(summary = "Autentica e retorna o token JWT")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return auth.login(req);
    }

    @GetMapping("/me")
    @Operation(summary = "Retorna o usuário do token atual")
    public ResponseEntity<UserDTO> me(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(new UserDTO(principal.getUser()));
    }
}
