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
                        "POST /api/auth/logout",
                        "GET /api/usuarios/me",
                        "GET /api/movimentacoes",
                        "GET /api/movimentacoes/{id}",
                        "POST /api/movimentacoes",
                        "PUT /api/movimentacoes/{id}",
                        "DELETE /api/movimentacoes/{id}",
                        "GET /api/categorias",
                        "GET /api/categorias/{id}",
                        "POST /api/categorias",
                        "PUT /api/categorias/{id}",
                        "DELETE /api/categorias/{id}",
                        "GET /api/categorias/resumo",
                        "GET /api/categorias/{id}/resumo",
                        "GET /api/metas",
                        "POST /api/metas",
                        "PUT /api/metas/{id}",
                        "DELETE /api/metas/{id}",
                        "GET /api/ai/conselho",
                        "POST /api/ai/chat"
                )
        );
    }
}
