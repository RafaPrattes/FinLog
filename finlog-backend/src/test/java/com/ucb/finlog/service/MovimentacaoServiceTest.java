package com.ucb.finlog.service;

import com.ucb.finlog.dto.MovimentacaoRequest;
import com.ucb.finlog.model.Categoria;
import com.ucb.finlog.model.Movimentacao;
import com.ucb.finlog.model.TipoMovimentacao;
import com.ucb.finlog.model.Usuario;
import com.ucb.finlog.repository.CategoriaRepository;
import com.ucb.finlog.repository.MovimentacaoRepository;
import com.ucb.finlog.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovimentacaoServiceTest {
    @Mock
    private MovimentacaoRepository repository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

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
        Usuario usuario = usuario(1L, "maria@email.com");
        MovimentacaoRequest request = request(null, null);

        when(usuarioRepository.findByEmail("maria@email.com")).thenReturn(Optional.of(usuario));
        when(repository.save(org.mockito.ArgumentMatchers.any(Movimentacao.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Movimentacao salva = service.salvar(request, "maria@email.com");

        assertEquals(usuario, salva.getUsuario());
        assertEquals("Salario", salva.getDescricao());
        assertEquals(new BigDecimal("100.00"), salva.getValor());
        assertEquals(LocalDate.of(2026, 5, 27), salva.getData());
        assertEquals(TipoMovimentacao.RECEITA, salva.getTipo());
        assertNull(salva.getCategoria());
    }

    @Test
    void deveFalharAoSalvarQuandoUsuarioNaoExiste() {
        when(usuarioRepository.findByEmail("maria@email.com")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.salvar(request(null, null), "maria@email.com")
        );

        assertEquals(404, exception.getStatusCode().value());
        assertEquals("Usuario nao encontrado", exception.getReason());
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
        assertEquals("Movimentacao nao encontrada", exception.getReason());
    }

    @Test
    void deveAtualizarMovimentacaoExistenteDoUsuario() {
        Usuario usuario = usuario(1L, "maria@email.com");
        Movimentacao existente = new Movimentacao();
        existente.setUsuario(usuario);
        MovimentacaoRequest request = request(null, null);

        when(repository.findByIdAndUsuarioEmail(10L, "maria@email.com")).thenReturn(Optional.of(existente));
        when(repository.save(existente)).thenReturn(existente);

        Movimentacao atualizada = service.atualizar(10L, request, "maria@email.com");

        assertEquals(existente, atualizada);
        assertEquals(usuario, atualizada.getUsuario());
        assertEquals("Salario", atualizada.getDescricao());
        assertEquals(new BigDecimal("100.00"), atualizada.getValor());
        assertEquals(LocalDate.of(2026, 5, 27), atualizada.getData());
        assertEquals(TipoMovimentacao.RECEITA, atualizada.getTipo());
        verify(repository).save(existente);
    }

    @Test
    void deveFalharAoAtualizarMovimentacaoInexistenteOuDeOutroUsuario() {
        when(repository.findByIdAndUsuarioEmail(10L, "maria@email.com")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.atualizar(10L, request(null, null), "maria@email.com")
        );

        assertEquals(404, exception.getStatusCode().value());
        assertEquals("Movimentacao nao encontrada", exception.getReason());
    }

    @Test
    void deveResolverCategoriaPorIdAoSalvar() {
        Usuario usuario = usuario(1L, "maria@email.com");
        Categoria categoria = categoria(5L, "Moradia", usuario);

        when(usuarioRepository.findByEmail("maria@email.com")).thenReturn(Optional.of(usuario));
        when(categoriaRepository.findByIdAndUsuarioEmail(5L, "maria@email.com")).thenReturn(Optional.of(categoria));
        when(repository.save(org.mockito.ArgumentMatchers.any(Movimentacao.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Movimentacao salva = service.salvar(request(5L, "Ignorado"), "maria@email.com");

        assertEquals(categoria, salva.getCategoria());
    }

    @Test
    void deveReutilizarCategoriaPorNomeAoAtualizar() {
        Usuario usuario = usuario(1L, "maria@email.com");
        Categoria categoria = categoria(5L, "Mercado", usuario);
        Movimentacao existente = new Movimentacao();
        existente.setUsuario(usuario);

        when(repository.findByIdAndUsuarioEmail(10L, "maria@email.com")).thenReturn(Optional.of(existente));
        when(categoriaRepository.findByNomeIgnoreCaseAndUsuarioEmail("Mercado", "maria@email.com"))
                .thenReturn(Optional.of(categoria));
        when(repository.save(existente)).thenReturn(existente);

        Movimentacao atualizada = service.atualizar(10L, request(null, " Mercado "), "maria@email.com");

        assertEquals(categoria, atualizada.getCategoria());
    }

    @Test
    void deveResolverCategoriaPorPayloadAninhadoAoSalvar() {
        Usuario usuario = usuario(1L, "maria@email.com");
        Categoria categoria = categoria(5L, "Mercado", usuario);
        MovimentacaoRequest request = new MovimentacaoRequest(
                "Salario",
                new BigDecimal("100.00"),
                LocalDate.of(2026, 5, 27),
                TipoMovimentacao.RECEITA,
                null,
                null,
                new MovimentacaoRequest.CategoriaPayload(" Mercado ")
        );

        when(usuarioRepository.findByEmail("maria@email.com")).thenReturn(Optional.of(usuario));
        when(categoriaRepository.findByNomeIgnoreCaseAndUsuarioEmail("Mercado", "maria@email.com"))
                .thenReturn(Optional.of(categoria));
        when(repository.save(org.mockito.ArgumentMatchers.any(Movimentacao.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Movimentacao salva = service.salvar(request, "maria@email.com");

        assertEquals(categoria, salva.getCategoria());
    }

    @Test
    void deveCriarCategoriaPorNomeAoAtualizar() {
        Usuario usuario = usuario(1L, "maria@email.com");
        Categoria nova = categoria(5L, "Mercado", usuario);
        Movimentacao existente = new Movimentacao();
        existente.setUsuario(usuario);

        when(repository.findByIdAndUsuarioEmail(10L, "maria@email.com")).thenReturn(Optional.of(existente));
        when(categoriaRepository.findByNomeIgnoreCaseAndUsuarioEmail("Mercado", "maria@email.com"))
                .thenReturn(Optional.empty());
        when(categoriaRepository.save(org.mockito.ArgumentMatchers.any(Categoria.class))).thenReturn(nova);
        when(repository.save(existente)).thenReturn(existente);

        Movimentacao atualizada = service.atualizar(10L, request(null, " Mercado "), "maria@email.com");

        ArgumentCaptor<Categoria> captor = ArgumentCaptor.forClass(Categoria.class);
        verify(categoriaRepository).save(captor.capture());
        assertEquals("Mercado", captor.getValue().getNome());
        assertEquals(usuario, captor.getValue().getUsuario());
        assertEquals(nova, atualizada.getCategoria());
    }

    @Test
    void deveRemoverCategoriaQuandoRequestInformarNomeVazio() {
        Usuario usuario = usuario(1L, "maria@email.com");
        Movimentacao existente = new Movimentacao();
        existente.setUsuario(usuario);
        existente.setCategoria(categoria(5L, "Mercado", usuario));

        when(repository.findByIdAndUsuarioEmail(10L, "maria@email.com")).thenReturn(Optional.of(existente));
        when(repository.save(existente)).thenReturn(existente);

        Movimentacao atualizada = service.atualizar(10L, request(null, ""), "maria@email.com");

        assertNull(atualizada.getCategoria());
    }

    @Test
    void devePreservarCategoriaAoAtualizarSemInformarCategoria() {
        Usuario usuario = usuario(1L, "maria@email.com");
        Categoria categoria = categoria(5L, "Mercado", usuario);
        Movimentacao existente = new Movimentacao();
        existente.setUsuario(usuario);
        existente.setCategoria(categoria);

        when(repository.findByIdAndUsuarioEmail(10L, "maria@email.com")).thenReturn(Optional.of(existente));
        when(repository.save(existente)).thenReturn(existente);

        Movimentacao atualizada = service.atualizar(10L, request(null, null), "maria@email.com");

        assertEquals(categoria, atualizada.getCategoria());
    }

    private MovimentacaoRequest request(Long categoriaId, String categoriaNome) {
        return new MovimentacaoRequest(
                "Salario",
                new BigDecimal("100.00"),
                LocalDate.of(2026, 5, 27),
                TipoMovimentacao.RECEITA,
                categoriaId,
                categoriaNome
        );
    }

    private Usuario usuario(Long id, String email) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setEmail(email);
        return usuario;
    }

    private Categoria categoria(Long id, String nome, Usuario usuario) {
        Categoria categoria = new Categoria();
        categoria.setId(id);
        categoria.setNome(nome);
        categoria.setUsuario(usuario);
        return categoria;
    }
}
