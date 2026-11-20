package com.ucb.finlog.dto;

import java.util.List;

public record GeminiRequest(List<Content> contents) {
    /** role: "user" (pessoa) ou "model" (IA), na ordem da conversa. */
    public record Content(String role, List<Part> parts) {}
    public record Part(String text) {}
}
