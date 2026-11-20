package com.ucb.finlog.security;

public record TokenValidado(
        Long usuarioId,
        int versao
) {
}
