package com.ucb.finlog.dto;

import com.ucb.finlog.model.TipoMovimentacao;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MovimentacaoRequest(
        String descricao,

        @NotNull(message = "Valor e obrigatorio")
        @Positive(message = "Valor deve ser positivo")
        BigDecimal valor,

        @NotNull(message = "Data e obrigatoria")
        LocalDate data,

        @NotNull(message = "Tipo e obrigatorio")
        TipoMovimentacao tipo,

        Long categoriaId,
        String categoriaNome,
        CategoriaPayload categoria
) {
    public MovimentacaoRequest(
            String descricao,
            BigDecimal valor,
            LocalDate data,
            TipoMovimentacao tipo,
            Long categoriaId,
            String categoriaNome
    ) {
        this(descricao, valor, data, tipo, categoriaId, categoriaNome, null);
    }

    public String categoriaNomeEfetivo() {
        if (categoriaNome != null) {
            return categoriaNome;
        }
        return categoria == null ? null : categoria.nome();
    }

    public record CategoriaPayload(String nome) {
    }
}
