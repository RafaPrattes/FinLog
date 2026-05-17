package com.ucb.finlog.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
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

    @Column(nullable = false)
    private String senha; // RNF003: Segurança com Hash [cite: 484]

    @JsonManagedReference // Indica que este é o lado principal que deve ser exibido
    @OneToMany(mappedBy = "usuario")
    private List<Movimentacao> movimentacoes;
}
