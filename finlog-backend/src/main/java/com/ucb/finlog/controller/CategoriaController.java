package com.ucb.finlog.controller;

import com.ucb.finlog.dto.CategoriaDTO;
import com.ucb.finlog.dto.CategoriaResumoDTO;
import com.ucb.finlog.security.UsuarioAutenticado;
import com.ucb.finlog.service.CategoriaService;
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
@RequestMapping("/api/categorias")
public class CategoriaController {
    private final CategoriaService service;

    public CategoriaController(CategoriaService service) {
        this.service = service;
    }

    @GetMapping
    public List<CategoriaDTO> listar(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return service.listarTodas(usuario.email());
    }

    @GetMapping("/{id}")
    public CategoriaDTO buscar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return service.buscarPorId(id, usuario.email());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoriaDTO criar(@RequestBody CategoriaDTO dto, @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return service.criar(dto, usuario.email());
    }

    @PutMapping("/{id}")
    public CategoriaDTO atualizar(
            @PathVariable Long id,
            @RequestBody CategoriaDTO dto,
            @AuthenticationPrincipal UsuarioAutenticado usuario
    ) {
        return service.atualizar(id, dto, usuario.email());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado usuario) {
        service.deletar(id, usuario.email());
    }

    @GetMapping("/resumo")
    public List<CategoriaResumoDTO> resumo(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return service.resumo(usuario.email());
    }

    @GetMapping("/{id}/resumo")
    public CategoriaResumoDTO resumoPorId(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return service.resumoPorId(id, usuario.email());
    }
}
