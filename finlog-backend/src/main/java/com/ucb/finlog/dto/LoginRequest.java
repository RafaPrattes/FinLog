package com.ucb.finlog.dto;

public record LoginRequest(
        String email,
        String senha
) {
}
