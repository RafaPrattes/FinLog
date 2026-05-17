package com.ucb.finlog.controller;

import com.ucb.finlog.dto.CadastroUsuarioRequest;
import com.ucb.finlog.dto.UsuarioResponse;
import com.ucb.finlog.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cadastro")
public class CadastroController {
    private final UsuarioService service;

    public CadastroController(UsuarioService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse cadastrar(@RequestBody CadastroUsuarioRequest request) {
        return UsuarioResponse.from(service.cadastrar(request));
    }
}
