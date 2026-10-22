package com.ucb.finlog.dto;

import com.ucb.finlog.model.Movimentacao;
import com.ucb.finlog.model.TipoMovimentacao;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MovimentacaoResponse(
        Long id,
        String descricao,
        BigDecimal valor,
        LocalDate data,
        TipoMovimentacao tipo,
        CategoriaResumo categoria
) {
    public static MovimentacaoResponse from(Movimentacao movimentacao) {
        return new MovimentacaoResponse(
                movimentacao.getId(),
                movimentacao.getDescricao(),
                movimentacao.getValor(),
                movimentacao.getData(),
                movimentacao.getTipo(),
                movimentacao.getCategoria() == null ? null : CategoriaResumo.from(movimentacao.getCategoria())
        );
    }

    public record CategoriaResumo(Long id, String nome, String descricao) {
        public static CategoriaResumo from(com.ucb.finlog.model.Categoria categoria) {
            return new CategoriaResumo(categoria.getId(), categoria.getNome(), categoria.getDescricao());
        }
    }
}
