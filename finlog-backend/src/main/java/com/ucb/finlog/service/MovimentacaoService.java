package com.ucb.finlog.service;

import com.ucb.finlog.model.Movimentacao;
import com.ucb.finlog.repository.MovimentacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovimentacaoService {

    @Autowired
    private MovimentacaoRepository repository;

    // Novo método que utiliza a busca isolada
    public List<Movimentacao> listarPorUsuario(Long usuarioId) {
        return repository.findByUsuarioId(usuarioId);
    }

    // Método de salvar com validação de segurança
    public Movimentacao salvar(Movimentacao movimentacao) {
        if (movimentacao.getUsuario() == null || movimentacao.getUsuario().getId() == null) {
            throw new IllegalArgumentException("Bloqueado: Toda movimentação precisa de um usuário vinculado.");
        }
        return repository.save(movimentacao);
    }

    public Movimentacao buscarPorId(Long id) {
        return repository.findById(id).orElse(null);
    }
}