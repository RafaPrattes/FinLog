package com.ucb.finlog.dto;

import com.ucb.finlog.model.Meta;

import java.math.BigDecimal;

public record MetaResponse(Long id, String descricao, BigDecimal valor) {
    public static MetaResponse from(Meta meta) {
        return new MetaResponse(meta.getId(), meta.getDescricao(), meta.getValor());
    }
}
