package com.ucb.finlog.service;

import com.ucb.finlog.dto.ChatMensagemResponse;
import com.ucb.finlog.dto.GeminiRequest;
import com.ucb.finlog.dto.GeminiResponse;
import com.ucb.finlog.model.Categoria;
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
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AIServiceTest {
    @Mock
    private RestTemplate restTemplate;

    @Mock
    private MovimentacaoRepository repository;

    @Mock
    private MetaRepository metaRepository;

    @Mock
    private ChatMensagemRepository chatRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private AIService service;

    @Test
    void deveRetornarConselhoDaApiGemini() {
        ReflectionTestUtils.setField(service, "apiKey", "chave");
        ReflectionTestUtils.setField(service, "apiUrl", "https://gemini.test");

        Movimentacao receita = new Movimentacao();
        receita.setTipo(TipoMovimentacao.RECEITA);
        receita.setDescricao("Salario");
        receita.setValor(BigDecimal.valueOf(5000));

        Categoria categoria = new Categoria();
        categoria.setNome("Mercado");

        Movimentacao despesa = new Movimentacao();
        despesa.setTipo(TipoMovimentacao.DESPESA);
        despesa.setDescricao("Compras");
        despesa.setValor(BigDecimal.valueOf(750));
        despesa.setCategoria(categoria);

        GeminiResponse response = new GeminiResponse(List.of(
                new GeminiResponse.Candidate(new GeminiResponse.Content(List.of(
                        new GeminiResponse.Part("Poupe parte da renda todo mes.")
                )))
        ));

        when(repository.findByUsuarioEmail("maria@email.com")).thenReturn(List.of(receita, despesa));
        when(restTemplate.postForObject(eq("https://gemini.test"), any(), eq(GeminiResponse.class)))
                .thenReturn(response);

        String conselho = service.obterConselhoIA("maria@email.com");

        assertEquals("Poupe parte da renda todo mes.", conselho);

        ArgumentCaptor<HttpEntity<GeminiRequest>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(eq("https://gemini.test"), entityCaptor.capture(), eq(GeminiResponse.class));

        HttpEntity<GeminiRequest> capturedEntity = entityCaptor.getValue();
        assertEquals("chave", capturedEntity.getHeaders().getFirst("x-goog-api-key"));

        String prompt = capturedEntity.getBody().contents().getFirst().parts().getFirst().text();
        assertTrue(prompt.contains("Receitas: R$5000,00"));
        assertTrue(prompt.contains("Despesas: R$750,00"));
        assertTrue(prompt.contains("Maior gasto em Mercado: R$750,00"));
        assertTrue(prompt.contains("Compras (Mercado): R$750,00"));
    }

    @Test
    void deveRetornarMensagemParaUsuarioSemMovimentacoes() {
        ReflectionTestUtils.setField(service, "apiKey", "chave");
        ReflectionTestUtils.setField(service, "apiUrl", "https://gemini.test");

        when(repository.findByUsuarioEmail("maria@email.com")).thenReturn(List.of());

        assertEquals("Comece a registrar suas transacoes para receber recomendacoes personalizadas!", service.obterConselhoIA("maria@email.com"));
        verify(restTemplate, never()).postForObject(eq("https://gemini.test"), any(), eq(GeminiResponse.class));
    }

    @Test
    void deveRetornarConselhoPadraoQuandoApiNaoRetornaCandidatos() {
        ReflectionTestUtils.setField(service, "apiKey", "chave");
        ReflectionTestUtils.setField(service, "apiUrl", "https://gemini.test");

        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setTipo(TipoMovimentacao.RECEITA);
        movimentacao.setDescricao("Freelance");
        movimentacao.setValor(BigDecimal.valueOf(1000));

        when(repository.findByUsuarioEmail("maria@email.com")).thenReturn(List.of(movimentacao));
        when(restTemplate.postForObject(eq("https://gemini.test"), any(), eq(GeminiResponse.class)))
                .thenReturn(new GeminiResponse(List.of()));

        assertEquals("Continue acompanhando seus gastos para um futuro melhor!", service.obterConselhoIA("maria@email.com"));
    }

    @Test
    void deveResponderPerguntaDoUsuarioComContextoFinanceiro() {
        ReflectionTestUtils.setField(service, "apiKey", "chave");
        ReflectionTestUtils.setField(service, "apiUrl", "https://gemini.test");

        Movimentacao despesa = new Movimentacao();
        despesa.setTipo(TipoMovimentacao.DESPESA);
        despesa.setDescricao("Mercado");
        despesa.setValor(BigDecimal.valueOf(200));

        when(repository.findByUsuarioEmail("maria@email.com")).thenReturn(List.of(despesa));
        when(restTemplate.postForObject(eq("https://gemini.test"), any(), eq(GeminiResponse.class)))
                .thenReturn(new GeminiResponse(List.of(new GeminiResponse.Candidate(
                        new GeminiResponse.Content(List.of(new GeminiResponse.Part("Corte o delivery.")))
                ))));

        assertEquals("Corte o delivery.", service.responderPergunta("maria@email.com", "  Como economizar?  "));

        ArgumentCaptor<HttpEntity<GeminiRequest>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(eq("https://gemini.test"), entityCaptor.capture(), eq(GeminiResponse.class));
        String prompt = entityCaptor.getValue().getBody().contents().getFirst().parts().getFirst().text();
        assertTrue(prompt.contains("Pergunta do usuario: \"Como economizar?\""));
        assertTrue(prompt.contains("Despesas: R$200,00"));
    }

    @Test
    void deveResponderPerguntaMesmoSemMovimentacoes() {
        ReflectionTestUtils.setField(service, "apiKey", "chave");
        ReflectionTestUtils.setField(service, "apiUrl", "https://gemini.test");

        when(repository.findByUsuarioEmail("maria@email.com")).thenReturn(List.of());
        when(restTemplate.postForObject(eq("https://gemini.test"), any(), eq(GeminiResponse.class)))
                .thenReturn(new GeminiResponse(List.of()));

        assertEquals(
                "Nao consegui gerar uma resposta agora. Tente reformular a pergunta.",
                service.responderPergunta("maria@email.com", "O que e reserva de emergencia?")
        );
    }

    @Test
    void deveRetornarConselhoPadraoQuandoApiRetornaRespostaIncompleta() {
        ReflectionTestUtils.setField(service, "apiKey", "chave");
        ReflectionTestUtils.setField(service, "apiUrl", "https://gemini.test");

        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setTipo(TipoMovimentacao.RECEITA);
        movimentacao.setDescricao("Freelance");
        movimentacao.setValor(BigDecimal.valueOf(1000));

        when(repository.findByUsuarioEmail("maria@email.com")).thenReturn(List.of(movimentacao));
        when(restTemplate.postForObject(eq("https://gemini.test"), any(), eq(GeminiResponse.class)))
                .thenReturn(new GeminiResponse(null))
                .thenReturn(new GeminiResponse(List.of(new GeminiResponse.Candidate(null))))
                .thenReturn(new GeminiResponse(List.of(new GeminiResponse.Candidate(new GeminiResponse.Content(null)))))
                .thenReturn(new GeminiResponse(List.of(new GeminiResponse.Candidate(new GeminiResponse.Content(List.of())))));

        for (int i = 0; i < 4; i++) {
            assertEquals("Continue acompanhando seus gastos para um futuro melhor!", service.obterConselhoIA("maria@email.com"));
        }
    }

    @Test
    void deveRetornarMensagemAmigavelQuandoApiFalhaComErroHttp() {
        ReflectionTestUtils.setField(service, "apiKey", "chave");
        ReflectionTestUtils.setField(service, "apiUrl", "https://gemini.test");

        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setTipo(TipoMovimentacao.RECEITA);
        movimentacao.setDescricao("Freelance");
        movimentacao.setValor(BigDecimal.valueOf(1000));

        when(repository.findByUsuarioEmail("maria@email.com")).thenReturn(List.of(movimentacao));
        when(restTemplate.postForObject(eq("https://gemini.test"), any(), eq(GeminiResponse.class)))
                .thenThrow(HttpServerErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Erro", null, null, null));

        assertEquals("Erro ao conectar com a IA. Tente novamente.", service.obterConselhoIA("maria@email.com"));
    }

    @Test
    void deveRetornarMensagemAmigavelQuandoApiFalhaComErroGenerico() {
        ReflectionTestUtils.setField(service, "apiKey", "chave");
        ReflectionTestUtils.setField(service, "apiUrl", "https://gemini.test");

        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setTipo(TipoMovimentacao.RECEITA);
        movimentacao.setDescricao("Freelance");
        movimentacao.setValor(BigDecimal.valueOf(1000));

        when(repository.findByUsuarioEmail("maria@email.com")).thenReturn(List.of(movimentacao));
        when(restTemplate.postForObject(eq("https://gemini.test"), any(), eq(GeminiResponse.class)))
                .thenThrow(new RuntimeException("falha"));

        assertEquals("Erro ao conectar com a IA. Tente novamente.", service.obterConselhoIA("maria@email.com"));
    }

    @Test
    void deveIncluirMetasDoUsuarioNoContextoDaPergunta() {
        ReflectionTestUtils.setField(service, "apiKey", "chave");
        ReflectionTestUtils.setField(service, "apiUrl", "https://gemini.test");

        Movimentacao receita = new Movimentacao();
        receita.setTipo(TipoMovimentacao.RECEITA);
        receita.setDescricao("Salario");
        receita.setValor(BigDecimal.valueOf(3000));

        when(repository.findByUsuarioEmail("maria@email.com")).thenReturn(List.of(receita));
        when(metaRepository.findByUsuarioEmailOrderByIdAsc("maria@email.com"))
                .thenReturn(List.of(meta("Viagem", 5000), meta("Celular", 2000)));
        when(restTemplate.postForObject(eq("https://gemini.test"), any(), eq(GeminiResponse.class)))
                .thenReturn(resposta("Faltam R$2.000,00 para a viagem."));

        service.responderPergunta("maria@email.com", "Quanto falta para minha viagem?");

        String prompt = capturarConversa().getLast().parts().getFirst().text();
        assertTrue(prompt.contains("Viagem: R$5000,00 (faltam R$2000,00)"));
        assertTrue(prompt.contains("Celular: R$2000,00 (ja coberta pelo saldo)"));
    }

    @Test
    void deveAvisarIaQuandoUsuarioNaoTemMetas() {
        ReflectionTestUtils.setField(service, "apiKey", "chave");
        ReflectionTestUtils.setField(service, "apiUrl", "https://gemini.test");

        when(repository.findByUsuarioEmail("maria@email.com")).thenReturn(List.of());
        when(restTemplate.postForObject(eq("https://gemini.test"), any(), eq(GeminiResponse.class)))
                .thenReturn(resposta("Crie sua primeira meta."));

        service.responderPergunta("maria@email.com", "Tenho metas?");

        String prompt = capturarConversa().getLast().parts().getFirst().text();
        assertTrue(prompt.contains("O usuario ainda nao cadastrou metas."));
    }

    @Test
    void deveEnviarMensagensAnterioresComoTurnosDaConversa() {
        ReflectionTestUtils.setField(service, "apiKey", "chave");
        ReflectionTestUtils.setField(service, "apiUrl", "https://gemini.test");

        // O repositorio devolve da mais nova para a mais antiga
        when(chatRepository.findTop10ByUsuarioEmailOrderByIdDesc("maria@email.com")).thenReturn(List.of(
                chat(PapelMensagem.IA, "Seu maior gasto e Mercado."),
                chat(PapelMensagem.USUARIO, "Onde gasto mais?")
        ));
        when(repository.findByUsuarioEmail("maria@email.com")).thenReturn(List.of());
        when(restTemplate.postForObject(eq("https://gemini.test"), any(), eq(GeminiResponse.class)))
                .thenReturn(resposta("Cozinhe em casa."));

        service.responderPergunta("maria@email.com", "E como reduzir isso?");

        List<GeminiRequest.Content> conversa = capturarConversa();
        assertEquals(3, conversa.size());
        assertEquals("user", conversa.get(0).role());
        assertEquals("Onde gasto mais?", conversa.get(0).parts().getFirst().text());
        assertEquals("model", conversa.get(1).role());
        assertEquals("Seu maior gasto e Mercado.", conversa.get(1).parts().getFirst().text());
        assertEquals("user", conversa.get(2).role());
        assertTrue(conversa.get(2).parts().getFirst().text().contains("E como reduzir isso?"));
    }

    @Test
    void deveSalvarPerguntaERespostaQuandoIaResponde() {
        ReflectionTestUtils.setField(service, "apiKey", "chave");
        ReflectionTestUtils.setField(service, "apiUrl", "https://gemini.test");

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("maria@email.com");

        when(repository.findByUsuarioEmail("maria@email.com")).thenReturn(List.of());
        when(usuarioRepository.findByEmail("maria@email.com")).thenReturn(Optional.of(usuario));
        when(restTemplate.postForObject(eq("https://gemini.test"), any(), eq(GeminiResponse.class)))
                .thenReturn(resposta("Guarde 10% da renda."));

        service.responderPergunta("maria@email.com", "  Como poupar?  ");

        ArgumentCaptor<List<ChatMensagem>> captor = ArgumentCaptor.forClass(List.class);
        verify(chatRepository).saveAll(captor.capture());
        List<ChatMensagem> salvas = captor.getValue();
        assertEquals(2, salvas.size());
        assertEquals(PapelMensagem.USUARIO, salvas.get(0).getPapel());
        assertEquals("Como poupar?", salvas.get(0).getTexto());
        assertEquals(PapelMensagem.IA, salvas.get(1).getPapel());
        assertEquals("Guarde 10% da renda.", salvas.get(1).getTexto());
        assertEquals(usuario, salvas.get(1).getUsuario());
    }

    @Test
    void naoDeveSalvarConversaQuandoIaFalha() {
        ReflectionTestUtils.setField(service, "apiKey", "chave");
        ReflectionTestUtils.setField(service, "apiUrl", "https://gemini.test");

        when(repository.findByUsuarioEmail("maria@email.com")).thenReturn(List.of());
        when(restTemplate.postForObject(eq("https://gemini.test"), any(), eq(GeminiResponse.class)))
                .thenThrow(new RuntimeException("falha"))
                .thenReturn(new GeminiResponse(List.of()));

        assertEquals("Erro ao conectar com a IA. Tente novamente.", service.responderPergunta("maria@email.com", "Oi"));
        assertEquals(
                "Nao consegui gerar uma resposta agora. Tente reformular a pergunta.",
                service.responderPergunta("maria@email.com", "Oi")
        );
        verify(chatRepository, never()).saveAll(any());
    }

    @Test
    void deveRetornarHistoricoDaSessaoEmOrdem() {
        ChatMensagem pergunta = chat(PapelMensagem.USUARIO, "Oi");
        pergunta.setCriadaEm(Instant.parse("2026-10-22T10:00:00Z"));
        ChatMensagem resposta = chat(PapelMensagem.IA, "Ola!");
        resposta.setCriadaEm(Instant.parse("2026-10-22T10:00:02Z"));
        when(chatRepository.findByUsuarioEmailOrderByIdAsc("maria@email.com")).thenReturn(List.of(pergunta, resposta));

        assertEquals(List.of(
                new ChatMensagemResponse("USUARIO", "Oi", Instant.parse("2026-10-22T10:00:00Z")),
                new ChatMensagemResponse("IA", "Ola!", Instant.parse("2026-10-22T10:00:02Z"))
        ), service.historico("maria@email.com"));
    }

    @Test
    void conselhoDeveConsiderarAsMetas() {
        ReflectionTestUtils.setField(service, "apiKey", "chave");
        ReflectionTestUtils.setField(service, "apiUrl", "https://gemini.test");

        Movimentacao receita = new Movimentacao();
        receita.setTipo(TipoMovimentacao.RECEITA);
        receita.setDescricao("Salario");
        receita.setValor(BigDecimal.valueOf(1000));

        when(repository.findByUsuarioEmail("maria@email.com")).thenReturn(List.of(receita));
        when(metaRepository.findByUsuarioEmailOrderByIdAsc("maria@email.com")).thenReturn(List.of(meta("Notebook", 4000)));
        when(restTemplate.postForObject(eq("https://gemini.test"), any(), eq(GeminiResponse.class)))
                .thenReturn(resposta("Guarde para o notebook."));

        service.obterConselhoIA("maria@email.com");

        String prompt = capturarConversa().getFirst().parts().getFirst().text();
        assertTrue(prompt.contains("Notebook: R$4000,00 (faltam R$3000,00)"));
    }

    private List<GeminiRequest.Content> capturarConversa() {
        ArgumentCaptor<HttpEntity<GeminiRequest>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(eq("https://gemini.test"), entityCaptor.capture(), eq(GeminiResponse.class));
        return entityCaptor.getValue().getBody().contents();
    }

    private static GeminiResponse resposta(String texto) {
        return new GeminiResponse(List.of(new GeminiResponse.Candidate(
                new GeminiResponse.Content(List.of(new GeminiResponse.Part(texto)))
        )));
    }

    private static Meta meta(String descricao, long valor) {
        Meta meta = new Meta();
        meta.setDescricao(descricao);
        meta.setValor(BigDecimal.valueOf(valor));
        return meta;
    }

    private static ChatMensagem chat(PapelMensagem papel, String texto) {
        ChatMensagem mensagem = new ChatMensagem();
        mensagem.setPapel(papel);
        mensagem.setTexto(texto);
        return mensagem;
    }
}
