package com.ucb.finlog.service;

import com.ucb.finlog.model.Movimentacao;
import com.ucb.finlog.model.Usuario;
import com.ucb.finlog.repository.MovimentacaoRepository;
import com.ucb.finlog.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovimentacaoServiceTest {
    @Mock
    private MovimentacaoRepository repository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private MovimentacaoService service;

    @Test
    void deveListarMovimentacoesDoUsuario() {
        Movimentacao movimentacao = new Movimentacao();
        when(repository.findByUsuarioEmail("maria@email.com")).thenReturn(List.of(movimentacao));

        assertEquals(List.of(movimentacao), service.listarTodas("maria@email.com"));
    }

    @Test
    void deveSalvarMovimentacaoAssociadaAoUsuario() {
        Usuario usuario = new Usuario();
        Movimentacao movimentacao = new Movimentacao();

        when(usuarioRepository.findByEmail("maria@email.com")).thenReturn(Optional.of(usuario));
        when(repository.save(movimentacao)).thenReturn(movimentacao);

        Movimentacao salva = service.salvar(movimentacao, "maria@email.com");

        assertEquals(usuario, salva.getUsuario());
        assertEquals(movimentacao, salva);
    }

    @Test
    void deveFalharAoSalvarQuandoUsuarioNaoExiste() {
        Movimentacao movimentacao = new Movimentacao();
        when(usuarioRepository.findByEmail("maria@email.com")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.salvar(movimentacao, "maria@email.com")
        );

        assertEquals(404, exception.getStatusCode().value());
        assertEquals("Usuário não encontrado", exception.getReason());
    }

    @Test
    void deveBuscarMovimentacaoPorIdDoUsuario() {
        Movimentacao movimentacao = new Movimentacao();
        when(repository.findByIdAndUsuarioEmail(10L, "maria@email.com")).thenReturn(Optional.of(movimentacao));

        assertEquals(movimentacao, service.buscarPorId(10L, "maria@email.com"));
    }

    @Test
    void deveFalharQuandoMovimentacaoNaoExiste() {
        when(repository.findByIdAndUsuarioEmail(10L, "maria@email.com")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.buscarPorId(10L, "maria@email.com")
        );

        assertEquals(404, exception.getStatusCode().value());
        assertEquals("Movimentação não encontrada", exception.getReason());
    }
}
