package com.ucb.finlog.security;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoginAttemptServiceTest {
    private final RelogioAjustavel relogio = new RelogioAjustavel(Instant.parse("2026-10-22T12:00:00Z"));
    private final LoginAttemptService service = new LoginAttemptService(relogio);

    @Test
    void devePermitirAteQuatroFalhasSemBloquear() {
        falhar("maria@email.com", LoginAttemptService.MAX_TENTATIVAS - 1);

        assertDoesNotThrow(() -> service.verificarBloqueio("maria@email.com"));
    }

    @Test
    void deveBloquearComStatus429AposCincoFalhasNaJanela() {
        falhar("maria@email.com", LoginAttemptService.MAX_TENTATIVAS);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.verificarBloqueio("maria@email.com")
        );

        assertEquals(429, exception.getStatusCode().value());
        assertEquals("Muitas tentativas. Tente novamente em alguns minutos.", exception.getReason());
    }

    @Test
    void deveNormalizarEmailAoContarTentativas() {
        falhar(" Maria@Email.COM ", 3);
        falhar("maria@email.com", 2);

        assertThrows(ResponseStatusException.class, () -> service.verificarBloqueio("MARIA@email.com"));
    }

    @Test
    void deveLiberarAposAJanelaExpirar() {
        falhar("maria@email.com", LoginAttemptService.MAX_TENTATIVAS);
        relogio.avancar(LoginAttemptService.JANELA.minusSeconds(1));
        assertThrows(ResponseStatusException.class, () -> service.verificarBloqueio("maria@email.com"));

        relogio.avancar(Duration.ofSeconds(1));

        assertDoesNotThrow(() -> service.verificarBloqueio("maria@email.com"));
        assertEquals(0, service.quantidadeRegistros());
    }

    @Test
    void falhaAposJanelaExpiradaDeveReiniciarContagem() {
        falhar("maria@email.com", LoginAttemptService.MAX_TENTATIVAS - 1);
        relogio.avancar(LoginAttemptService.JANELA);

        falhar("maria@email.com", LoginAttemptService.MAX_TENTATIVAS - 1);

        assertDoesNotThrow(() -> service.verificarBloqueio("maria@email.com"));
    }

    @Test
    void sucessoDeveZerarTentativas() {
        falhar("maria@email.com", LoginAttemptService.MAX_TENTATIVAS - 1);
        service.registrarSucesso("maria@email.com");
        falhar("maria@email.com", LoginAttemptService.MAX_TENTATIVAS - 1);

        assertDoesNotThrow(() -> service.verificarBloqueio("maria@email.com"));
    }

    @Test
    void bloqueioDeUmEmailNaoAfetaOutro() {
        falhar("maria@email.com", LoginAttemptService.MAX_TENTATIVAS);

        assertDoesNotThrow(() -> service.verificarBloqueio("joao@email.com"));
        assertDoesNotThrow(() -> service.verificarBloqueio(null));
    }

    @Test
    void deveLimparRegistrosExpiradosQuandoMapaCrescerDemais() {
        for (int i = 0; i < LoginAttemptService.LIMITE_REGISTROS_ANTES_DA_LIMPEZA; i++) {
            service.registrarFalha("usuario" + i + "@email.com");
        }
        relogio.avancar(LoginAttemptService.JANELA);

        service.registrarFalha("novo@email.com");

        assertEquals(1, service.quantidadeRegistros());
    }

    @Test
    void construtorPadraoDeveUsarRelogioDoSistema() {
        assertDoesNotThrow(() -> new LoginAttemptService().verificarBloqueio("maria@email.com"));
    }

    private void falhar(String email, int vezes) {
        for (int i = 0; i < vezes; i++) {
            service.registrarFalha(email);
        }
    }

    private static final class RelogioAjustavel extends Clock {
        private Instant agora;

        private RelogioAjustavel(Instant agora) {
            this.agora = agora;
        }

        void avancar(Duration duracao) {
            agora = agora.plus(duracao);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return agora;
        }
    }
}
