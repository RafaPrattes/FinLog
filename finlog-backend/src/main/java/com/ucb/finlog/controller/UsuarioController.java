package com.ucb.finlog.controller;

import com.ucb.finlog.model.Usuario;
import com.ucb.finlog.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    /**
     * POST /api/usuarios
     * Cadastra um novo usuário.
     * Body: { "nome": "João", "email": "joao@email.com", "senha": "1234" }
     */
    @PostMapping("/usuarios")
    public ResponseEntity<?> cadastrar(@RequestBody Usuario usuario) {
        try {
            Usuario salvo = usuarioService.cadastrar(usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * POST /api/auth/login
     * Autentica um usuário.
     * Body: { "email": "joao@email.com", "senha": "1234" }
     * Retorna o objeto do usuário (sem senha) em caso de sucesso.
     */
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        try {
            String email = body.get("email");
            String senha = body.get("senha");
            Usuario usuario = usuarioService.login(email, senha);
            return ResponseEntity.ok(usuario);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
}