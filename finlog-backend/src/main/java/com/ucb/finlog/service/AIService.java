package com.ucb.finlog.service;

import com.ucb.finlog.dto.GeminiRequest;
import com.ucb.finlog.dto.GeminiResponse;
import com.ucb.finlog.model.Movimentacao;
import com.ucb.finlog.model.TipoMovimentacao;
import com.ucb.finlog.repository.MovimentacaoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
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

        if (dados.isEmpty()) {
            return "Comece a registrar suas transações para receber recomendações personalizadas!";
        }

        // Separar receitas e despesas
        BigDecimal totalReceitas = dados.stream()
                .filter(m -> m.getTipo() == TipoMovimentacao.RECEITA)
                .map(Movimentacao::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDespesas = dados.stream()
                .filter(m -> m.getTipo() == TipoMovimentacao.DESPESA)
                .map(Movimentacao::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Gastos por categoria
        Map<String, BigDecimal> gastosPorCategoria = dados.stream()
                .filter(m -> m.getTipo() == TipoMovimentacao.DESPESA)
                .collect(Collectors.groupingBy(
                        m -> m.getCategoria() != null ? m.getCategoria().getNome() : "Sem categoria",
                        Collectors.reducing(BigDecimal.ZERO, Movimentacao::getValor, BigDecimal::add)
                ));

        // Encontrar categoria com maior gasto
        String categoriaComMaiorGasto = gastosPorCategoria.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("gastos gerais");

        BigDecimal maiorGasto = gastosPorCategoria.values().stream()
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        // Montar prompt com contexto real
        String resumoFinanceiro = String.format(
                "Receitas: R$%.2f | Despesas: R$%.2f | Saldo: R$%.2f | " +
                "Maior gasto em %s: R$%.2f",
                totalReceitas, totalDespesas, totalReceitas.subtract(totalDespesas),
                categoriaComMaiorGasto, maiorGasto
        );

        String listaUltimasTransacoes = dados.stream()
                .limit(5)
                .map(m -> String.format("%s (%s): R$%.2f", 
                        m.getDescricao(), 
                        m.getCategoria() != null ? m.getCategoria().getNome() : "Geral",
                        m.getValor()))
                .collect(Collectors.joining(" | "));

        String prompt = "Você é um mentor financeiro do app FinLog. " +
                "Analise este resumo: " + resumoFinanceiro + ". " +
                "Últimas transações: " + listaUltimasTransacoes + ". " +
                "Dê um conselho prático e motivador (máximo 40 palavras) focando em reduzir gastos em " + 
                categoriaComMaiorGasto + " ou aproveitar bem as receitas." +
                " De conselhos simples utilizando técnicas de finanças comportamentais, como orçamento";

        var request = new GeminiRequest(List.of(
                new GeminiRequest.Content(List.of(
                        new GeminiRequest.Part(prompt)
                ))
        ));

        try {
            String urlComChave = apiUrl + "?key=" + apiKey;
            GeminiResponse response = restTemplate.postForObject(urlComChave, request, GeminiResponse.class);

            if (response != null && !response.candidates().isEmpty()) {
                return response.candidates().get(0).content().parts().get(0).text();
            }
        } catch (Exception e) {
            return "Erro ao conectar com a IA. Tente novamente.";
        }

        return "Continue acompanhando seus gastos para um futuro melhor!";
    }
}