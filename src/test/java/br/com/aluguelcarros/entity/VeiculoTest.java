package br.com.aluguelcarros.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VeiculoTest {

    @Test
    void deveInicializarQuilometragemComZero() {
        Veiculo veiculo = new Veiculo();

        assertEquals(0, veiculo.getQuilometragem());
    }
}