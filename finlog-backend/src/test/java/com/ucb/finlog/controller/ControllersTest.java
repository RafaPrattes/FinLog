package com.ucb.finlog.controller;

import com.ucb.finlog.dto.AuthResponse;
import com.ucb.finlog.dto.CadastroUsuarioRequest;
import com.ucb.finlog.dto.LoginRequest;
import com.ucb.finlog.dto.UsuarioResponse;
import com.ucb.finlog.model.Movimentacao;
import com.ucb.finlog.model.Usuario;
import com.ucb.finlog.security.UsuarioAutenticado;
import com.ucb.finlog.service.AIService;
import com.ucb.finlog.service.AuthService;
import com.ucb.finlog.service.MovimentacaoService;
import com.ucb.finlog.service.UsuarioService;
import org.junit.jupiter.api.Test;

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
    void usuarioControllerDeveListarEExibirPerfil() {
        UsuarioService service = mock(UsuarioService.class);
        Usuario usuario = usuario(1L, "Maria", "maria@email.com");

        when(service.listarTodos()).thenReturn(List.of(usuario));
        when(service.buscarPorEmail("maria@email.com")).thenReturn(usuario);

        UsuarioController controller = new UsuarioController(service);

        assertEquals(1, controller.listar().size());
        assertEquals("maria@email.com", controller.perfil(new UsuarioAutenticado(1L, "maria@email.com")).email());
    }

    @Test
    void movimentacaoControllerDeveDelegarOperacoes() {
        MovimentacaoService service = mock(MovimentacaoService.class);
        UsuarioAutenticado usuario = new UsuarioAutenticado(1L, "maria@email.com");
        Movimentacao movimentacao = new Movimentacao();

        when(service.listarTodas("maria@email.com")).thenReturn(List.of(movimentacao));
        when(service.buscarPorId(10L, "maria@email.com")).thenReturn(movimentacao);
        when(service.salvar(movimentacao, "maria@email.com")).thenReturn(movimentacao);

        MovimentacaoController controller = new MovimentacaoController(service);

        assertEquals(1, controller.listar(usuario).size());
        assertEquals(movimentacao, controller.buscar(10L, usuario));
        assertEquals(movimentacao, controller.salvar(movimentacao, usuario));
    }

    @Test
    void aiControllerDeveRetornarConselho() {
        AIService service = mock(AIService.class);
        when(service.obterConselhoIA("maria@email.com")).thenReturn("Poupe mais.");

        assertEquals("Poupe mais.", new AIController(service).pegarConselho(new UsuarioAutenticado(1L, "maria@email.com")));
    }

    private Usuario usuario(Long id, String nome, String email) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNome(nome);
        usuario.setEmail(email);
        return usuario;
    }
}
