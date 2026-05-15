package com.ucb.finlog.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "movimentacoes")
public class Movimentacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String descricao;

    @Column(nullable = false)
    private BigDecimal valor;

    @Column(nullable = false)
    private LocalDate data;

    @Enumerated(EnumType.STRING)
    private TipoMovimentacao tipo; // RECEITA ou DESPESA

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    // TODO: quando o banco estiver pronto, descomentar o relacionamento abaixo
    // e remover o campo categoriaId
    // @ManyToOne
    // @JoinColumn(name = "categoria_id")
    // private Categoria categoria;

    @Column(name = "categoria_id")
    private Long categoriaId;
}
