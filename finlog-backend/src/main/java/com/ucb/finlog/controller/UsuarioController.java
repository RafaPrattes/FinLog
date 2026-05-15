package com.ucb.finlog.controller;

import com.ucb.finlog.dto.CadastroUsuarioRequest;
import com.ucb.finlog.dto.UsuarioResponse;
import com.ucb.finlog.security.UsuarioAutenticado;
import com.ucb.finlog.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse cadastrar(@RequestBody CadastroUsuarioRequest request) {
        return UsuarioResponse.from(service.cadastrar(request));
    }

    @GetMapping("/me")
    public UsuarioResponse perfil(@AuthenticationPrincipal UsuarioAutenticado usuarioAutenticado) {
        return UsuarioResponse.from(service.buscarPorEmail(usuarioAutenticado.email()));
    }
}
