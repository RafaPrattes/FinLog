package com.ucb.finlog.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class EntidadesTest {
    @Test
    void relacionamentosBidirecionaisNaoDevemCausarRecursaoEmToStringOuHashCode() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        Categoria categoria = new Categoria();
        categoria.setUsuario(usuario);
        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setUsuario(usuario);
        movimentacao.setCategoria(categoria);
        usuario.setMovimentacoes(List.of(movimentacao));

        assertDoesNotThrow(() -> usuario.toString());
        assertDoesNotThrow(() -> usuario.hashCode());
        assertDoesNotThrow(() -> movimentacao.toString());
        assertDoesNotThrow(() -> movimentacao.hashCode());
        assertDoesNotThrow(() -> categoria.hashCode());
    }

    @Test
    void entidadesComMesmosDadosNaoDevemSerIguaisPorValor() {
        Movimentacao primeira = new Movimentacao();
        primeira.setId(1L);
        Movimentacao segunda = new Movimentacao();
        segunda.setId(1L);

        assertNotEquals(primeira, segunda);
    }
}
