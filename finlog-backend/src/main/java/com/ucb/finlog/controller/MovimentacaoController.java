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

    /**
     * GET /api/movimentacoes?usuarioId=1
     * Lista movimentações. Se usuarioId for informado, filtra por usuário.
     */
    @GetMapping
    public List<Movimentacao> listar(@RequestParam(required = false) Long usuarioId) {
        if (usuarioId != null) {
            return service.listarPorUsuario(usuarioId);
        }
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

    @PutMapping("/{id}")
    public Movimentacao atualizar(@PathVariable Long id, @RequestBody Movimentacao movimentacao) {
        movimentacao.setId(id);
        return service.salvar(movimentacao);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        service.deletar(id);
    }
}