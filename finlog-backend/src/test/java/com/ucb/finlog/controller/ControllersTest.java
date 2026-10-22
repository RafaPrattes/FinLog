package com.ucb.finlog.controller;

import com.ucb.finlog.dto.AuthResponse;
import com.ucb.finlog.dto.CadastroUsuarioRequest;
import com.ucb.finlog.dto.ChatRequest;
import com.ucb.finlog.dto.ChatResponse;
import com.ucb.finlog.dto.LoginRequest;
import com.ucb.finlog.dto.MetaRequest;
import com.ucb.finlog.dto.MetaResponse;
import com.ucb.finlog.dto.MovimentacaoRequest;
import com.ucb.finlog.dto.MovimentacaoResponse;
import com.ucb.finlog.dto.UsuarioResponse;
import com.ucb.finlog.model.Meta;
import com.ucb.finlog.model.Movimentacao;
import com.ucb.finlog.model.TipoMovimentacao;
import com.ucb.finlog.model.Usuario;
import com.ucb.finlog.security.UsuarioAutenticado;
import com.ucb.finlog.service.AIService;
import com.ucb.finlog.service.AuthService;
import com.ucb.finlog.service.MetaService;
import com.ucb.finlog.service.MovimentacaoService;
import com.ucb.finlog.service.UsuarioService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ControllersTest {
    @Test
    void homeDeveRetornarStatusERotas() {
        Map<String, Object> response = new HomeController().home();

        assertEquals("FinLog backend em execucao", response.get("status"));
        assertTrue(((List<?>) response.get("endpointsPublicos")).contains("POST /api/cadastro"));
    }

    @Test
    void authControllerDeveDelegarLoginELogout() {
        AuthService service = mock(AuthService.class);
        LoginRequest request = new LoginRequest("maria@email.com", "123456");
        AuthResponse authResponse = new AuthResponse(new UsuarioResponse(1L, "Maria", "maria@email.com"), "token", "Bearer", 3600);

        when(service.login(request)).thenReturn(authResponse);

        AuthController controller = new AuthController(service);

        assertEquals(authResponse, controller.login(request));
        controller.logout();
        verify(service).login(request);
    }

    @Test
    void cadastroControllerDeveCadastrarUsuario() {
        UsuarioService service = mock(UsuarioService.class);
        CadastroUsuarioRequest request = new CadastroUsuarioRequest("Maria", "maria@email.com", "123456");
        Usuario usuario = usuario(1L, "Maria", "maria@email.com");

        when(service.cadastrar(request)).thenReturn(usuario);

        UsuarioResponse response = new CadastroController(service).cadastrar(request);

        assertEquals("maria@email.com", response.email());
    }

    @Test
    void usuarioControllerDeveExibirPerfil() {
        UsuarioService service = mock(UsuarioService.class);
        Usuario usuario = usuario(1L, "Maria", "maria@email.com");

        when(service.buscarPorEmail("maria@email.com")).thenReturn(usuario);

        UsuarioController controller = new UsuarioController(service);

        assertEquals("maria@email.com", controller.perfil(new UsuarioAutenticado(1L, "maria@email.com")).email());
    }

    @Test
    void movimentacaoControllerDeveDelegarOperacoes() {
        MovimentacaoService service = mock(MovimentacaoService.class);
        UsuarioAutenticado usuario = new UsuarioAutenticado(1L, "maria@email.com");
        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setId(10L);
        movimentacao.setDescricao("Salario");
        movimentacao.setValor(new BigDecimal("100.00"));
        movimentacao.setData(LocalDate.of(2026, 5, 27));
        movimentacao.setTipo(TipoMovimentacao.RECEITA);
        MovimentacaoRequest request = new MovimentacaoRequest(
                "Salario",
                new BigDecimal("100.00"),
                LocalDate.of(2026, 5, 27),
                TipoMovimentacao.RECEITA,
                null,
                null
        );

        when(service.listarTodas("maria@email.com")).thenReturn(List.of(movimentacao));
        when(service.buscarPorId(10L, "maria@email.com")).thenReturn(movimentacao);
        when(service.salvar(request, "maria@email.com")).thenReturn(movimentacao);
        when(service.atualizar(10L, request, "maria@email.com")).thenReturn(movimentacao);

        MovimentacaoController controller = new MovimentacaoController(service);

        assertEquals(1, controller.listar(usuario).size());
        assertMovimentacaoResponse(controller.buscar(10L, usuario));
        assertMovimentacaoResponse(controller.salvar(request, usuario));
        assertMovimentacaoResponse(controller.atualizar(10L, request, usuario));
        controller.deletar(10L, usuario);
        verify(service).deletar(10L, "maria@email.com");
    }

    @Test
    void metaControllerDeveDelegarOperacoes() {
        MetaService service = mock(MetaService.class);
        UsuarioAutenticado usuario = new UsuarioAutenticado(1L, "maria@email.com");
        Meta meta = new Meta();
        meta.setId(5L);
        meta.setDescricao("Viagem");
        meta.setValor(new BigDecimal("1500.00"));
        MetaRequest request = new MetaRequest("Viagem", new BigDecimal("1500.00"));

        when(service.listar("maria@email.com")).thenReturn(List.of(meta));
        when(service.criar(request, "maria@email.com")).thenReturn(meta);
        when(service.atualizar(5L, request, "maria@email.com")).thenReturn(meta);

        MetaController controller = new MetaController(service);

        assertEquals(List.of(new MetaResponse(5L, "Viagem", new BigDecimal("1500.00"))), controller.listar(usuario));
        assertEquals("Viagem", controller.criar(request, usuario).descricao());
        assertEquals(5L, controller.atualizar(5L, request, usuario).id());
        controller.deletar(5L, usuario);
        verify(service).deletar(5L, "maria@email.com");
    }

    @Test
    void aiControllerDeveRetornarConselho() {
        AIService service = mock(AIService.class);
        when(service.obterConselhoIA("maria@email.com")).thenReturn("Poupe mais.");

        assertEquals("Poupe mais.", new AIController(service).pegarConselho(new UsuarioAutenticado(1L, "maria@email.com")));
    }

    @Test
    void aiControllerDeveResponderPerguntaDoChat() {
        AIService service = mock(AIService.class);
        when(service.responderPergunta("maria@email.com", "Quanto gastei?")).thenReturn("Voce gastou R$100,00.");

        ChatResponse response = new AIController(service)
                .conversar(new ChatRequest("Quanto gastei?"), new UsuarioAutenticado(1L, "maria@email.com"));

        assertEquals("Voce gastou R$100,00.", response.resposta());
    }

    private Usuario usuario(Long id, String nome, String email) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNome(nome);
        usuario.setEmail(email);
        return usuario;
    }

    private void assertMovimentacaoResponse(MovimentacaoResponse response) {
        assertEquals(10L, response.id());
        assertEquals("Salario", response.descricao());
        assertEquals(new BigDecimal("100.00"), response.valor());
        assertEquals(LocalDate.of(2026, 5, 27), response.data());
        assertEquals(TipoMovimentacao.RECEITA, response.tipo());
    }
}
