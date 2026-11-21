package br.com.aluguelcarros.exception;

public class VeiculoNaoEncontradoException extends RuntimeException {

    public VeiculoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
