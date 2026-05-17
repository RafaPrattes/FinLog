package com.ucb.finlog.service;

import com.ucb.finlog.model.Usuario;
import com.ucb.finlog.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Cadastra um novo usuário.
     * Lança IllegalArgumentException se o e-mail já estiver em uso.
     */
    public Usuario cadastrar(Usuario usuario) {
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new IllegalArgumentException("E-mail já cadastrado.");
        }
        // TODO: aplicar hash na senha antes de salvar (ex: BCrypt)
        // usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        return usuarioRepository.save(usuario);
    }

    /**
     * Autentica um usuário por e-mail e senha.
     * Retorna o usuário se as credenciais forem válidas.
     * Lança IllegalArgumentException se inválidas.
     */
    public Usuario login(String email, String senha) {
        Optional<Usuario> opt = usuarioRepository.findByEmail(email);

        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Usuário ou senha inválidos.");
        }

        Usuario usuario = opt.get();

        // TODO: comparar com hash (ex: passwordEncoder.matches(senha, usuario.getSenha()))
        if (!usuario.getSenha().equals(senha)) {
            throw new IllegalArgumentException("Usuário ou senha inválidos.");
        }

        // Remove a senha do objeto antes de retornar
        usuario.setSenha(null);
        return usuario;
    }
}