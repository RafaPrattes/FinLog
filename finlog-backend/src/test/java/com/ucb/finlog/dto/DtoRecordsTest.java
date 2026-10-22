package com.ucb.finlog.dto;

import com.ucb.finlog.model.TipoMovimentacao;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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

    @Test
    void movimentacaoRequestDeveAceitarCategoriaPorNomeOuPayloadAninhado() {
        MovimentacaoRequest porNome = new MovimentacaoRequest(
                "Mercado",
                BigDecimal.TEN,
                LocalDate.of(2026, 5, 28),
                TipoMovimentacao.DESPESA,
                null,
                "Alimentacao"
        );
        MovimentacaoRequest porPayloadAninhado = new MovimentacaoRequest(
                "Mercado",
                BigDecimal.TEN,
                LocalDate.of(2026, 5, 28),
                TipoMovimentacao.DESPESA,
                null,
                null,
                new MovimentacaoRequest.CategoriaPayload("Alimentacao")
        );
        MovimentacaoRequest semCategoria = new MovimentacaoRequest(
                "Mercado",
                BigDecimal.TEN,
                LocalDate.of(2026, 5, 28),
                TipoMovimentacao.DESPESA,
                null,
                null
        );

        assertEquals("Alimentacao", porNome.categoriaNomeEfetivo());
        assertEquals("Alimentacao", porPayloadAninhado.categoriaNomeEfetivo());
        assertNull(semCategoria.categoriaNomeEfetivo());
    }
}
