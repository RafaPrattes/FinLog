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
    private TipoMovimentacao tipo; // Crie um Enum com RECEITA, DESPESA

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}