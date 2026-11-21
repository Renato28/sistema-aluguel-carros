package br.com.aluguelcarros.exception;

public class ReservaNaoPodeSerCanceladaException extends RuntimeException {

    public ReservaNaoPodeSerCanceladaException(String mensagem) {
        super(mensagem);
    }
}
