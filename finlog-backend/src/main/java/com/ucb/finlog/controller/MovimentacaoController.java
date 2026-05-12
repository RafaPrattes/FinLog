package com.ucb.finlog.controller;

import com.ucb.finlog.model.Movimentacao;
import com.ucb.finlog.repository.MovimentacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movimentacoes")
public class MovimentacaoController {

    @Autowired
    private MovimentacaoRepository repository;

    @GetMapping
    public List<Movimentacao> listar() {
        return repository.findAll();
    }

    @PostMapping
    public Movimentacao salvar(@RequestBody Movimentacao movimentacao) {
        return repository.save(movimentacao);
    }
}