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

    public List<Movimentacao> listarTodas() {
        return repository.findAll();
    }

    public Movimentacao salvar(Movimentacao movimentacao) {
        // Futuramente: Adicionar validações de negócio aqui (ex: saldo insuficiente)
        return repository.save(movimentacao);
    }

    public Movimentacao buscarPorId(Long id) {
        return repository.findById(id).orElse(null);
    }
}