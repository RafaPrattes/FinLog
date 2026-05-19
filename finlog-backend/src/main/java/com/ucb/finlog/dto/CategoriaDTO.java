package com.ucb.finlog.dto;

import lombok.Data;

@Data
public class CategoriaDTO {

    private Long id;
    private String nome;
    private String descricao;
    private int totalMovimentacoes;
    private java.math.BigDecimal totalValor;
}
