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

    // A rota agora exige o ID do dono da conta
    @GetMapping("/usuario/{usuarioId}")
    public List<Movimentacao> listarDoUsuario(@PathVariable Long usuarioId) {
        return service.listarPorUsuario(usuarioId);
    }
    @PostMapping
    public Movimentacao salvar(@RequestBody Movimentacao movimentacao) {
        return service.salvar(movimentacao);
    }
}