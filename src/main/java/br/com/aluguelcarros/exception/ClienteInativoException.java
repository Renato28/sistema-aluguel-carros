package br.com.aluguelcarros.exception;

public class ClienteInativoException extends RuntimeException {

    public ClienteInativoException(String mensagem) {
        super(mensagem);
    }
}
