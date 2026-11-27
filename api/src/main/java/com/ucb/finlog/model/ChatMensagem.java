package com.ucb.finlog.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Mensagem da conversa com a IA. Vale pela sessao: e apagada no login e no logout do usuario.
 */
@Getter
@Setter
@Entity
@Table(name = "chat_mensagens")
public class ChatMensagem {
    public static final int TAMANHO_MAXIMO_TEXTO = 4000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PapelMensagem papel;

    @Column(nullable = false, length = TAMANHO_MAXIMO_TEXTO)
    private String texto;

    @Column(nullable = false)
    private Instant criadaEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnore
    private Usuario usuario;
}
