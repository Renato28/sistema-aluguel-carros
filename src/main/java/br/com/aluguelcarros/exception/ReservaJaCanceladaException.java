package br.com.aluguelcarros.exception;

public class ReservaJaCanceladaException extends RuntimeException {

    public ReservaJaCanceladaException(String mensagem) {
        super(mensagem);
    }
}
