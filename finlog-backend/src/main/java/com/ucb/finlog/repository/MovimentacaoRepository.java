package com.ucb.finlog.repository;

import com.ucb.finlog.model.Movimentacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Long> {
    List<Movimentacao> findByUsuarioEmail(String email);

    Optional<Movimentacao> findByIdAndUsuarioEmail(Long id, String email);

    List<Movimentacao> findByCategoriaIdAndUsuarioEmail(Long categoriaId, String email);

    boolean existsByCategoriaIdAndUsuarioEmail(Long categoriaId, String email);

    @Query("""
            select new com.ucb.finlog.repository.TotalPorCategoria(m.categoria.id, m.tipo, sum(m.valor))
            from Movimentacao m
            where m.usuario.email = :email and m.categoria is not null
            group by m.categoria.id, m.tipo
            """)
    List<TotalPorCategoria> somarPorCategoriaETipo(@Param("email") String email);
}
