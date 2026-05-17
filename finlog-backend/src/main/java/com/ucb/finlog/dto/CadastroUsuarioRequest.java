package com.ucb.finlog.dto;

public record CadastroUsuarioRequest(
        String nome,
        String email,
        String senha
) {
}
