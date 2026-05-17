package com.ucb.finlog.service;

import com.ucb.finlog.dto.AuthResponse;
import com.ucb.finlog.dto.LoginRequest;
import com.ucb.finlog.dto.UsuarioResponse;
import com.ucb.finlog.model.Usuario;
import com.ucb.finlog.repository.UsuarioRepository;
import com.ucb.finlog.security.AuthTokenService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenService authTokenService;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, AuthTokenService authTokenService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.authTokenService = authTokenService;
    }

    public AuthResponse login(LoginRequest request) {
        if (request == null || request.email() == null || request.senha() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail e senha são obrigatórios");
        }

        Usuario usuario = usuarioRepository.findByEmail(request.email().trim().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas"));

        if (!passwordEncoder.matches(request.senha(), usuario.getSenha())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
        }

        return criarResposta(usuario);
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
