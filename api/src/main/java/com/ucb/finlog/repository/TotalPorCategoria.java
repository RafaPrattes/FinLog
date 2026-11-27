package com.ucb.finlog.repository;

import com.ucb.finlog.model.TipoMovimentacao;

import java.math.BigDecimal;

public record TotalPorCategoria(Long categoriaId, TipoMovimentacao tipo, BigDecimal total) {
}
