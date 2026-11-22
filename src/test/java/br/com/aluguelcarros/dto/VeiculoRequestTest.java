package br.com.aluguelcarros.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VeiculoRequestTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void configurarValidador() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void fecharFactory() {
        factory.close();
    }

    @Test
    void deveRejeitarQuilometragemNula() {
        VeiculoRequest request = criarRequestValido();
        request.setQuilometragem(null);

        var violations = validator.validate(request);

        assertTrue(violations.stream()
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("quilometragem")));
    }

    @Test
    void deveAceitarQuilometragemZero() {
        VeiculoRequest request = criarRequestValido();
        request.setQuilometragem(0);

        var violations = validator.validate(request);

        assertFalse(violations.stream()
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("quilometragem")));
    }

    @Test
    void deveRejeitarQuilometragemNegativa() {
        VeiculoRequest request = criarRequestValido();
        request.setQuilometragem(-1);

        var violations = validator.validate(request);

        assertTrue(violations.stream()
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("quilometragem")));
    }

    private VeiculoRequest criarRequestValido() {
        VeiculoRequest request = new VeiculoRequest();

        request.setCategoriaId(1L);
        request.setMarca("Toyota");
        request.setModelo("Corolla");
        request.setAnoFabricacao(2025);
        request.setPlaca("ABC1D23");
        request.setCor("Preto");
        request.setQuilometragem(1000);
        request.setValorDiaria(new java.math.BigDecimal("250.00"));

        return request;
    }
}