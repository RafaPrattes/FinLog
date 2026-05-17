package com.ucb.finlog.security;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthTokenFilterTest {
    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveAutenticarQuandoTokenForValido() throws ServletException, IOException {
        AuthTokenService service = mock(AuthTokenService.class);
        when(service.validarToken("token")).thenReturn(new UsuarioAutenticado(1L, "maria@email.com"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token");

        new AuthTokenFilter(service).doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        UsuarioAutenticado principal = (UsuarioAutenticado) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        assertEquals("maria@email.com", principal.email());
    }

    @Test
    void deveIgnorarTokenInvalido() throws ServletException, IOException {
        AuthTokenService service = mock(AuthTokenService.class);
        when(service.validarToken("token")).thenThrow(new IllegalArgumentException("Token inválido"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token");

        new AuthTokenFilter(service).doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
