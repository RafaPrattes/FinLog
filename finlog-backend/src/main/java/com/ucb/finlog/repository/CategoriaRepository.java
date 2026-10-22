package com.ucb.finlog.repository;

import com.ucb.finlog.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    List<Categoria> findByUsuarioEmailOrderByNomeAsc(String email);

    Optional<Categoria> findByIdAndUsuarioEmail(Long id, String email);

    Optional<Categoria> findByNomeIgnoreCaseAndUsuarioEmail(String nome, String email);

    boolean existsByNomeIgnoreCaseAndUsuarioEmail(String nome, String email);
}
