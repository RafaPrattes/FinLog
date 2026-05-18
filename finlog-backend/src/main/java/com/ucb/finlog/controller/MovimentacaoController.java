package com.ucb.finlog.controller;

import com.ucb.finlog.model.Movimentacao;
import com.ucb.finlog.security.UsuarioAutenticado;
import com.ucb.finlog.service.MovimentacaoService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/movimentacoes")
public class MovimentacaoController {
    private final MovimentacaoService service;

    public MovimentacaoController(MovimentacaoService service) {
        this.service = service;
    }

    @GetMapping
    public List<Movimentacao> listar(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return service.listarTodas(usuario.email());
    }

    @GetMapping("/{id}")
    public Movimentacao buscar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return service.buscarPorId(id, usuario.email());
    }

    @PostMapping
    public Movimentacao salvar(@RequestBody Movimentacao movimentacao, @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return service.salvar(movimentacao, usuario.email());
    }
}
