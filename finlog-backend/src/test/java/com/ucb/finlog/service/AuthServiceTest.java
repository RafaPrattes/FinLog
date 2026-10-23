package com.ucb.finlog.service;

import com.ucb.finlog.dto.AuthResponse;
import com.ucb.finlog.dto.LoginRequest;
import com.ucb.finlog.model.Usuario;
import com.ucb.finlog.repository.UsuarioRepository;
import com.ucb.finlog.security.AuthTokenService;
import com.ucb.finlog.security.LoginAttemptService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthTokenService authTokenService;

    @Mock
    private LoginAttemptService loginAttemptService;

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

        AuthResponse response = service.login(new LoginRequest(" Maria@Email.com ", "123456"));

        assertEquals("token-assinado", response.token());
        assertEquals("Bearer", response.tipo());
        assertEquals(3600L, response.expiraEmSegundos());
        assertEquals("maria@email.com", response.usuario().email());
        verify(loginAttemptService).verificarBloqueio("maria@email.com");
        verify(loginAttemptService).registrarSucesso("maria@email.com");
        verify(loginAttemptService, never()).registrarFalha(anyString());
    }

    @Test
    void deveRejeitarLoginSemDadosObrigatorios() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.login(null));

        assertEquals(400, exception.getStatusCode().value());
        assertEquals("E-mail e senha são obrigatórios", exception.getReason());
    }

    @Test
    void deveRejeitarEmailInexistenteComparandoComHashFicticio() {
        when(usuarioRepository.findByEmail("maria@email.com")).thenReturn(Optional.empty());
        when(passwordEncoder.matches("123456", AuthService.HASH_FICTICIO)).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.login(new LoginRequest("maria@email.com", "123456"))
        );

        assertEquals(401, exception.getStatusCode().value());
        assertEquals("Credenciais inválidas", exception.getReason());
        verify(passwordEncoder).matches("123456", AuthService.HASH_FICTICIO);
        verify(loginAttemptService).registrarFalha("maria@email.com");
    }

    @Test
    void naoDeveAutenticarEmailInexistenteMesmoQueSenhaConfiraComHashFicticio() {
        when(usuarioRepository.findByEmail("maria@email.com")).thenReturn(Optional.empty());
        when(passwordEncoder.matches("qualquer", AuthService.HASH_FICTICIO)).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.login(new LoginRequest("maria@email.com", "qualquer"))
        );

        assertEquals(401, exception.getStatusCode().value());
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
        verify(loginAttemptService).registrarFalha("maria@email.com");
        verify(loginAttemptService, never()).registrarSucesso(anyString());
    }

    @Test
    void deveRecusarLoginBloqueadoSemConsultarUsuario() {
        doThrow(new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Muitas tentativas"))
                .when(loginAttemptService).verificarBloqueio("maria@email.com");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.login(new LoginRequest("maria@email.com", "123456"))
        );

        assertEquals(429, exception.getStatusCode().value());
        verify(usuarioRepository, never()).findByEmail(anyString());
    }

    @Test
    void logoutDeveIncrementarVersaoDoTokenDoUsuario() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setTokenVersion(3);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        service.logout(1L);

        assertEquals(4, usuario.getTokenVersion());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void logoutDeUsuarioInexistenteNaoDeveFalhar() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        service.logout(1L);

        verify(usuarioRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void hashFicticioDeveSerBcryptValidoComMesmoCustoDoPasswordEncoder() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        assertTrue(AuthService.HASH_FICTICIO.startsWith("$2a$10$"));
        assertFalse(encoder.matches("123456", AuthService.HASH_FICTICIO));
        assertFalse(encoder.upgradeEncoding(AuthService.HASH_FICTICIO));
    }
}
