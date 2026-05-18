package com.ucb.finlog.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * Resumo de gastos/receitas agrupados por categoria.
 * Retornado pelo endpoint GET /api/categorias/resumo
 */
@Data
public class CategoriaResumoDTO {

    private Long categoriaId;
    private String categoriaNome;

    private int totalMovimentacoes;
    private BigDecimal totalReceitas;
    private BigDecimal totalDespesas;
    private BigDecimal saldo; // receitas - despesas
}
