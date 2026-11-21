package br.com.aluguelcarros.exception;

public class PeriodoReservaInvalidoException extends RuntimeException {

    public PeriodoReservaInvalidoException(String mensagem){
        super(mensagem);
    }
}
