package com.ucb.finlog.service;

import com.ucb.finlog.dto.MetaRequest;
import com.ucb.finlog.model.Meta;
import com.ucb.finlog.model.Usuario;
import com.ucb.finlog.repository.MetaRepository;
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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetaServiceTest {
    @Mock
    private MetaRepository repository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private MetaService service;

    @Test
    void deveListarMetasDoUsuario() {
        Meta meta = new Meta();
        when(repository.findByUsuarioEmailOrderByIdAsc("maria@email.com")).thenReturn(List.of(meta));

        assertEquals(List.of(meta), service.listar("maria@email.com"));
    }

    @Test
    void deveCriarMetaAssociadaAoUsuario() {
        Usuario usuario = new Usuario();
        usuario.setEmail("maria@email.com");

        when(usuarioRepository.findByEmail("maria@email.com")).thenReturn(Optional.of(usuario));
        when(repository.save(any(Meta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Meta meta = service.criar(new MetaRequest("  Viagem  ", new BigDecimal("1500.00")), "maria@email.com");

        assertSame(usuario, meta.getUsuario());
        assertEquals("Viagem", meta.getDescricao());
        assertEquals(new BigDecimal("1500.00"), meta.getValor());
    }

    @Test
    void deveFalharAoCriarMetaParaUsuarioInexistente() {
        when(usuarioRepository.findByEmail("maria@email.com")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.criar(new MetaRequest("Viagem", BigDecimal.TEN), "maria@email.com")
        );

        assertEquals(404, exception.getStatusCode().value());
        verify(repository, never()).save(any());
    }

    @Test
    void deveAtualizarMetaDoUsuario() {
        Meta meta = new Meta();
        meta.setId(3L);
        meta.setDescricao("Antiga");
        meta.setValor(BigDecimal.ONE);

        when(repository.findByIdAndUsuarioEmail(3L, "maria@email.com")).thenReturn(Optional.of(meta));
        when(repository.save(meta)).thenReturn(meta);

        Meta atualizada = service.atualizar(3L, new MetaRequest("Carro", new BigDecimal("30000.00")), "maria@email.com");

        assertEquals("Carro", atualizada.getDescricao());
        assertEquals(new BigDecimal("30000.00"), atualizada.getValor());
    }

    @Test
    void deveRetornar404ParaMetaDeOutroUsuario() {
        when(repository.findByIdAndUsuarioEmail(3L, "maria@email.com")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.deletar(3L, "maria@email.com")
        );

        assertEquals(404, exception.getStatusCode().value());
        assertEquals("Meta nao encontrada", exception.getReason());
    }

    @Test
    void deveDeletarMetaDoUsuario() {
        Meta meta = new Meta();
        when(repository.findByIdAndUsuarioEmail(3L, "maria@email.com")).thenReturn(Optional.of(meta));

        service.deletar(3L, "maria@email.com");

        verify(repository).delete(meta);
    }
}
