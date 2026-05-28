package com.ucb.finlog.service;

import com.ucb.finlog.dto.CategoriaDTO;
import com.ucb.finlog.dto.CategoriaResumoDTO;
import com.ucb.finlog.model.Categoria;
import com.ucb.finlog.model.Movimentacao;
import com.ucb.finlog.model.TipoMovimentacao;
import com.ucb.finlog.model.Usuario;
import com.ucb.finlog.repository.CategoriaRepository;
import com.ucb.finlog.repository.MovimentacaoRepository;
import com.ucb.finlog.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {
    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private MovimentacaoRepository movimentacaoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CategoriaService service;

    @Test
    void deveListarApenasCategoriasDoUsuario() {
        Categoria categoria = categoria(1L, "Mercado", usuario("maria@email.com"));
        when(categoriaRepository.findByUsuarioEmailOrderByNomeAsc("maria@email.com")).thenReturn(List.of(categoria));
        when(movimentacaoRepository.findByCategoriaIdAndUsuarioEmail(1L, "maria@email.com")).thenReturn(List.of(movimentacao(BigDecimal.TEN, TipoMovimentacao.DESPESA)));

        List<CategoriaDTO> categorias = service.listarTodas("maria@email.com");

        assertEquals(1, categorias.size());
        assertEquals("Mercado", categorias.getFirst().getNome());
        assertEquals(BigDecimal.TEN, categorias.getFirst().getTotalValor());
    }

    @Test
    void deveCriarCategoriaParaUsuario() {
        Usuario usuario = usuario("maria@email.com");
        CategoriaDTO dto = dto("Mercado");

        when(usuarioRepository.findByEmail("maria@email.com")).thenReturn(Optional.of(usuario));
        when(categoriaRepository.existsByNomeIgnoreCaseAndUsuarioEmail("Mercado", "maria@email.com")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(invocation -> {
            Categoria categoria = invocation.getArgument(0);
            categoria.setId(1L);
            return categoria;
        });
        when(movimentacaoRepository.findByCategoriaIdAndUsuarioEmail(1L, "maria@email.com")).thenReturn(List.of());

        CategoriaDTO criada = service.criar(dto, "maria@email.com");

        assertEquals("Mercado", criada.getNome());
        assertEquals(1L, criada.getId());
    }

    @Test
    void deveRejeitarCategoriaDuplicadaNoMesmoUsuario() {
        when(usuarioRepository.findByEmail("maria@email.com")).thenReturn(Optional.of(usuario("maria@email.com")));
        when(categoriaRepository.existsByNomeIgnoreCaseAndUsuarioEmail("Mercado", "maria@email.com")).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.criar(dto("Mercado"), "maria@email.com")
        );

        assertEquals(409, exception.getStatusCode().value());
    }

    @Test
    void devePermitirMesmoNomeParaUsuariosDiferentesPelaConsultaDoUsuarioAtual() {
        when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario("joao@email.com")));
        when(categoriaRepository.existsByNomeIgnoreCaseAndUsuarioEmail("Mercado", "joao@email.com")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(invocation -> {
            Categoria categoria = invocation.getArgument(0);
            categoria.setId(2L);
            return categoria;
        });
        when(movimentacaoRepository.findByCategoriaIdAndUsuarioEmail(2L, "joao@email.com")).thenReturn(List.of());

        CategoriaDTO criada = service.criar(dto("Mercado"), "joao@email.com");

        assertEquals("Mercado", criada.getNome());
    }

    @Test
    void deveCalcularResumoComMovimentacoesPersistidasDoUsuario() {
        Categoria categoria = categoria(1L, "Mercado", usuario("maria@email.com"));
        when(categoriaRepository.findByIdAndUsuarioEmail(1L, "maria@email.com")).thenReturn(Optional.of(categoria));
        when(movimentacaoRepository.findByCategoriaIdAndUsuarioEmail(1L, "maria@email.com")).thenReturn(List.of(
                movimentacao(BigDecimal.valueOf(100), TipoMovimentacao.RECEITA),
                movimentacao(BigDecimal.valueOf(35), TipoMovimentacao.DESPESA)
        ));

        CategoriaResumoDTO resumo = service.resumoPorId(1L, "maria@email.com");

        assertEquals(BigDecimal.valueOf(100), resumo.getTotalReceitas());
        assertEquals(BigDecimal.valueOf(35), resumo.getTotalDespesas());
        assertEquals(BigDecimal.valueOf(65), resumo.getSaldo());
    }

    @Test
    void deveImpedirExclusaoComMovimentacoesDoUsuario() {
        Categoria categoria = categoria(1L, "Mercado", usuario("maria@email.com"));
        when(categoriaRepository.findByIdAndUsuarioEmail(1L, "maria@email.com")).thenReturn(Optional.of(categoria));
        when(movimentacaoRepository.existsByCategoriaIdAndUsuarioEmail(1L, "maria@email.com")).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.deletar(1L, "maria@email.com")
        );

        assertEquals(409, exception.getStatusCode().value());
    }

    @Test
    void deveExcluirCategoriaSemMovimentacoes() {
        Categoria categoria = categoria(1L, "Mercado", usuario("maria@email.com"));
        when(categoriaRepository.findByIdAndUsuarioEmail(1L, "maria@email.com")).thenReturn(Optional.of(categoria));
        when(movimentacaoRepository.existsByCategoriaIdAndUsuarioEmail(1L, "maria@email.com")).thenReturn(false);

        service.deletar(1L, "maria@email.com");

        verify(categoriaRepository).delete(categoria);
    }

    private CategoriaDTO dto(String nome) {
        CategoriaDTO dto = new CategoriaDTO();
        dto.setNome(nome);
        return dto;
    }

    private Categoria categoria(Long id, String nome, Usuario usuario) {
        Categoria categoria = new Categoria();
        categoria.setId(id);
        categoria.setNome(nome);
        categoria.setUsuario(usuario);
        return categoria;
    }

    private Usuario usuario(String email) {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail(email);
        return usuario;
    }

    private Movimentacao movimentacao(BigDecimal valor, TipoMovimentacao tipo) {
        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setValor(valor);
        movimentacao.setTipo(tipo);
        return movimentacao;
    }
}
