package com.ucb.finlog.exception;

import com.ucb.finlog.dto.ApiErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class ApiExceptionHandlerTest {
    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void deveTratarResponseStatusException() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/cadastro");

        ResponseEntity<ApiErrorResponse> response = handler.handleResponseStatusException(
                new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado"),
                request
        );

        assertEquals(409, response.getStatusCode().value());
        assertEquals("Conflito", response.getBody().error());
        assertEquals("E-mail já cadastrado", response.getBody().message());
        assertEquals("/api/cadastro", response.getBody().path());
    }

    @Test
    void deveTraduzirTodosOsStatusMapeadosEmResponseStatusException() {
        assertResponseStatus(HttpStatus.BAD_REQUEST, "Requisição inválida");
        assertResponseStatus(HttpStatus.UNAUTHORIZED, "Não autorizado");
        assertResponseStatus(HttpStatus.FORBIDDEN, "Acesso negado");
        assertResponseStatus(HttpStatus.NOT_FOUND, "Não encontrado");
        assertResponseStatus(HttpStatus.CONFLICT, "Conflito");
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
        assertEquals("Requisição inválida", response.getBody().error());
        assertEquals("Body da requisição inválido", response.getBody().message());
    }

    @Test
    void deveTratarErroInterno() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/ai/conselho");

        ResponseEntity<ApiErrorResponse> response = handler.handleException(new RuntimeException("falha"), request);

        assertEquals(500, response.getStatusCode().value());
        assertEquals("Erro interno do servidor", response.getBody().message());
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
