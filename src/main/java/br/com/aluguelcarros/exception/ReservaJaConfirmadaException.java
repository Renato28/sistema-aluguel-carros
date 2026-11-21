package br.com.aluguelcarros.exception;

public class ReservaJaConfirmadaException extends RuntimeException  {
    public ReservaJaConfirmadaException(String mensagem) {
        super(mensagem);
    }
}
