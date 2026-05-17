package com.ucb.finlog.service;

import com.ucb.finlog.model.Movimentacao;
import com.ucb.finlog.model.Usuario;
import com.ucb.finlog.repository.MovimentacaoRepository;
import com.ucb.finlog.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovimentacaoService {

    @Autowired
    private MovimentacaoRepository repository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<Movimentacao> listarTodas() {
        return repository.findAll();
    }

    public List<Movimentacao> listarPorUsuario(Long usuarioId) {
        return repository.findByUsuarioId(usuarioId);
    }

    public Movimentacao salvar(Movimentacao movimentacao) {
        // Garante que o objeto Usuario está corretamente associado pelo id
        if (movimentacao.getUsuario() != null && movimentacao.getUsuario().getId() != null) {
            Long usuarioId = movimentacao.getUsuario().getId();
            Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
            movimentacao.setUsuario(usuario);
        }
        return repository.save(movimentacao);
    }

    public Movimentacao buscarPorId(Long id) {
        return repository.findById(id).orElse(null);
    }

    public void deletar(Long id) {
        repository.deleteById(id);
    }
}