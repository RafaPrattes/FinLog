package com.ucb.finlog.config;

import com.ucb.finlog.security.AuthTokenFilter;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecurityConfigTest {
    private final SecurityConfig config = new SecurityConfig(mock(AuthTokenFilter.class));

    @Test
    void deveCriarPasswordEncoderBCrypt() {
        PasswordEncoder encoder = config.passwordEncoder();

        assertTrue(encoder.matches("123456", encoder.encode("123456")));
    }

    @Test
    void userDetailsServiceDeveRecusarUsuariosCarregadosPorUsername() {
        assertInstanceOf(
                UsernameNotFoundException.class,
                assertThrows(UsernameNotFoundException.class, () -> config.userDetailsService().loadUserByUsername("maria"))
        );
    }

    @Test
    void deveEscreverErroDeAutenticacaoEmJson() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        config.escreverErro(response, HttpStatus.UNAUTHORIZED, "Token \"ausente\" ou inválido", "/api\\usuarios");

        assertEquals(401, response.getStatus());
        assertEquals("application/json", response.getContentType());
        assertEquals(
                "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Token \\\"ausente\\\" ou inválido\",\"path\":\"/api\\\\usuarios\"}",
                response.getContentAsString()
        );
    }

    @Test
    void authenticationEntryPointDeveEscreverErroComPathDaRequisicao() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/usuarios");
        MockHttpServletResponse response = new MockHttpServletResponse();

        config.authenticationEntryPoint().commence(request, response, new org.springframework.security.core.AuthenticationException("falha") {});

        assertEquals(401, response.getStatus());
        assertEquals(
                "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Token ausente ou inválido\",\"path\":\"/api/usuarios\"}",
                response.getContentAsString()
        );
    }

    @Test
    void accessDeniedHandlerDeveEscreverErroComPathDaRequisicao() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/admin");
        MockHttpServletResponse response = new MockHttpServletResponse();

        config.accessDeniedHandler().handle(request, response, new org.springframework.security.access.AccessDeniedException("negado"));

        assertEquals(403, response.getStatus());
        assertEquals(
                "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Acesso negado\",\"path\":\"/api/admin\"}",
                response.getContentAsString()
        );
    }

    @Test
    void deveEscaparValorNuloComoStringVazia() {
        assertEquals("", config.escapeJson(null));
    }

    @Test
    void buildDeveDelegarParaHttpSecurity() throws Exception {
        org.springframework.security.config.annotation.web.builders.HttpSecurity httpSecurity = mock(org.springframework.security.config.annotation.web.builders.HttpSecurity.class);
        DefaultSecurityFilterChain filterChain = mock(DefaultSecurityFilterChain.class);
        when(httpSecurity.build()).thenReturn(filterChain);

        assertEquals(filterChain, config.build(httpSecurity));
    }

    @Test
    void deveCriarSecurityFilterChain() throws Exception {
        ObjectPostProcessor<Object> postProcessor = new ObjectPostProcessor<>() {
            @Override
            public <O> O postProcess(O object) {
                return object;
            }
        };
        AuthenticationManagerBuilder authenticationBuilder = new AuthenticationManagerBuilder(postProcessor);
        HttpSecurity http = new HttpSecurity(postProcessor, authenticationBuilder, new java.util.HashMap<>());
        StaticApplicationContext context = new StaticApplicationContext();
        http.setSharedObject(ApplicationContext.class, context);

        assertInstanceOf(SecurityFilterChain.class, config.securityFilterChain(http));
    }
}
