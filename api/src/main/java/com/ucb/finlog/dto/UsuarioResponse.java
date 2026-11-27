package com.ucb.finlog.dto;

import com.ucb.finlog.model.Usuario;

public record UsuarioResponse(
        Long id,
        String nome,
        String email
) {
    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail());
    }
}
