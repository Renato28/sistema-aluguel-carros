package br.com.aluguelcarros.exception;

public class CategoriaDuplicadaException extends RuntimeException {

    public CategoriaDuplicadaException(String mensagem) {
        super(mensagem);
    }
}
