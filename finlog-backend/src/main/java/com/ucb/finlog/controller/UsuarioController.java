package com.ucb.finlog.controller;

import com.ucb.finlog.dto.UsuarioResponse;
import com.ucb.finlog.security.UsuarioAutenticado;
import com.ucb.finlog.service.UsuarioService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping
    public List<UsuarioResponse> listar() {
        return service.listarTodos().stream()
                .map(UsuarioResponse::from)
                .toList();
    }

    @GetMapping("/me")
    public UsuarioResponse perfil(@AuthenticationPrincipal UsuarioAutenticado usuarioAutenticado) {
        return UsuarioResponse.from(service.buscarPorEmail(usuarioAutenticado.email()));
    }
}
