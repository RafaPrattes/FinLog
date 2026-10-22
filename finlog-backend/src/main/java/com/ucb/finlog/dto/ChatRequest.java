package com.ucb.finlog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRequest(
        @NotBlank(message = "Mensagem e obrigatoria")
        @Size(max = 500, message = "Mensagem deve ter no maximo 500 caracteres")
        String mensagem
) {
}
