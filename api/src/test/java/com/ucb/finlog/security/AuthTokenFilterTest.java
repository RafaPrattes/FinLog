package com.ucb.finlog.security;

import com.ucb.finlog.model.Usuario;
import com.ucb.finlog.repository.UsuarioRepository;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthTokenFilterTest {
    private final AuthTokenService service = mock(AuthTokenService.class);
    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final AuthTokenFilter filter = new AuthTokenFilter(service, usuarioRepository);

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveAutenticarQuandoTokenForValidoEVersaoConferir() throws ServletException, IOException {
        when(service.validarToken("token")).thenReturn(new TokenValidado(1L, 2));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario(1L, "maria@email.com", 2)));

        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(requisicaoComToken("token"), new MockHttpServletResponse(), chain);

        UsuarioAutenticado principal = (UsuarioAutenticado) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        assertEquals(1L, principal.id());
        assertEquals("maria@email.com", principal.email());
        assertNotNull(chain.getRequest());
    }

    @Test
    void deveIgnorarTokenInvalido() throws ServletException, IOException {
        when(service.validarToken("token")).thenThrow(new IllegalArgumentException("Token inválido"));

        filter.doFilter(requisicaoComToken("token"), new MockHttpServletResponse(), new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(usuarioRepository, never()).findById(any());
    }

    @Test
    void deveRecusarTokenDeVersaoAnteriorAoLogout() throws ServletException, IOException {
        when(service.validarToken("token")).thenReturn(new TokenValidado(1L, 0));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario(1L, "maria@email.com", 1)));

        filter.doFilter(requisicaoComToken("token"), new MockHttpServletResponse(), new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void deveRecusarTokenDeUsuarioQueNaoExisteMais() throws ServletException, IOException {
        when(service.validarToken("token")).thenReturn(new TokenValidado(9L, 0));
        when(usuarioRepository.findById(9L)).thenReturn(Optional.empty());

        filter.doFilter(requisicaoComToken("token"), new MockHttpServletResponse(), new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void deveSeguirSemAutenticacaoQuandoNaoHouverBearer() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic abc");

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(service, never()).validarToken(any());
    }

    private MockHttpServletRequest requisicaoComToken(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }

    private Usuario usuario(Long id, String email, int tokenVersion) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setEmail(email);
        usuario.setTokenVersion(tokenVersion);
        return usuario;
    }
}
