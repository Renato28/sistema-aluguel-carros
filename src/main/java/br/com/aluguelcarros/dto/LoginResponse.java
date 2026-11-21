package br.com.aluguelcarros.dto;

public record LoginResponse(
        String token,
        String tipo,
        long expiresIn
) {
}
