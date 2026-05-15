package com.ucb.finlog.dto;

import lombok.Data;

@Data
public class CategoriaDTO {

    private Long id;
    private String nome;
    private String descricao;

    // Campos calculados — preenchidos pelo service, não vêm do banco
    private int totalMovimentacoes;
    private java.math.BigDecimal totalValor;
}
