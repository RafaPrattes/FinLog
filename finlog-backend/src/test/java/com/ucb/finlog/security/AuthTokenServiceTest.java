package com.ucb.finlog.security;

import com.ucb.finlog.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthTokenServiceTest {
    @Test
    void deveGerarEValidarTokenComIdEVersao() {
        AuthTokenService service = serviceComConfiguracao(3600);
        Usuario usuario = usuario(7L, "maria@email.com", 4);

        String token = service.gerarToken(usuario);
        TokenValidado validado = service.validarToken(token);

        assertEquals(7L, validado.usuarioId());
        assertEquals(4, validado.versao());
        assertEquals(3600L, service.getExpirationSeconds());
    }

    @Test
    void tokenNaoDeveConterEmailEFuncionarComEmailContendoDoisPontos() {
        AuthTokenService service = serviceComConfiguracao(3600);
        Usuario usuario = usuario(7L, "\"a:b\"@email.com", 0);

        String token = service.gerarToken(usuario);
        String payload = new String(Base64.getUrlDecoder().decode(token.split("\\.")[0]), StandardCharsets.UTF_8);

        assertFalse(payload.contains("email.com"));
        assertEquals(7L, service.validarToken(token).usuarioId());
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

        String token = service.gerarToken(usuario(7L, "maria@email.com", 0));

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

        assertThrows(IllegalArgumentException.class, () -> service.validarToken(tokenAssinado(service, "1:0")));
    }

    @Test
    void deveRejeitarTokenNoFormatoAntigoComEmail() {
        AuthTokenService service = serviceComConfiguracao(3600);
        long expiracao = java.time.Instant.now().plusSeconds(3600).getEpochSecond();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.validarToken(tokenAssinado(service, "1:maria@email.com:" + expiracao))
        );
    }

    @Test
    void deveFalharQuandoNaoConseguirAssinarToken() {
        AuthTokenService service = serviceComConfiguracao(3600);
        ReflectionTestUtils.setField(service, "tokenSecret", null);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.gerarToken(usuario(7L, "maria@email.com", 0))
        );

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
        String token = service.gerarToken(usuario(7L, "maria@email.com", 0));
        String adulterado = token.substring(0, token.length() - 1) + (token.endsWith("A") ? "B" : "A");

        assertThrows(IllegalArgumentException.class, () -> service.validarToken(adulterado));
    }

    private String tokenAssinado(AuthTokenService service, String conteudo) {
        String payload = Base64.getUrlEncoder().withoutPadding().encodeToString(conteudo.getBytes(StandardCharsets.UTF_8));
        String assinatura = ReflectionTestUtils.invokeMethod(service, "sign", payload);
        return payload + "." + assinatura;
    }

    private Usuario usuario(Long id, String email, int tokenVersion) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setEmail(email);
        usuario.setTokenVersion(tokenVersion);
        return usuario;
    }

    private AuthTokenService serviceComConfiguracao(long expirationSeconds) {
        AuthTokenService service = new AuthTokenService();
        ReflectionTestUtils.setField(service, "tokenSecret", "segredo-de-teste-com-mais-de-32-caracteres");
        ReflectionTestUtils.setField(service, "expirationSeconds", expirationSeconds);
        return service;
    }
}
