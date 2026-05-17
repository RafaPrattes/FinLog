package com.ucb.finlog.repository;

import com.ucb.finlog.model.Movimentacao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Long> {

    // Filtra as movimentações para trazer apenas as do usuário logado
    List<Movimentacao> findByUsuarioId(Long usuarioId);
}