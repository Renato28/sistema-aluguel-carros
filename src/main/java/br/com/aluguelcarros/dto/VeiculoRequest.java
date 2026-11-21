package br.com.aluguelcarros.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class VeiculoRequest {

    @NotNull(message = "A categoria é obrigatória.")
    @Positive(message = "O ID da categoria deve ser positivo.")
    private Long categoriaId;

    @NotBlank(message = "A marca é obrigatória.")
    @Size(max = 100, message = "A marca deve ter no máximo 100 caracteres.")
    private String marca;

    @NotBlank(message = "O modelo é obrigatório.")
    @Size(max = 100, message = "O modelo deve ter no máximo 100 caracteres.")
    private String modelo;

    @NotNull(message = "O ano de fabricação é obrigatório.")
    @Min(value = 1900, message = "O ano de fabricação deve ser maior ou igual a 1900.")
    private Integer anoFabricacao;

    @NotBlank(message = "A placa é obrigatória.")
    @Size(max = 10, message = "A placa deve ter no máximo 10 caracteres.")
    private String placa;

    @Size(max = 50, message = "A cor deve ter no máximo 50 caracteres.")
    private String cor;

    @NotNull(message = "A quilometragem é obrigatória.")
    @Min(value = 0, message = "A quilometragem não pode ser negativa.")
    private Integer quilometragem;

    @NotNull(message = "O valor da diária é obrigatório.")
    @DecimalMin(value = "0.01", message = "O valor da diária deve ser maior que zero.")
    private BigDecimal valorDiaria;

    public VeiculoRequest() {

    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Long categoriaId) {
        this.categoriaId = categoriaId;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public Integer getAnoFabricacao() {
        return anoFabricacao;
    }

    public void setAnoFabricacao(Integer anoFabricacao) {
        this.anoFabricacao = anoFabricacao;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getCor() {
        return cor;
    }

    public void setCor(String cor) {
        this.cor = cor;
    }

    public Integer getQuilometragem() {
        return quilometragem;
    }

    public void setQuilometragem(Integer quilometragem) {
        this.quilometragem = quilometragem;
    }

    public BigDecimal getValorDiaria() {
        return valorDiaria;
    }

    public void setValorDiaria(BigDecimal valorDiaria) {
        this.valorDiaria = valorDiaria;
    }
}
