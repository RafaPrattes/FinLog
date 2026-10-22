package com.ucb.finlog.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String senha;

    /**
     * Versao dos tokens emitidos para o usuario. Incrementar invalida todos os tokens anteriores (logout).
     */
    @JsonIgnore
    @ColumnDefault("0")
    @Column(nullable = false)
    private int tokenVersion;

    @JsonIgnore
    @OneToMany(mappedBy = "usuario")
    private List<Movimentacao> movimentacoes;
}
