package br.com.aluguelcarros.exception;

public class EmailUsuarioDuplicadoException extends RuntimeException {

    public EmailUsuarioDuplicadoException(String mensagem) {
        super(mensagem);
    }
}
