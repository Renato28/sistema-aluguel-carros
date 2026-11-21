package br.com.aluguelcarros.exception;

public class PlacaVeiculoDuplicadaException extends RuntimeException {
    public PlacaVeiculoDuplicadaException(String mensagem) {
        super(mensagem);
    }
}
