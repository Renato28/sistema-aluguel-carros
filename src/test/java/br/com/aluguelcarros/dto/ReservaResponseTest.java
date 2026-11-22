package br.com.aluguelcarros.dto;

import br.com.aluguelcarros.entity.StatusReserva;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ReservaResponseTest {

    @Test
    void deveCriarReservaResponseComConstrutorPadrao() {
        ReservaResponse response = new ReservaResponse();

        assertNotNull(response);
    }

    @Test
    void deveDefinirEConsultarTodosOsAtributos() {
        ReservaResponse response = new ReservaResponse();

        LocalDate dataRetirada = LocalDate.of(2026, 12, 10);
        LocalDate dataDevolucao = LocalDate.of(2026, 12, 15);
        LocalDateTime dataCriacao = LocalDateTime.of(2026, 11, 20, 10, 30, 0);
        BigDecimal valorDiaria = new BigDecimal("150.00");

        response.setId(1L);
        response.setClienteId(2L);
        response.setClienteNome("João Silva");
        response.setVeiculoId(3L);
        response.setVeiculoDescricao("Toyota Corolla");
        response.setDataRetirada(dataRetirada);
        response.setDataDevolucao(dataDevolucao);
        response.setValorDiaria(valorDiaria);
        response.setStatus(StatusReserva.PENDENTE);
        response.setDataCriacao(dataCriacao);

        assertEquals(1L, response.getId());
        assertEquals(2L, response.getClienteId());
        assertEquals("João Silva", response.getClienteNome());
        assertEquals(3L, response.getVeiculoId());
        assertEquals("Toyota Corolla", response.getVeiculoDescricao());
        assertEquals(dataRetirada, response.getDataRetirada());
        assertEquals(dataDevolucao, response.getDataDevolucao());
        assertEquals(valorDiaria, response.getValorDiaria());
        assertEquals(StatusReserva.PENDENTE, response.getStatus());
        assertEquals(dataCriacao, response.getDataCriacao());
    }
}
