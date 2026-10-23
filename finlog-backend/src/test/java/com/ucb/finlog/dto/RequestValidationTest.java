package com.ucb.finlog.dto;

import com.ucb.finlog.model.TipoMovimentacao;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestValidationTest {
    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void criarValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void fecharValidator() {
        factory.close();
    }

    @Test
    void cadastroValidoNaoDeveTerViolacoes() {
        assertTrue(validator.validate(new CadastroUsuarioRequest("Maria", "maria@email.com", "123456")).isEmpty());
    }

    @Test
    void cadastroDeveExigirCamposObrigatorios() {
        Set<String> mensagens = mensagens(validator.validate(new CadastroUsuarioRequest(" ", null, "")));

        assertTrue(mensagens.contains("Nome e obrigatorio"));
        assertTrue(mensagens.contains("E-mail e obrigatorio"));
        assertTrue(mensagens.contains("Senha e obrigatoria"));
    }

    @Test
    void cadastroDeveRejeitarEmailInvalido() {
        Set<String> mensagens = mensagens(validator.validate(
                new CadastroUsuarioRequest("Maria", "email-invalido", "123456")
        ));

        assertEquals(Set.of("E-mail invalido"), mensagens);
    }

    @Test
    void cadastroDeveRejeitarSenhaCurtaOuMaiorQueLimiteDoBcrypt() {
        assertEquals(
                Set.of("A senha deve ter entre 6 e 72 caracteres"),
                mensagens(validator.validate(new CadastroUsuarioRequest("Maria", "maria@email.com", "12345")))
        );
        assertEquals(
                Set.of("A senha deve ter entre 6 e 72 caracteres"),
                mensagens(validator.validate(new CadastroUsuarioRequest("Maria", "maria@email.com", "a".repeat(73))))
        );
        assertTrue(validator.validate(new CadastroUsuarioRequest("Maria", "maria@email.com", "a".repeat(72))).isEmpty());
    }

    @Test
    void cadastroDeveLimitarTamanhoDoNomeEDoEmail() {
        Set<String> mensagens = mensagens(validator.validate(
                new CadastroUsuarioRequest("a".repeat(101), "a".repeat(250) + "@x.com", "123456")
        ));

        assertTrue(mensagens.contains("Nome deve ter no maximo 100 caracteres"));
        assertTrue(mensagens.contains("E-mail deve ter no maximo 255 caracteres"));
    }

    @Test
    void loginDeveExigirEmailESenha() {
        assertEquals(
                Set.of("E-mail e obrigatorio", "Senha e obrigatoria"),
                mensagens(validator.validate(new LoginRequest("", null)))
        );
        assertTrue(validator.validate(new LoginRequest("maria@email.com", "123456")).isEmpty());
    }

    @Test
    void movimentacaoDeveLimitarTextosECasasDecimais() {
        MovimentacaoRequest request = new MovimentacaoRequest(
                "a".repeat(256),
                new BigDecimal("10.123"),
                LocalDate.of(2026, 5, 27),
                TipoMovimentacao.DESPESA,
                null,
                "c".repeat(101)
        );

        assertEquals(
                Set.of(
                        "Descricao deve ter no maximo 255 caracteres",
                        "Valor invalido",
                        "Nome da categoria deve ter no maximo 100 caracteres"
                ),
                mensagens(validator.validate(request))
        );
    }

    @Test
    void movimentacaoDeveValidarCategoriaAninhada() {
        MovimentacaoRequest request = new MovimentacaoRequest(
                "Mercado",
                new BigDecimal("10.00"),
                LocalDate.of(2026, 5, 27),
                TipoMovimentacao.DESPESA,
                null,
                null,
                new MovimentacaoRequest.CategoriaPayload("c".repeat(101))
        );

        assertEquals(
                Set.of("Nome da categoria deve ter no maximo 100 caracteres"),
                mensagens(validator.validate(request))
        );
    }

    @Test
    void movimentacaoDeveRejeitarValorComMaisDe13DigitosInteiros() {
        MovimentacaoRequest request = new MovimentacaoRequest(
                "Mercado",
                new BigDecimal("12345678901234.00"),
                LocalDate.of(2026, 5, 27),
                TipoMovimentacao.DESPESA,
                null,
                null
        );

        assertEquals(Set.of("Valor invalido"), mensagens(validator.validate(request)));
    }

    @Test
    void categoriaDeveExigirNomeELimitarTamanhos() {
        assertEquals(
                Set.of("Nome da categoria e obrigatorio"),
                mensagens(validator.validate(new CategoriaRequest(" ", null)))
        );
        assertEquals(
                Set.of(
                        "Nome da categoria deve ter no maximo 100 caracteres",
                        "Descricao deve ter no maximo 255 caracteres"
                ),
                mensagens(validator.validate(new CategoriaRequest("c".repeat(101), "d".repeat(256))))
        );
        assertTrue(validator.validate(new CategoriaRequest("Mercado", "Compras")).isEmpty());
    }


    @Test
    void movimentacaoDeveRejeitarValorZero() {
        MovimentacaoRequest request = new MovimentacaoRequest(
                "Mercado",
                BigDecimal.ZERO,
                LocalDate.of(2026, 5, 27),
                TipoMovimentacao.DESPESA,
                null,
                null
        );

        assertEquals(
                Set.of("Valor deve ser positivo"),
                mensagens(validator.validate(request))
        );
    }

    @Test
    void movimentacaoDeveRejeitarValorNegativo() {
        MovimentacaoRequest request = new MovimentacaoRequest(
                "Mercado",
                new BigDecimal("-10.00"),
                LocalDate.of(2026, 5, 27),
                TipoMovimentacao.DESPESA,
                null,
                null
        );

        assertEquals(
                Set.of("Valor deve ser positivo"),
                mensagens(validator.validate(request))
        );
    }

    @Test
    void movimentacaoDeveExigirCamposObrigatorios() {
        MovimentacaoRequest request = new MovimentacaoRequest(
                "Mercado",
                null,
                null,
                null,
                null,
                null
        );

        assertEquals(
                Set.of(
                        "Valor e obrigatorio",
                        "Data e obrigatoria",
                        "Tipo e obrigatorio"
                ),
                mensagens(validator.validate(request))
        );
    }

    private static <T> Set<String> mensagens(Set<ConstraintViolation<T>> violacoes) {
        return violacoes.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
    }
}
