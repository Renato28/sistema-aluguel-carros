package br.com.aluguelcarros.exception;

public class CpfClienteDuplicadoException extends RuntimeException {

    public CpfClienteDuplicadoException(String mensagem) {
        super(mensagem);
    }
}
