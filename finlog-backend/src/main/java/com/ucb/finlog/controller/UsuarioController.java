package com.ucb.finlog.controller;

import com.ucb.finlog.model.Usuario;
import com.ucb.finlog.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService service;

    @PostMapping
    public Usuario criarUsuario(@RequestBody Usuario usuario) {
        return service.salvar(usuario);
    }

    @GetMapping("/{id}")
    public Usuario obterPerfil(@PathVariable Long id) {
        return service.buscarPorId(id);
    }
}