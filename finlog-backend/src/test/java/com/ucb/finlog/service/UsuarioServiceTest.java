package com.ucb.finlog.service;

import com.ucb.finlog.dto.CadastroUsuarioRequest;
import com.ucb.finlog.model.Usuario;
import com.ucb.finlog.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {
    @Mock
    private UsuarioRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService service;

    @Test
    void deveCadastrarUsuarioComSenhaCriptografada() {
        CadastroUsuarioRequest request = new CadastroUsuarioRequest("Maria", "MARIA@email.com", "123456");

        when(repository.existsByEmail("maria@email.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("senha-com-hash");
        when(repository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.cadastrar(request);

        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(repository).save(usuarioCaptor.capture());

        Usuario usuarioSalvo = usuarioCaptor.getValue();
        assertEquals("Maria", usuarioSalvo.getNome());
        assertEquals("maria@email.com", usuarioSalvo.getEmail());
        assertEquals("senha-com-hash", usuarioSalvo.getSenha());
        assertNotEquals("123456", usuarioSalvo.getSenha());
    }

    @Test
    void deveRejeitarEmailJaCadastrado() {
        when(repository.existsByEmail("maria@email.com")).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.cadastrar(new CadastroUsuarioRequest("Maria", "maria@email.com", "123456"))
        );

        assertEquals(409, exception.getStatusCode().value());
        assertEquals("E-mail já cadastrado", exception.getReason());
    }

    @Test
    void deveRejeitarCadastroInvalido() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.cadastrar(new CadastroUsuarioRequest("", "email-invalido", "123"))
        );

        assertEquals(400, exception.getStatusCode().value());
        assertEquals("Nome, e-mail e senha são obrigatórios", exception.getReason());
    }

    @Test
    void deveRejeitarCadastroNulo() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.cadastrar(null));

        assertEquals(400, exception.getStatusCode().value());
        assertEquals("Nome, e-mail e senha são obrigatórios", exception.getReason());
    }

    @Test
    void deveRejeitarEmailInvalido() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.cadastrar(new CadastroUsuarioRequest("Maria", "email-invalido", "123456"))
        );

        assertEquals(400, exception.getStatusCode().value());
        assertEquals("E-mail inválido", exception.getReason());
    }

    @Test
    void deveRejeitarSenhaCurta() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.cadastrar(new CadastroUsuarioRequest("Maria", "maria@email.com", "12345"))
        );

        assertEquals(400, exception.getStatusCode().value());
        assertEquals("A senha deve ter pelo menos 6 caracteres", exception.getReason());
    }

    @Test
    void deveBuscarUsuarioPorEmail() {
        Usuario usuario = new Usuario();
        usuario.setEmail("maria@email.com");

        when(repository.findByEmail("maria@email.com")).thenReturn(Optional.of(usuario));

        assertEquals(usuario, service.buscarPorEmail("maria@email.com"));
    }

    @Test
    void deveFalharQuandoUsuarioNaoExiste() {
        when(repository.findByEmail("maria@email.com")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.buscarPorEmail("maria@email.com")
        );

        assertEquals(404, exception.getStatusCode().value());
        assertEquals("Usuário não encontrado", exception.getReason());
    }

    @Test
    void deveListarUsuarios() {
        Usuario usuario = new Usuario();
        usuario.setEmail("maria@email.com");

        when(repository.findAll()).thenReturn(List.of(usuario));

        assertEquals(1, service.listarTodos().size());
    }
}
