package com.ucb.finlog.controller;

import com.ucb.finlog.dto.MetaRequest;
import com.ucb.finlog.dto.MetaResponse;
import com.ucb.finlog.security.UsuarioAutenticado;
import com.ucb.finlog.service.MetaService;
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
@RequestMapping("/api/metas")
public class MetaController {
    private final MetaService service;

    public MetaController(MetaService service) {
        this.service = service;
    }

    @GetMapping
    public List<MetaResponse> listar(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return service.listar(usuario.email()).stream()
                .map(MetaResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MetaResponse criar(
            @Valid @RequestBody MetaRequest request,
            @AuthenticationPrincipal UsuarioAutenticado usuario
    ) {
        return MetaResponse.from(service.criar(request, usuario.email()));
    }

    @PutMapping("/{id}")
    public MetaResponse atualizar(
            @PathVariable Long id,
            @Valid @RequestBody MetaRequest request,
            @AuthenticationPrincipal UsuarioAutenticado usuario
    ) {
        return MetaResponse.from(service.atualizar(id, request, usuario.email()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado usuario) {
        service.deletar(id, usuario.email());
    }
}
