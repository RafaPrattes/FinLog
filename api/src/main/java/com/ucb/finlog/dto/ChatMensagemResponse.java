package com.ucb.finlog.dto;

import com.ucb.finlog.model.ChatMensagem;

import java.time.Instant;

public record ChatMensagemResponse(String papel, String texto, Instant criadaEm) {
    public static ChatMensagemResponse from(ChatMensagem mensagem) {
        return new ChatMensagemResponse(mensagem.getPapel().name(), mensagem.getTexto(), mensagem.getCriadaEm());
    }
}
