package com.ucb.finlog.exception;

import com.ucb.finlog.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResourceFound(
            NoResourceFoundException exception,
            HttpServletRequest request
    ) {
        return erro(HttpStatus.NOT_FOUND, "Recurso nao encontrado", request);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ApiErrorResponse> handleParametroInvalido(Exception exception, HttpServletRequest request) {
        return erro(HttpStatus.BAD_REQUEST, "Parametro da requisicao invalido", request);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException exception,
            HttpServletRequest request
    ) {
        return erro(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Content-Type nao suportado", request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        return erro(HttpStatus.CONFLICT, "Registro duplicado ou em uso", request);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatusException(
            ResponseStatusException exception,
            HttpServletRequest request
    ) {
        HttpStatusCode statusCode = exception.getStatusCode();
        HttpStatus status = HttpStatus.resolve(statusCode.value());
        String error = status != null ? traduzirStatus(status) : "Erro";

        return ResponseEntity
                .status(statusCode)
                .body(new ApiErrorResponse(
                        statusCode.value(),
                        error,
                        exception.getReason(),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .badRequest()
                .body(new ApiErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        traduzirStatus(HttpStatus.BAD_REQUEST),
                        "Body da requisicao invalido",
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> fieldError.getDefaultMessage())
                .orElse("Dados da requisicao invalidos");

        return ResponseEntity
                .badRequest()
                .body(new ApiErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        traduzirStatus(HttpStatus.BAD_REQUEST),
                        message,
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(new ApiErrorResponse(
                        HttpStatus.METHOD_NOT_ALLOWED.value(),
                        traduzirStatus(HttpStatus.METHOD_NOT_ALLOWED),
                        exception.getMessage(),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleException(Exception exception, HttpServletRequest request) {
        log.error("Erro nao tratado em {} {}", request.getMethod(), request.getRequestURI(), exception);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        traduzirStatus(HttpStatus.INTERNAL_SERVER_ERROR),
                        "Erro interno do servidor",
                        request.getRequestURI()
                ));
    }

    private ResponseEntity<ApiErrorResponse> erro(HttpStatus status, String message, HttpServletRequest request) {
        return ResponseEntity
                .status(status)
                .body(new ApiErrorResponse(status.value(), traduzirStatus(status), message, request.getRequestURI()));
    }

    private String traduzirStatus(HttpStatus status) {
        return switch (status) {
            case UNSUPPORTED_MEDIA_TYPE -> "Tipo de midia nao suportado";
            case BAD_REQUEST -> "Requisicao invalida";
            case UNAUTHORIZED -> "Nao autorizado";
            case FORBIDDEN -> "Acesso negado";
            case NOT_FOUND -> "Nao encontrado";
            case CONFLICT -> "Conflito";
            case METHOD_NOT_ALLOWED -> "Metodo nao permitido";
            case TOO_MANY_REQUESTS -> "Muitas tentativas";
            case INTERNAL_SERVER_ERROR -> "Erro interno do servidor";
            default -> status.getReasonPhrase();
        };
    }
}
