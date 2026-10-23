package com.ucb.finlog.dto;

import com.ucb.finlog.model.TipoMovimentacao;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MovimentacaoRequest(
        @Size(max = 255, message = "Descricao deve ter no maximo 255 caracteres")
        String descricao,

        @NotNull(message = "Valor e obrigatorio")
        @Positive(message = "Valor deve ser positivo")
        @Digits(integer = 13, fraction = 2, message = "Valor invalido")
        BigDecimal valor,

        @NotNull(message = "Data e obrigatoria")
        LocalDate data,

        @NotNull(message = "Tipo e obrigatorio")
        TipoMovimentacao tipo,

        Long categoriaId,

        @Size(max = 100, message = "Nome da categoria deve ter no maximo 100 caracteres")
        String categoriaNome,

        @Valid
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

    public record CategoriaPayload(
            @Size(max = 100, message = "Nome da categoria deve ter no maximo 100 caracteres")
            String nome
    ) {
    }
}
