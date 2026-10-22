package com.ucb.finlog.security;

import com.ucb.finlog.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthTokenServiceTest {
    @Test
    void deveGerarEValidarToken() {
        AuthTokenService service = serviceComConfiguracao(3600);
        Usuario usuario = new Usuario();
        usuario.setId(7L);
        usuario.setEmail("maria@email.com");

        String token = service.gerarToken(usuario);
        UsuarioAutenticado autenticado = service.validarToken(token);

        assertEquals(7L, autenticado.id());
        assertEquals("maria@email.com", autenticado.email());
        assertEquals(3600L, service.getExpirationSeconds());
    }

    @Test
    void deveRejeitarTokenComAssinaturaInvalida() {
        AuthTokenService service = serviceComConfiguracao(3600);

        assertThrows(IllegalArgumentException.class, () -> service.validarToken("payload.assinatura"));
    }

    @Test
    void deveRejeitarTokenSemDuasPartes() {
        AuthTokenService service = serviceComConfiguracao(3600);

        assertThrows(IllegalArgumentException.class, () -> service.validarToken("token-sem-assinatura"));
    }

    @Test
    void deveRejeitarTokenExpirado() {
        AuthTokenService service = serviceComConfiguracao(-1);
        Usuario usuario = new Usuario();
        usuario.setId(7L);
        usuario.setEmail("maria@email.com");

        String token = service.gerarToken(usuario);

        assertThrows(IllegalArgumentException.class, () -> service.validarToken(token));
    }

    @Test
    void deveRejeitarPayloadMalformado() {
        AuthTokenService service = serviceComConfiguracao(3600);
        String payload = Base64.getUrlEncoder().withoutPadding().encodeToString("invalido".getBytes());

        assertThrows(IllegalArgumentException.class, () -> service.validarToken(payload + ".assinatura"));
    }

    @Test
    void deveRejeitarPayloadAssinadoComQuantidadeDeCamposInvalida() {
        AuthTokenService service = serviceComConfiguracao(3600);
        String payload = Base64.getUrlEncoder().withoutPadding().encodeToString("1:maria@email.com".getBytes());
        String assinatura = ReflectionTestUtils.invokeMethod(service, "sign", payload);

        assertThrows(IllegalArgumentException.class, () -> service.validarToken(payload + "." + assinatura));
    }

    @Test
    void deveFalharQuandoNaoConseguirAssinarToken() {
        AuthTokenService service = serviceComConfiguracao(3600);
        ReflectionTestUtils.setField(service, "tokenSecret", null);
        Usuario usuario = new Usuario();
        usuario.setId(7L);
        usuario.setEmail("maria@email.com");

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> service.gerarToken(usuario));

        assertEquals("Nao foi possivel assinar o token", exception.getMessage());
        assertInstanceOf(NullPointerException.class, exception.getCause());
    }

    @Test
    void deveAceitarSegredoForte() {
        AuthTokenService service = serviceComConfiguracao(3600);

        assertDoesNotThrow(service::validarSegredo);
    }

    @Test
    void deveRecusarSegredoAusenteCurtoOuDeExemplo() {
        for (String segredo : new String[]{null, "curto", "change-me-to-a-long-random-secret-0123456789"}) {
            AuthTokenService service = serviceComConfiguracao(3600);
            ReflectionTestUtils.setField(service, "tokenSecret", segredo);

            assertThrows(IllegalStateException.class, service::validarSegredo);
        }
    }

    @Test
    void deveRejeitarTokenComAssinaturaAdulterada() {
        AuthTokenService service = serviceComConfiguracao(3600);
        Usuario usuario = new Usuario();
        usuario.setId(7L);
        usuario.setEmail("maria@email.com");
        String token = service.gerarToken(usuario);
        String adulterado = token.substring(0, token.length() - 1) + (token.endsWith("A") ? "B" : "A");

        assertThrows(IllegalArgumentException.class, () -> service.validarToken(adulterado));
    }

    private AuthTokenService serviceComConfiguracao(long expirationSeconds) {
        AuthTokenService service = new AuthTokenService();
        ReflectionTestUtils.setField(service, "tokenSecret", "segredo-de-teste-com-mais-de-32-caracteres");
        ReflectionTestUtils.setField(service, "expirationSeconds", expirationSeconds);
        return service;
    }
}
