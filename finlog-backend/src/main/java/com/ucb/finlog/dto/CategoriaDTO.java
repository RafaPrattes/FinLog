package com.ucb.finlog.dto;

import com.ucb.finlog.model.Categoria;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CategoriaDTO {
    private Long id;
    private String nome;
    private String descricao;
    private BigDecimal totalValor = BigDecimal.ZERO;

    public static CategoriaDTO from(Categoria categoria, BigDecimal totalValor) {
        CategoriaDTO dto = new CategoriaDTO();
        dto.setId(categoria.getId());
        dto.setNome(categoria.getNome());
        dto.setDescricao(categoria.getDescricao());
        dto.setTotalValor(totalValor);
        return dto;
    }
}
