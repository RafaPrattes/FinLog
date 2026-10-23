package com.ucb.finlog.service;

import com.ucb.finlog.dto.CategoriaDTO;
import com.ucb.finlog.dto.CategoriaRequest;
import com.ucb.finlog.dto.CategoriaResumoDTO;
import com.ucb.finlog.model.Categoria;
import com.ucb.finlog.model.Movimentacao;
import com.ucb.finlog.model.TipoMovimentacao;
import com.ucb.finlog.model.Usuario;
import com.ucb.finlog.repository.CategoriaRepository;
import com.ucb.finlog.repository.MovimentacaoRepository;
import com.ucb.finlog.repository.TotalPorCategoria;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
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
    void deveListarApenasCategoriasDoUsuarioComTotaisEmUmaUnicaConsulta() {
        Usuario usuario = usuario("maria@email.com");
        Categoria mercado = categoria(1L, "Mercado", usuario);
        Categoria salario = categoria(2L, "Salario", usuario);
        Categoria vazia = categoria(3L, "Viagem", usuario);
        when(categoriaRepository.findByUsuarioEmailOrderByNomeAsc("maria@email.com"))
                .thenReturn(List.of(mercado, salario, vazia));
        when(movimentacaoRepository.somarPorCategoriaETipo("maria@email.com")).thenReturn(List.of(
                new TotalPorCategoria(1L, TipoMovimentacao.DESPESA, BigDecimal.TEN),
                new TotalPorCategoria(2L, TipoMovimentacao.RECEITA, new BigDecimal("100")),
                new TotalPorCategoria(2L, TipoMovimentacao.DESPESA, new BigDecimal("5"))
        ));

        List<CategoriaDTO> categorias = service.listarTodas("maria@email.com");

        assertEquals(3, categorias.size());
        assertEquals("Mercado", categorias.getFirst().getNome());
        assertEquals(BigDecimal.TEN, categorias.get(0).getTotalValor());
        assertEquals(new BigDecimal("105"), categorias.get(1).getTotalValor());
        assertEquals(BigDecimal.ZERO, categorias.get(2).getTotalValor());
        verify(movimentacaoRepository, never()).findByCategoriaIdAndUsuarioEmail(anyLong(), anyString());
    }

    @Test
    void deveCalcularResumoDeTodasAsCategoriasSemConsultaPorCategoria() {
        Usuario usuario = usuario("maria@email.com");
        when(categoriaRepository.findByUsuarioEmailOrderByNomeAsc("maria@email.com")).thenReturn(List.of(
                categoria(1L, "Mercado", usuario),
                categoria(2L, "Viagem", usuario)
        ));
        when(movimentacaoRepository.somarPorCategoriaETipo("maria@email.com")).thenReturn(List.of(
                new TotalPorCategoria(1L, TipoMovimentacao.RECEITA, new BigDecimal("100")),
                new TotalPorCategoria(1L, TipoMovimentacao.DESPESA, new BigDecimal("35")),
                new TotalPorCategoria(99L, TipoMovimentacao.DESPESA, null)
        ));

        List<CategoriaResumoDTO> resumo = service.resumo("maria@email.com");

        assertEquals(new BigDecimal("100"), resumo.get(0).getTotalReceitas());
        assertEquals(new BigDecimal("35"), resumo.get(0).getTotalDespesas());
        assertEquals(new BigDecimal("65"), resumo.get(0).getSaldo());
        assertEquals(BigDecimal.ZERO, resumo.get(1).getTotalReceitas());
        assertEquals(BigDecimal.ZERO, resumo.get(1).getSaldo());
        verify(movimentacaoRepository, never()).findByCategoriaIdAndUsuarioEmail(anyLong(), anyString());
    }

    @Test
    void deveBuscarCategoriaPorIdComTotal() {
        Categoria categoria = categoria(1L, "Mercado", usuario("maria@email.com"));
        when(categoriaRepository.findByIdAndUsuarioEmail(1L, "maria@email.com")).thenReturn(Optional.of(categoria));
        when(movimentacaoRepository.findByCategoriaIdAndUsuarioEmail(1L, "maria@email.com")).thenReturn(List.of(
                movimentacao(BigDecimal.TEN, TipoMovimentacao.DESPESA),
                movimentacao(BigDecimal.ONE, TipoMovimentacao.RECEITA)
        ));

        assertEquals(new BigDecimal("11"), service.buscarPorId(1L, "maria@email.com").getTotalValor());
    }

    @Test
    void deveCriarCategoriaParaUsuario() {
        Usuario usuario = usuario("maria@email.com");

        when(usuarioRepository.findByEmail("maria@email.com")).thenReturn(Optional.of(usuario));
        when(categoriaRepository.existsByNomeIgnoreCaseAndUsuarioEmail("Mercado", "maria@email.com")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(invocation -> {
            Categoria categoria = invocation.getArgument(0);
            categoria.setId(1L);
            return categoria;
        });

        CategoriaDTO criada = service.criar(new CategoriaRequest("  Mercado  ", "Compras do mes"), "maria@email.com");

        assertEquals("Mercado", criada.getNome());
        assertEquals("Compras do mes", criada.getDescricao());
        assertEquals(1L, criada.getId());
        assertEquals(BigDecimal.ZERO, criada.getTotalValor());
    }

    @Test
    void deveRejeitarCategoriaDuplicadaNoMesmoUsuario() {
        when(usuarioRepository.findByEmail("maria@email.com")).thenReturn(Optional.of(usuario("maria@email.com")));
        when(categoriaRepository.existsByNomeIgnoreCaseAndUsuarioEmail("Mercado", "maria@email.com")).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.criar(request("Mercado"), "maria@email.com")
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

        CategoriaDTO criada = service.criar(request("Mercado"), "joao@email.com");

        assertEquals("Mercado", criada.getNome());
    }

    @Test
    void deveRejeitarNomeEmBrancoMesmoSemValidacaoDoController() {
        when(usuarioRepository.findByEmail("maria@email.com")).thenReturn(Optional.of(usuario("maria@email.com")));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.criar(request("   "), "maria@email.com")
        );

        assertEquals(400, exception.getStatusCode().value());
    }

    @Test
    void deveAtualizarCategoriaDoUsuario() {
        Categoria categoria = categoria(1L, "Mercado", usuario("maria@email.com"));
        when(categoriaRepository.findByIdAndUsuarioEmail(1L, "maria@email.com")).thenReturn(Optional.of(categoria));
        when(categoriaRepository.findByNomeIgnoreCaseAndUsuarioEmail("Feira", "maria@email.com")).thenReturn(Optional.empty());
        when(categoriaRepository.save(categoria)).thenReturn(categoria);
        when(movimentacaoRepository.findByCategoriaIdAndUsuarioEmail(1L, "maria@email.com")).thenReturn(List.of());

        CategoriaDTO atualizada = service.atualizar(1L, new CategoriaRequest("Feira", "Hortifruti"), "maria@email.com");

        assertEquals("Feira", atualizada.getNome());
        assertEquals("Hortifruti", atualizada.getDescricao());
    }

    @Test
    void deveRejeitarAtualizacaoParaNomeDeOutraCategoria() {
        Usuario usuario = usuario("maria@email.com");
        when(categoriaRepository.findByIdAndUsuarioEmail(1L, "maria@email.com"))
                .thenReturn(Optional.of(categoria(1L, "Mercado", usuario)));
        when(categoriaRepository.findByNomeIgnoreCaseAndUsuarioEmail("Feira", "maria@email.com"))
                .thenReturn(Optional.of(categoria(2L, "Feira", usuario)));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.atualizar(1L, request("Feira"), "maria@email.com")
        );

        assertEquals(409, exception.getStatusCode().value());
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

    private CategoriaRequest request(String nome) {
        return new CategoriaRequest(nome, null);
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
