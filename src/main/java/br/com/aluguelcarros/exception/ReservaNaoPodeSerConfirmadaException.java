package br.com.aluguelcarros.exception;

public class ReservaNaoPodeSerConfirmadaException extends RuntimeException {

    public ReservaNaoPodeSerConfirmadaException(String mensagem) {
        super(mensagem);
    }
}
