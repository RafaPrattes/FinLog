package com.ucb.finlog.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DtoRecordsTest {
    @Test
    void deveExporDadosDosRecordsDoGeminiEDoErro() {
        GeminiRequest request = new GeminiRequest(List.of(
                new GeminiRequest.Content(List.of(new GeminiRequest.Part("prompt")))
        ));
        GeminiResponse response = new GeminiResponse(List.of(
                new GeminiResponse.Candidate(new GeminiResponse.Content(List.of(new GeminiResponse.Part("texto"))))
        ));
        ApiErrorResponse error = new ApiErrorResponse(400, "Requisição inválida", "Mensagem", "/api");

        assertEquals("prompt", request.contents().getFirst().parts().getFirst().text());
        assertEquals("texto", response.candidates().getFirst().content().parts().getFirst().text());
        assertEquals("/api", error.path());
    }
}
