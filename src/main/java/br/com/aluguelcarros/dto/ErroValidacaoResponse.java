package br.com.aluguelcarros.dto;

import java.time.LocalDateTime;
import java.util.Map;

public class ErroValidacaoResponse {

    private int status;
    private String mensagem;
    private Map<String, String> erros;
    private LocalDateTime timestamp;

    public ErroValidacaoResponse(int status, String mensagem, Map<String, String> erros, LocalDateTime timestamp) {
        this.status = status;
        this.mensagem = mensagem;
        this.erros = erros;
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getMensagem() {
        return mensagem;
    }

    public Map<String, String> getErros() {
        return erros;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
