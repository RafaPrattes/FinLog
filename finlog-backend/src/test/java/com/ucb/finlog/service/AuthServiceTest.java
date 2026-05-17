package com.ucb.finlog.service;

import com.ucb.finlog.dto.AuthResponse;
import com.ucb.finlog.dto.LoginRequest;
import com.ucb.finlog.model.Usuario;
import com.ucb.finlog.repository.UsuarioRepository;
import com.ucb.finlog.security.AuthTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import org.springframework.web.server.ResponseStatusException;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthTokenService authTokenService;

    @InjectMocks
    private AuthService service;

    @Test
    void deveRetornarTokenQuandoCredenciaisForemValidas() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Maria");
        usuario.setEmail("maria@email.com");
        usuario.setSenha("senha-com-hash");

        when(usuarioRepository.findByEmail("maria@email.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("123456", "senha-com-hash")).thenReturn(true);
        when(authTokenService.gerarToken(usuario)).thenReturn("token-assinado");
        when(authTokenService.getExpirationSeconds()).thenReturn(3600L);

        AuthResponse response = service.login(new LoginRequest("maria@email.com", "123456"));

        assertEquals("token-assinado", response.token());
        assertEquals("Bearer", response.tipo());
        assertEquals(3600L, response.expiraEmSegundos());
        assertEquals("maria@email.com", response.usuario().email());
    }

    @Test
    void deveRejeitarLoginSemDadosObrigatorios() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.login(null));

        assertEquals(400, exception.getStatusCode().value());
        assertEquals("E-mail e senha são obrigatórios", exception.getReason());
    }

    @Test
    void deveRejeitarCredenciaisInvalidas() {
        when(usuarioRepository.findByEmail("maria@email.com")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.login(new LoginRequest("maria@email.com", "123456"))
        );

        assertEquals(401, exception.getStatusCode().value());
        assertEquals("Credenciais inválidas", exception.getReason());
    }

    @Test
    void deveRejeitarSenhaIncorreta() {
        Usuario usuario = new Usuario();
        usuario.setEmail("maria@email.com");
        usuario.setSenha("senha-com-hash");

        when(usuarioRepository.findByEmail("maria@email.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senha-errada", "senha-com-hash")).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.login(new LoginRequest("maria@email.com", "senha-errada"))
        );

        assertEquals(401, exception.getStatusCode().value());
        assertEquals("Credenciais inválidas", exception.getReason());
    }
}
