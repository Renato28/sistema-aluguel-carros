package br.com.aluguelcarros.exception;

public class VeiculoIndisponivelException extends RuntimeException {

    public VeiculoIndisponivelException(String mensagem) {
        super(mensagem);
    }
}
