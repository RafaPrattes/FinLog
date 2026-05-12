package com.ucb.finlog.service;

import com.ucb.finlog.dto.GeminiRequest;
import com.ucb.finlog.dto.GeminiResponse;
import com.ucb.finlog.model.Movimentacao;
import com.ucb.finlog.repository.MovimentacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private MovimentacaoRepository repository;

    public String obterConselhoIA() {
        // 1. Pegar dados do MySQL que você já configurou
        List<Movimentacao> dados = repository.findAll();

        String resumoFinanceiro = dados.stream()
                .map(m -> m.getTipo() + ": " + m.getDescricao() + " R$" + m.getValor())
                .collect(Collectors.joining(", "));

        // 2. Montar o Prompt técnico
        String prompt = "Aja como um mentor financeiro para o app FinLog. " +
                "Analise estas transações: " + resumoFinanceiro +
                ". Dê um conselho curto e motivador de no máximo 20 palavras.";

        // 3. Montar o JSON para o Google
        var request = new GeminiRequest(List.of(
                new GeminiRequest.Content(List.of(
                        new GeminiRequest.Part(prompt)
                ))
        ));

        // 4. Chamar a API real
        String urlComChave = apiUrl + "?key=" + apiKey;
        GeminiResponse response = restTemplate.postForObject(urlComChave, request, GeminiResponse.class);

        // 5. Extrair o texto da resposta
        if (response != null && !response.candidates().isEmpty()) {
            return response.candidates().get(0).content().parts().get(0).text();
        }

        return "Continue acompanhando seus gastos para um futuro melhor!";
    }
}