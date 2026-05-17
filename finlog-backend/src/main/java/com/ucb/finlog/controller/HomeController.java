package com.ucb.finlog.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class HomeController {
    @GetMapping("/")
    public Map<String, Object> home() {
        return Map.of(
                "status", "FinLog backend em execucao",
                "endpointsPublicos", List.of(
                        "POST /api/cadastro",
                        "POST /api/auth/login"
                ),
                "endpointsAutenticados", List.of(
                        "GET /api/usuarios",
                        "GET /api/usuarios/me",
                        "GET /api/movimentacoes",
                        "POST /api/movimentacoes",
                        "GET /api/ai/conselho"
                )
        );
    }
}
