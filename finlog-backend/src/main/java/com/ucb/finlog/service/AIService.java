package com.ucb.finlog.service;

import com.ucb.finlog.dto.GeminiRequest;
import com.ucb.finlog.dto.GeminiResponse;
import com.ucb.finlog.model.Movimentacao;
import com.ucb.finlog.model.TipoMovimentacao;
import com.ucb.finlog.repository.MovimentacaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AIService {

    private static final Logger log = LoggerFactory.getLogger(AIService.class);

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final MovimentacaoRepository repository;

    public AIService(RestTemplate restTemplate, MovimentacaoRepository repository) {
        this.restTemplate = restTemplate;
        this.repository = repository;
    }

    public String obterConselhoIA(String emailUsuario) {
        List<Movimentacao> dados = repository.findByUsuarioEmail(emailUsuario);

        String resumoFinanceiro = dados.stream()
                .map(m -> m.getTipo() + ": " + m.getDescricao() + " R$" + m.getValor())
                .collect(Collectors.joining(", "));

        BigDecimal totalReceitas = dados.stream()
                .filter(m -> m.getTipo() == TipoMovimentacao.RECEITA)
                .map(Movimentacao::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDespesas = dados.stream()
                .filter(m -> m.getTipo() == TipoMovimentacao.DESPESA)
                .map(Movimentacao::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal saldo = totalReceitas.subtract(totalDespesas);

        // Observação: a lógica de "maior categoria de gasto" (presente na branch
        // Ultima-do-Teo) depende do modelo Categoria, que ainda não existe no master.
        // Será incorporada automaticamente quando o merge trouxer esse modelo.
        String prompt = "Aja como um mentor financeiro para o app FinLog. " +
                "Saldo atual: R$" + saldo + ". Total de receitas: R$" + totalReceitas +
                ". Total de despesas: R$" + totalDespesas + ". " +
                "Analise estas transacoes: " + resumoFinanceiro +
                ". De um conselho curto e motivador de no maximo 20 palavras.";

        var request = new GeminiRequest(List.of(
                new GeminiRequest.Content(List.of(
                        new GeminiRequest.Part(prompt)
                ))
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey);

        HttpEntity<GeminiRequest> entity = new HttpEntity<>(request, headers);

        GeminiResponse response;
        try {
            response = restTemplate.postForObject(apiUrl, entity, GeminiResponse.class);
        } catch (HttpStatusCodeException e) {
            log.error("ERRO GEMINI - status={} corpo={}", e.getStatusCode(), e.getResponseBodyAsString());
            return "Erro ao conectar com a IA. Tente novamente.";
        } catch (RuntimeException e) {
            log.error("ERRO GEMINI - falha inesperada ao chamar a API", e);
            return "Erro ao conectar com a IA. Tente novamente.";
        }

        if (response != null && !response.candidates().isEmpty()) {
            return response.candidates().get(0).content().parts().get(0).text();
        }

        return "Continue acompanhando seus gastos para um futuro melhor!";
    }
}
