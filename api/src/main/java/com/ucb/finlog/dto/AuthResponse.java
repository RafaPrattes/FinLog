package com.ucb.finlog.dto;

public record AuthResponse(
        UsuarioResponse usuario,
        String token,
        String tipo,
        long expiraEmSegundos
) {
}
