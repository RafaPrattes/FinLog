package com.ucb.finlog.service;

import com.ucb.finlog.dto.GeminiRequest;
import com.ucb.finlog.dto.GeminiResponse;
import com.ucb.finlog.model.Movimentacao;
import com.ucb.finlog.model.TipoMovimentacao;
import com.ucb.finlog.repository.MovimentacaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AIServiceTest {
    @Mock
    private RestTemplate restTemplate;

    @Mock
    private MovimentacaoRepository repository;

    @InjectMocks
    private AIService service;

    @Test
    void deveRetornarConselhoDaApiGemini() {
        ReflectionTestUtils.setField(service, "apiKey", "chave");
        ReflectionTestUtils.setField(service, "apiUrl", "https://gemini.test");

        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setTipo(TipoMovimentacao.RECEITA);
        movimentacao.setDescricao("Salario");
        movimentacao.setValor(BigDecimal.valueOf(5000));

        GeminiResponse response = new GeminiResponse(List.of(
                new GeminiResponse.Candidate(new GeminiResponse.Content(List.of(
                        new GeminiResponse.Part("Poupe parte da renda todo mês.")
                )))
        ));

        when(repository.findByUsuarioEmail("maria@email.com")).thenReturn(List.of(movimentacao));
        when(restTemplate.postForObject(eq("https://gemini.test"), any(), eq(GeminiResponse.class)))
                .thenReturn(response);

        String conselho = service.obterConselhoIA("maria@email.com");

        assertEquals("Poupe parte da renda todo mês.", conselho);

        ArgumentCaptor<HttpEntity<GeminiRequest>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(eq("https://gemini.test"), entityCaptor.capture(), eq(GeminiResponse.class));

        HttpEntity<GeminiRequest> capturedEntity = entityCaptor.getValue();
        assertEquals("chave", capturedEntity.getHeaders().getFirst("x-goog-api-key"));

        String prompt = capturedEntity.getBody().contents().getFirst().parts().getFirst().text();
        assertTrue(prompt.contains("RECEITA: Salario R$5000"));
        assertTrue(prompt.contains("Saldo atual: R$5000"));
        assertTrue(prompt.contains("Total de receitas: R$5000"));
        assertTrue(prompt.contains("Total de despesas: R$0"));
    }

    @Test
    void deveRetornarConselhoPadraoQuandoApiNaoRetornaCandidatos() {
        ReflectionTestUtils.setField(service, "apiKey", "chave");
        ReflectionTestUtils.setField(service, "apiUrl", "https://gemini.test");

        when(repository.findByUsuarioEmail("maria@email.com")).thenReturn(List.of());
        when(restTemplate.postForObject(eq("https://gemini.test"), any(), eq(GeminiResponse.class)))
                .thenReturn(new GeminiResponse(List.of()));

        assertEquals("Continue acompanhando seus gastos para um futuro melhor!", service.obterConselhoIA("maria@email.com"));
    }

    @Test
    void deveRetornarMensagemAmigavelQuandoApiFalha() {
        ReflectionTestUtils.setField(service, "apiKey", "chave");
        ReflectionTestUtils.setField(service, "apiUrl", "https://gemini.test");

        when(repository.findByUsuarioEmail("maria@email.com")).thenReturn(List.of());
        when(restTemplate.postForObject(eq("https://gemini.test"), any(), eq(GeminiResponse.class)))
                .thenThrow(HttpServerErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Erro", null, null, null));

        assertEquals("Erro ao conectar com a IA. Tente novamente.", service.obterConselhoIA("maria@email.com"));
    }
}
