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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        when(restTemplate.postForObject(eq("https://gemini.test?key=chave"), org.mockito.ArgumentMatchers.any(), eq(GeminiResponse.class)))
                .thenReturn(response);

        String conselho = service.obterConselhoIA("maria@email.com");

        assertEquals("Poupe parte da renda todo mês.", conselho);

        ArgumentCaptor<GeminiRequest> requestCaptor = ArgumentCaptor.forClass(GeminiRequest.class);
        verify(restTemplate).postForObject(eq("https://gemini.test?key=chave"), requestCaptor.capture(), eq(GeminiResponse.class));
        String prompt = requestCaptor.getValue().contents().getFirst().parts().getFirst().text();
        assertTrue(prompt.contains("RECEITA: Salario R$5000"));
    }

    @Test
    void deveRetornarConselhoPadraoQuandoApiNaoRetornaCandidatos() {
        ReflectionTestUtils.setField(service, "apiKey", "chave");
        ReflectionTestUtils.setField(service, "apiUrl", "https://gemini.test");

        when(repository.findByUsuarioEmail("maria@email.com")).thenReturn(List.of());
        when(restTemplate.postForObject(eq("https://gemini.test?key=chave"), org.mockito.ArgumentMatchers.any(), eq(GeminiResponse.class)))
                .thenReturn(new GeminiResponse(List.of()));

        assertEquals("Continue acompanhando seus gastos para um futuro melhor!", service.obterConselhoIA("maria@email.com"));
    }
}
