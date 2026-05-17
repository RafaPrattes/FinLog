package com.ucb.finlog.controller;

import com.ucb.finlog.security.UsuarioAutenticado;
import com.ucb.finlog.service.AIService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AIController {
    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    @GetMapping("/conselho")
    public String pegarConselho(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return aiService.obterConselhoIA(usuario.email());
    }
}
