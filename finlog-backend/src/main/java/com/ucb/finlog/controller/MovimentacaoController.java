package com.ucb.finlog.controller;

import com.ucb.finlog.model.Movimentacao;
import com.ucb.finlog.service.MovimentacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movimentacoes")
public class MovimentacaoController {

    @Autowired
    private MovimentacaoService service;

    @GetMapping
    public List<Movimentacao> listar() {
        return service.listarTodas();
    }

    @GetMapping("/{id}")
    public Movimentacao buscar(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public Movimentacao salvar(@RequestBody Movimentacao movimentacao) {
        return service.salvar(movimentacao);
    }
}