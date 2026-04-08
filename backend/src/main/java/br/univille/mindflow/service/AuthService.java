package br.univille.mindflow.service;

import br.univille.mindflow.dto.AuthResponse;
import br.univille.mindflow.dto.LoginRequest;
import br.univille.mindflow.dto.RegisterRequest;
import br.univille.mindflow.dto.UserDTO;
import br.univille.mindflow.model.User;
import br.univille.mindflow.repository.UserRepository;
import br.univille.mindflow.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
@Transactional
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    public AuthResponse register(RegisterRequest req) {
        if (users.existsByEmailIgnoreCase(req.getEmail())) {
            throw new ResponseStatusException(CONFLICT, "Email já cadastrado");
        }
        User u = new User();
        u.setEmail(req.getEmail().toLowerCase().trim());
        u.setName(req.getName().trim());
        u.setPasswordHash(encoder.encode(req.getPassword()));
        users.save(u);
        return buildResponse(u);
    }

    public AuthResponse login(LoginRequest req) {
        User u = users.findByEmailIgnoreCase(req.getEmail())
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Credenciais inválidas"));
        if (!encoder.matches(req.getPassword(), u.getPasswordHash())) {
            throw new ResponseStatusException(UNAUTHORIZED, "Credenciais inválidas");
        }
        return buildResponse(u);
    }

    private AuthResponse buildResponse(User u) {
        return new AuthResponse(jwt.generate(u), jwt.getExpirationMs(), new UserDTO(u));
    }
}
