package com.ucb.finlog.service;

import com.ucb.finlog.dto.GeminiRequest;
import com.ucb.finlog.dto.GeminiResponse;
import com.ucb.finlog.model.Movimentacao;
import com.ucb.finlog.repository.MovimentacaoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AIService {

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

        String prompt = "Aja como um mentor financeiro para o app FinLog. " +
                "Analise estas transacoes: " + resumoFinanceiro +
                ". De um conselho curto e motivador de no maximo 20 palavras.";

        var request = new GeminiRequest(List.of(
                new GeminiRequest.Content(List.of(
                        new GeminiRequest.Part(prompt)
                ))
        ));

        String urlComChave = apiUrl + "?key=" + apiKey;
        GeminiResponse response = restTemplate.postForObject(urlComChave, request, GeminiResponse.class);

        if (response != null && !response.candidates().isEmpty()) {
            return response.candidates().get(0).content().parts().get(0).text();
        }

        return "Continue acompanhando seus gastos para um futuro melhor!";
    }
}
