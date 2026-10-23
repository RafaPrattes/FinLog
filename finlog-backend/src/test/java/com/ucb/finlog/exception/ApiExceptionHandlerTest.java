package com.ucb.finlog.exception;

import com.ucb.finlog.dto.ApiErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApiExceptionHandlerTest {
    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void deveTratarResponseStatusException() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/cadastro");

        ResponseEntity<ApiErrorResponse> response = handler.handleResponseStatusException(
                new ResponseStatusException(HttpStatus.CONFLICT, "E-mail ja cadastrado"),
                request
        );

        assertEquals(409, response.getStatusCode().value());
        assertEquals("Conflito", response.getBody().error());
        assertEquals("E-mail ja cadastrado", response.getBody().message());
        assertEquals("/api/cadastro", response.getBody().path());
    }

    @Test
    void deveTraduzirTodosOsStatusMapeadosEmResponseStatusException() {
        assertResponseStatus(HttpStatus.BAD_REQUEST, "Requisicao invalida");
        assertResponseStatus(HttpStatus.UNAUTHORIZED, "Nao autorizado");
        assertResponseStatus(HttpStatus.FORBIDDEN, "Acesso negado");
        assertResponseStatus(HttpStatus.NOT_FOUND, "Nao encontrado");
        assertResponseStatus(HttpStatus.CONFLICT, "Conflito");
        assertResponseStatus(HttpStatus.METHOD_NOT_ALLOWED, "Metodo nao permitido");
        assertResponseStatus(HttpStatus.TOO_MANY_REQUESTS, "Muitas tentativas");
        assertResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor");
    }

    @Test
    void deveUsarReasonPhraseParaStatusNaoMapeado() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/teste");

        ResponseEntity<ApiErrorResponse> response = handler.handleResponseStatusException(
                new ResponseStatusException(HttpStatus.I_AM_A_TEAPOT, "Bule"),
                request
        );

        assertEquals("I'm a teapot", response.getBody().error());
        assertEquals("Bule", response.getBody().message());
    }

    @Test
    void deveUsarErroQuandoStatusCodeNaoForHttpStatusConhecido() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/teste");

        ResponseEntity<ApiErrorResponse> response = handler.handleResponseStatusException(
                new ResponseStatusException(HttpStatusCode.valueOf(599), "Erro externo"),
                request
        );

        assertEquals(599, response.getStatusCode().value());
        assertEquals("Erro", response.getBody().error());
        assertEquals("Erro externo", response.getBody().message());
    }

    @Test
    void deveTratarBodyInvalido() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");

        ResponseEntity<ApiErrorResponse> response = handler.handleHttpMessageNotReadable(
                new HttpMessageNotReadableException("json invalido", mock(HttpInputMessage.class)),
                request
        );

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Requisicao invalida", response.getBody().error());
        assertEquals("Body da requisicao invalido", response.getBody().message());
    }

    @Test
    void deveTratarValidacaoInvalida() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/movimentacoes");
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("movimentacaoRequest", "valor", "Valor e obrigatorio")
        ));

        ResponseEntity<ApiErrorResponse> response = handler.handleMethodArgumentNotValid(exception, request);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Requisicao invalida", response.getBody().error());
        assertEquals("Valor e obrigatorio", response.getBody().message());
        assertEquals("/api/movimentacoes", response.getBody().path());
    }

    @Test
    void deveTratarMetodoHttpNaoSuportado() {
        MockHttpServletRequest request = new MockHttpServletRequest("PATCH", "/api/movimentacoes/10");

        ResponseEntity<ApiErrorResponse> response = handler.handleHttpRequestMethodNotSupported(
                new HttpRequestMethodNotSupportedException("PATCH"),
                request
        );

        assertEquals(405, response.getStatusCode().value());
        assertEquals("Metodo nao permitido", response.getBody().error());
        assertEquals("/api/movimentacoes/10", response.getBody().path());
    }

    @Test
    void deveTratarErroInterno() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/ai/conselho");

        ResponseEntity<ApiErrorResponse> response = handler.handleException(new RuntimeException("falha"), request);

        assertEquals(500, response.getStatusCode().value());
        assertEquals("Erro interno do servidor", response.getBody().message());
    }

    @Test
    void deveRetornar404ParaRotaInexistente() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/nao-existe");

        ResponseEntity<ApiErrorResponse> response = handler.handleNoResourceFound(mock(NoResourceFoundException.class), request);

        assertEquals(404, response.getStatusCode().value());
        assertEquals("Nao encontrado", response.getBody().error());
        assertEquals("/api/nao-existe", response.getBody().path());
    }

    @Test
    void deveRetornar400ParaParametroInvalido() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/movimentacoes/abc");

        ResponseEntity<ApiErrorResponse> response = handler.handleParametroInvalido(
                mock(MethodArgumentTypeMismatchException.class),
                request
        );

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Parametro da requisicao invalido", response.getBody().message());
    }

    @Test
    void deveRetornar415ParaContentTypeNaoSuportado() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/movimentacoes");

        ResponseEntity<ApiErrorResponse> response = handler.handleMediaTypeNotSupported(
                new HttpMediaTypeNotSupportedException("text/plain"),
                request
        );

        assertEquals(415, response.getStatusCode().value());
        assertEquals("Tipo de midia nao suportado", response.getBody().error());
    }

    @Test
    void deveRetornar409ParaViolacaoDeIntegridade() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/categorias");

        ResponseEntity<ApiErrorResponse> response = handler.handleDataIntegrityViolation(
                new DataIntegrityViolationException("duplicado"),
                request
        );

        assertEquals(409, response.getStatusCode().value());
        assertEquals("Conflito", response.getBody().error());
    }

    private void assertResponseStatus(HttpStatus status, String error) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/teste");

        ResponseEntity<ApiErrorResponse> response = handler.handleResponseStatusException(
                new ResponseStatusException(status, "Mensagem"),
                request
        );

        assertEquals(status.value(), response.getStatusCode().value());
        assertEquals(error, response.getBody().error());
        assertEquals("Mensagem", response.getBody().message());
        assertEquals("/api/teste", response.getBody().path());
    }
}
