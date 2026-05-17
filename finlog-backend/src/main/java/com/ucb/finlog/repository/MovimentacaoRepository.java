package com.ucb.finlog.repository;

import com.ucb.finlog.model.Movimentacao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Long> {

    // Filtra as movimentações para trazer apenas as do usuário logado
    List<Movimentacao> findByUsuarioId(Long usuarioId);

    //Calculo para mostrar o saldo atualizado subtraindo despesas de receitas
    @Query("SELECT COALESCE(SUM(CASE WHEN m.tipo = 'RECEITA' THEN m.valor ELSE -m.valor END), 0) FROM Movimentacao m WHERE m.usuario.id = :usuarioId")
    Double calcularSaldoPorUsuario(@Param("usuarioId") Long usuarioId);
}