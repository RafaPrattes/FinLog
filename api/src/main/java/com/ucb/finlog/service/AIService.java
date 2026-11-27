package com.ucb.finlog.service;

import com.ucb.finlog.dto.ChatMensagemResponse;
import com.ucb.finlog.dto.GeminiRequest;
import com.ucb.finlog.dto.GeminiResponse;
import com.ucb.finlog.model.ChatMensagem;
import com.ucb.finlog.model.Meta;
import com.ucb.finlog.model.Movimentacao;
import com.ucb.finlog.model.PapelMensagem;
import com.ucb.finlog.model.TipoMovimentacao;
import com.ucb.finlog.model.Usuario;
import com.ucb.finlog.repository.ChatMensagemRepository;
import com.ucb.finlog.repository.MetaRepository;
import com.ucb.finlog.repository.MovimentacaoRepository;
import com.ucb.finlog.repository.UsuarioRepository;
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
import java.time.Instant;
import java.util.ArrayList;
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
    private final MetaRepository metaRepository;
    private final ChatMensagemRepository chatRepository;
    private final UsuarioRepository usuarioRepository;

    public AIService(
            RestTemplate restTemplate,
            MovimentacaoRepository repository,
            MetaRepository metaRepository,
            ChatMensagemRepository chatRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.restTemplate = restTemplate;
        this.repository = repository;
        this.metaRepository = metaRepository;
        this.chatRepository = chatRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public String obterConselhoIA(String emailUsuario) {
        List<Movimentacao> dados = repository.findByUsuarioEmail(emailUsuario);

        if (dados.isEmpty()) {
            return "Comece a registrar suas transacoes para receber recomendacoes personalizadas!";
        }

        ResumoFinanceiro resumo = resumir(dados);
        String metas = descreverMetas(metaRepository.findByUsuarioEmailOrderByIdAsc(emailUsuario), resumo.saldo());
        String prompt = "Voce e um mentor financeiro do app FinLog. " +
                "Analise este resumo: " + resumo.texto() + ". " +
                "Ultimas transacoes: " + resumo.ultimasTransacoes() + ". " +
                metas + " " +
                "De um conselho pratico e motivador de no maximo 40 palavras, focando em reduzir gastos em " +
                resumo.categoriaComMaiorGasto() + " ou aproveitar melhor as receitas para alcancar as metas.";

        String texto = chamarGemini(List.of(conteudo("user", prompt)));
        return texto != null ? texto : "Continue acompanhando seus gastos para um futuro melhor!";
    }

    public List<ChatMensagemResponse> historico(String emailUsuario) {
        return chatRepository.findByUsuarioEmailOrderByIdAsc(emailUsuario).stream()
                .map(ChatMensagemResponse::from)
                .toList();
    }

    public String responderPergunta(String emailUsuario, String pergunta) {
        Instant perguntadaEm = Instant.now();
        List<Movimentacao> dados = repository.findByUsuarioEmail(emailUsuario);

        String contexto = "O usuario ainda nao registrou transacoes.";
        BigDecimal saldo = BigDecimal.ZERO;
        if (!dados.isEmpty()) {
            ResumoFinanceiro resumo = resumir(dados);
            contexto = "Resumo: " + resumo.texto() + ". Ultimas transacoes: " + resumo.ultimasTransacoes() + ".";
            saldo = resumo.saldo();
        }
        contexto += " " + descreverMetas(metaRepository.findByUsuarioEmailOrderByIdAsc(emailUsuario), saldo);

        String prompt = "Voce e o assistente financeiro do app FinLog. " +
                "Dados do usuario: " + contexto + " " +
                "No FinLog, metas sao criadas na tela Metas (botao \"Nova meta\") e lancamentos pelo botao \"Novo lancamento\". " +
                "Pergunta do usuario: \"" + pergunta.trim() + "\". " +
                "Responda em portugues, de forma pratica e objetiva, em no maximo 80 palavras, " +
                "usando os dados do usuario e as mensagens anteriores da conversa quando forem relevantes. " +
                "Se a pergunta nao for sobre financas pessoais, explique gentilmente que so pode ajudar com financas.";

        // As mensagens anteriores vao como turnos da conversa para a IA lembrar do contexto
        List<GeminiRequest.Content> conversa = new ArrayList<>();
        chatRepository.findTop10ByUsuarioEmailOrderByIdDesc(emailUsuario).reversed().forEach(m ->
                conversa.add(conteudo(m.getPapel() == PapelMensagem.USUARIO ? "user" : "model", m.getTexto())));
        conversa.add(conteudo("user", prompt));

        String texto = chamarGemini(conversa);
        if (texto == null) {
            return "Nao consegui gerar uma resposta agora. Tente reformular a pergunta.";
        }
        if (!ERRO_CONEXAO.equals(texto)) {
            salvarTroca(emailUsuario, pergunta.trim(), perguntadaEm, texto);
        }
        return texto;
    }

    private void salvarTroca(String emailUsuario, String pergunta, Instant perguntadaEm, String resposta) {
        usuarioRepository.findByEmail(emailUsuario).ifPresent(usuario -> chatRepository.saveAll(List.of(
                mensagem(usuario, PapelMensagem.USUARIO, pergunta, perguntadaEm),
                mensagem(usuario, PapelMensagem.IA, resposta, Instant.now())
        )));
    }

    private ChatMensagem mensagem(Usuario usuario, PapelMensagem papel, String texto, Instant criadaEm) {
        ChatMensagem mensagem = new ChatMensagem();
        mensagem.setUsuario(usuario);
        mensagem.setPapel(papel);
        mensagem.setTexto(texto.length() > ChatMensagem.TAMANHO_MAXIMO_TEXTO
                ? texto.substring(0, ChatMensagem.TAMANHO_MAXIMO_TEXTO)
                : texto);
        mensagem.setCriadaEm(criadaEm);
        return mensagem;
    }

    private String descreverMetas(List<Meta> metas, BigDecimal saldo) {
        if (metas.isEmpty()) {
            return "O usuario ainda nao cadastrou metas.";
        }
        BigDecimal disponivel = saldo.max(BigDecimal.ZERO);
        return "Metas do usuario (quanto falta e calculado com o saldo atual, para cada meta separadamente): " +
                metas.stream()
                        .map(m -> {
                            BigDecimal falta = m.getValor().subtract(disponivel);
                            return falta.signum() > 0
                                    ? String.format(PT_BR, "%s: R$%.2f (faltam R$%.2f)", m.getDescricao(), m.getValor(), falta)
                                    : String.format(PT_BR, "%s: R$%.2f (ja coberta pelo saldo)", m.getDescricao(), m.getValor());
                        })
                        .collect(Collectors.joining(" | ")) + ".";
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

        return new ResumoFinanceiro(texto, categoriaComMaiorGasto, ultimasTransacoes, saldo);
    }

    private static GeminiRequest.Content conteudo(String role, String texto) {
        return new GeminiRequest.Content(role, List.of(new GeminiRequest.Part(texto)));
    }

    private String chamarGemini(List<GeminiRequest.Content> conversa) {
        var request = new GeminiRequest(conversa);

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

    private record ResumoFinanceiro(String texto, String categoriaComMaiorGasto, String ultimasTransacoes, BigDecimal saldo) {
    }
}
