package com.ucb.finlog.controller;

import com.ucb.finlog.dto.MovimentacaoRequest;
import com.ucb.finlog.dto.MovimentacaoResponse;
import com.ucb.finlog.security.UsuarioAutenticado;
import com.ucb.finlog.service.MovimentacaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
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
    public List<MovimentacaoResponse> listar(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return service.listarTodas(usuario.email()).stream()
                .map(MovimentacaoResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public MovimentacaoResponse buscar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return MovimentacaoResponse.from(service.buscarPorId(id, usuario.email()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MovimentacaoResponse salvar(
            @Valid @RequestBody MovimentacaoRequest request,
            @AuthenticationPrincipal UsuarioAutenticado usuario
    ) {
        return MovimentacaoResponse.from(service.salvar(request, usuario.email()));
    }

    @PutMapping("/{id}")
    public MovimentacaoResponse atualizar(
            @PathVariable Long id,
            @Valid @RequestBody MovimentacaoRequest request,
            @AuthenticationPrincipal UsuarioAutenticado usuario
    ) {
        return MovimentacaoResponse.from(service.atualizar(id, request, usuario.email()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado usuario) {
        service.deletar(id, usuario.email());
    }
}
