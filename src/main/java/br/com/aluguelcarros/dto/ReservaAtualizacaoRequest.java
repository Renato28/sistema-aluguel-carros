package br.com.aluguelcarros.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class ReservaAtualizacaoRequest {

    @NotNull(message = "A data de retirada é obrigatória.")
    private LocalDate dataRetirada;

    @NotNull(message = "A data de devolução é obrigatória.")
    private LocalDate dataDevolucao;

    public ReservaAtualizacaoRequest() {

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
