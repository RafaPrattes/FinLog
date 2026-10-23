package com.ucb.finlog.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record MetaRequest(
        @NotBlank(message = "Descricao e obrigatoria")
        @Size(max = 255, message = "Descricao deve ter no maximo 255 caracteres")
        String descricao,

        @NotNull(message = "Valor e obrigatorio")
        @Positive(message = "Valor deve ser positivo")
        @Digits(integer = 13, fraction = 2, message = "Valor invalido")
        BigDecimal valor
) {
}
