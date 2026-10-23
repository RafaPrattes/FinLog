package com.ucb.finlog.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limita tentativas de login por e-mail em memoria: apos {@value #MAX_TENTATIVAS} falhas
 * dentro da janela, novas tentativas sao recusadas com 429 ate a janela expirar.
 * Em um cenario com varias instancias do backend o limite vale por instancia.
 */
@Component
public class LoginAttemptService {
    static final int MAX_TENTATIVAS = 5;
    static final Duration JANELA = Duration.ofMinutes(15);
    static final int LIMITE_REGISTROS_ANTES_DA_LIMPEZA = 10_000;
    static final String MENSAGEM_BLOQUEIO = "Muitas tentativas. Tente novamente em alguns minutos.";

    private final Clock clock;
    private final Map<String, Tentativas> tentativas = new ConcurrentHashMap<>();

    @Autowired
    public LoginAttemptService() {
        this(Clock.systemUTC());
    }

    LoginAttemptService(Clock clock) {
        this.clock = clock;
    }

    public void verificarBloqueio(String email) {
        String chave = normalizar(email);
        Tentativas atual = tentativas.get(chave);
        if (atual == null) {
            return;
        }
        if (expirou(atual)) {
            tentativas.remove(chave, atual);
            return;
        }
        if (atual.falhas() >= MAX_TENTATIVAS) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, MENSAGEM_BLOQUEIO);
        }
    }

    public void registrarFalha(String email) {
        if (tentativas.size() >= LIMITE_REGISTROS_ANTES_DA_LIMPEZA) {
            tentativas.values().removeIf(this::expirou);
        }
        tentativas.compute(normalizar(email), (chave, atual) -> atual == null || expirou(atual)
                ? new Tentativas(1, clock.instant())
                : new Tentativas(atual.falhas() + 1, atual.inicio()));
    }

    public void registrarSucesso(String email) {
        tentativas.remove(normalizar(email));
    }

    int quantidadeRegistros() {
        return tentativas.size();
    }

    private boolean expirou(Tentativas registro) {
        return !clock.instant().isBefore(registro.inicio().plus(JANELA));
    }

    private String normalizar(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private record Tentativas(int falhas, Instant inicio) {
    }
}
