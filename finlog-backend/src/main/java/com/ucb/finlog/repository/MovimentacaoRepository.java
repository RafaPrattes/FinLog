package com.ucb.finlog.repository;

import com.ucb.finlog.model.Movimentacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Long> {
    List<Movimentacao> findByUsuarioEmail(String email);

    Optional<Movimentacao> findByIdAndUsuarioEmail(Long id, String email);
}
