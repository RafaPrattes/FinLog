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
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AIService {

    private static final Logger log = LoggerFactory.getLogger(AIService.class);
    private static final Locale PT_BR = Locale.of("pt", "BR");
    private static final String ERRO_CONEXAO = "Erro ao conectar com a IA. Tente novamente.";

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
            return "Comece a registrar suas transacoes para receber recomendacoes personalizadas!";
        }

        ResumoFinanceiro resumo = resumir(dados);
        String prompt = "Voce e um mentor financeiro do app FinLog. " +
                "Analise este resumo: " + resumo.texto() + ". " +
                "Ultimas transacoes: " + resumo.ultimasTransacoes() + ". " +
                "De um conselho pratico e motivador de no maximo 40 palavras, focando em reduzir gastos em " +
                resumo.categoriaComMaiorGasto() + " ou aproveitar melhor as receitas.";

        String texto = chamarGemini(prompt);
        return texto != null ? texto : "Continue acompanhando seus gastos para um futuro melhor!";
    }

    public String responderPergunta(String emailUsuario, String pergunta) {
        List<Movimentacao> dados = repository.findByUsuarioEmail(emailUsuario);

        String contexto = "O usuario ainda nao registrou transacoes.";
        if (!dados.isEmpty()) {
            ResumoFinanceiro resumo = resumir(dados);
            contexto = "Resumo: " + resumo.texto() + ". Ultimas transacoes: " + resumo.ultimasTransacoes() + ".";
        }

        String prompt = "Voce e o assistente financeiro do app FinLog. " +
                "Dados do usuario: " + contexto + " " +
                "Pergunta do usuario: \"" + pergunta.trim() + "\". " +
                "Responda em portugues, de forma pratica e objetiva, em no maximo 80 palavras, " +
                "usando os dados do usuario quando forem relevantes. " +
                "Se a pergunta nao for sobre financas pessoais, explique gentilmente que so pode ajudar com financas.";

        String texto = chamarGemini(prompt);
        return texto != null ? texto : "Nao consegui gerar uma resposta agora. Tente reformular a pergunta.";
    }

    private ResumoFinanceiro resumir(List<Movimentacao> dados) {
        BigDecimal totalReceitas = dados.stream()
                .filter(m -> m.getTipo() == TipoMovimentacao.RECEITA)
                .map(Movimentacao::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDespesas = dados.stream()
                .filter(m -> m.getTipo() == TipoMovimentacao.DESPESA)
                .map(Movimentacao::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal saldo = totalReceitas.subtract(totalDespesas);

        Map<String, BigDecimal> gastosPorCategoria = dados.stream()
                .filter(m -> m.getTipo() == TipoMovimentacao.DESPESA)
                .collect(Collectors.groupingBy(
                        m -> m.getCategoria() != null ? m.getCategoria().getNome() : "Sem categoria",
                        Collectors.reducing(BigDecimal.ZERO, Movimentacao::getValor, BigDecimal::add)
                ));

        String categoriaComMaiorGasto = gastosPorCategoria.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("gastos gerais");

        BigDecimal maiorGasto = gastosPorCategoria.values().stream()
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        String texto = String.format(
                PT_BR,
                "Receitas: R$%.2f | Despesas: R$%.2f | Saldo: R$%.2f | Maior gasto em %s: R$%.2f",
                totalReceitas,
                totalDespesas,
                saldo,
                categoriaComMaiorGasto,
                maiorGasto
        );

        String ultimasTransacoes = dados.stream()
                .sorted(Comparator.comparing(Movimentacao::getData,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .map(m -> String.format(
                        PT_BR,
                        "%s (%s): R$%.2f",
                        m.getDescricao(),
                        m.getCategoria() != null ? m.getCategoria().getNome() : "Geral",
                        m.getValor()
                ))
                .collect(Collectors.joining(" | "));

        return new ResumoFinanceiro(texto, categoriaComMaiorGasto, ultimasTransacoes);
    }

    private String chamarGemini(String prompt) {
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
            return ERRO_CONEXAO;
        } catch (RuntimeException e) {
            log.error("ERRO GEMINI - falha inesperada ao chamar a API", e);
            return ERRO_CONEXAO;
        }

        return extrairTexto(response);
    }

    private String extrairTexto(GeminiResponse response) {
        if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
            return null;
        }
        GeminiResponse.Candidate candidato = response.candidates().getFirst();
        if (candidato == null || candidato.content() == null
                || candidato.content().parts() == null || candidato.content().parts().isEmpty()) {
            return null;
        }
        GeminiResponse.Part parte = candidato.content().parts().getFirst();
        if (parte == null || parte.text() == null || parte.text().isBlank()) {
            return null;
        }
        return parte.text();
    }

    private record ResumoFinanceiro(String texto, String categoriaComMaiorGasto, String ultimasTransacoes) {
    }
}
