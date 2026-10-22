package com.ucb.finlog.service;

import com.ucb.finlog.dto.AuthResponse;
import com.ucb.finlog.dto.LoginRequest;
import com.ucb.finlog.dto.UsuarioResponse;
import com.ucb.finlog.model.Usuario;
import com.ucb.finlog.repository.UsuarioRepository;
import com.ucb.finlog.security.AuthTokenService;
import com.ucb.finlog.security.LoginAttemptService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class AuthService {
    /**
     * Hash BCrypt (custo 10, igual ao PasswordEncoder) de uma senha aleatoria. E comparado quando o
     * e-mail nao existe, para que a resposta leve o mesmo tempo e nao revele quais e-mails estao cadastrados.
     */
    static final String HASH_FICTICIO = "$2a$10$CUipds4dSxzC24ATY3oT7ewTnU2pt.hteah.AMZydJfWMoANo5hZa";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenService authTokenService;
    private final LoginAttemptService loginAttemptService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            AuthTokenService authTokenService,
            LoginAttemptService loginAttemptService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.authTokenService = authTokenService;
        this.loginAttemptService = loginAttemptService;
    }

    public AuthResponse login(LoginRequest request) {
        if (request == null || request.email() == null || request.senha() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail e senha são obrigatórios");
        }

        String email = request.email().trim().toLowerCase();
        loginAttemptService.verificarBloqueio(email);

        Optional<Usuario> usuario = usuarioRepository.findByEmail(email);
        String hash = usuario.map(Usuario::getSenha).orElse(HASH_FICTICIO);
        boolean senhaConfere = passwordEncoder.matches(request.senha(), hash);

        if (usuario.isEmpty() || !senhaConfere) {
            loginAttemptService.registrarFalha(email);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
        }

        loginAttemptService.registrarSucesso(email);
        return criarResposta(usuario.get());
    }

    private AuthResponse criarResposta(Usuario usuario) {
        return new AuthResponse(
                UsuarioResponse.from(usuario),
                authTokenService.gerarToken(usuario),
                "Bearer",
                authTokenService.getExpirationSeconds()
        );
    }
}
