package br.com.aluguelcarros.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public class ReservaRequest {

    @NotNull(message = "O ID do cliente é obrigatório.")
    @Positive(message = "O ID do cliente deve ser positivo.")
    private Long clienteId;

    @NotNull(message = "O ID do veículo é obrigatório.")
    @Positive(message = "O ID do veículo deve ser positivo.")
    private Long veiculoId;

    @NotNull(message = "A data de retirada é obrigatória.")
    private LocalDate dataRetirada;

    @NotNull(message = "A data de devolução é obrigatória.")
    private LocalDate dataDevolucao;

    public ReservaRequest() {
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Long getVeiculoId() {
        return veiculoId;
    }

    public void setVeiculoId(Long veiculoId) {
        this.veiculoId = veiculoId;
    }

    public LocalDate getDataRetirada() {
        return dataRetirada;
    }

    public void setDataRetirada(LocalDate dataRetirada) {
        this.dataRetirada = dataRetirada;
    }

    public LocalDate getDataDevolucao() {
        return dataDevolucao;
    }

    public void setDataDevolucao(LocalDate dataDevolucao) {
        this.dataDevolucao = dataDevolucao;
    }
}
