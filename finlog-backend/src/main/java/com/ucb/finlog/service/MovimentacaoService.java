package com.ucb.finlog.service;

import com.ucb.finlog.model.Movimentacao;
import com.ucb.finlog.model.Usuario;
import com.ucb.finlog.repository.MovimentacaoRepository;
import com.ucb.finlog.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class MovimentacaoService {
    private final MovimentacaoRepository repository;
    private final UsuarioRepository usuarioRepository;

    public MovimentacaoService(MovimentacaoRepository repository, UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<Movimentacao> listarTodas(String emailUsuario) {
        return repository.findByUsuarioEmail(emailUsuario);
    }

    public Movimentacao salvar(Movimentacao movimentacao, String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        movimentacao.setUsuario(usuario);
        return repository.save(movimentacao);
    }

    public Movimentacao buscarPorId(Long id, String emailUsuario) {
        return repository.findByIdAndUsuarioEmail(id, emailUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Movimentação não encontrada"));
    }
}
