package br.com.aluguelcarros.exception;

public class ReservaNaoPodeSerAlteradaException extends RuntimeException {
    public ReservaNaoPodeSerAlteradaException(String mensagem) {
        super(mensagem);
    }
}
