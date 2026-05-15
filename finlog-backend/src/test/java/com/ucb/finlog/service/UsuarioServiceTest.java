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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
}
