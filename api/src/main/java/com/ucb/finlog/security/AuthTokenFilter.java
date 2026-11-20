package com.ucb.finlog.security;

import com.ucb.finlog.model.Usuario;
import com.ucb.finlog.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {
    private final AuthTokenService authTokenService;
    private final UsuarioRepository usuarioRepository;

    public AuthTokenFilter(AuthTokenService authTokenService, UsuarioRepository usuarioRepository) {
        this.authTokenService = authTokenService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");

        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            try {
                UsuarioAutenticado usuario = autenticar(token);
                var authentication = new UsernamePasswordAuthenticationToken(usuario, null, List.of());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (IllegalArgumentException exception) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private UsuarioAutenticado autenticar(String token) {
        TokenValidado tokenValidado = authTokenService.validarToken(token);
        Usuario usuario = usuarioRepository.findById(tokenValidado.usuarioId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario do token nao existe"));

        if (usuario.getTokenVersion() != tokenValidado.versao()) {
            throw new IllegalArgumentException("Token revogado");
        }

        return new UsuarioAutenticado(usuario.getId(), usuario.getEmail());
    }
}
