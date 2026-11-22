package br.com.aluguelcarros.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JwtServiceTest {

    private JwtEncoder jwtEncoder;
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtEncoder = mock(JwtEncoder.class);
        jwtService = new JwtService(3600000L, jwtEncoder);
    }

    @Test
    void deveGerarTokenComSucesso() {

        var jwt = mock(Jwt.class);

        when(jwt.getTokenValue()).thenReturn("token-jwt-teste");
        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(jwt);

        String token = jwtService.gerarToken(
                "cliente@email.com",
                "CLIENTE"
        );

        assertNotNull(token);
        assertEquals("token-jwt-teste", token);

        ArgumentCaptor<JwtEncoderParameters> captor =
                ArgumentCaptor.forClass(JwtEncoderParameters.class);

        verify(jwtEncoder).encode(captor.capture());

        JwtClaimsSet claims = captor.getValue().getClaims();

        assertEquals("sistema-aluguel-carros", claims.getClaim("iss"));
        assertEquals("cliente@email.com", claims.getSubject());
        assertEquals("CLIENTE", claims.getClaim("perfil"));

        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiresAt());

        assertEquals(
                3600000L,
                Duration.between(
                        claims.getIssuedAt(),
                        claims.getExpiresAt()
                ).toMillis()
        );
    }

    @Test
    void devePropagarExcecaoQuandoEncoderFalhar() {

        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
                .thenThrow(new IllegalStateException("Falha ao gerar token"));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> jwtService.gerarToken(
                        "cliente@email.com",
                        "CLIENTE"
                )
        );

        assertEquals("Falha ao gerar token", exception.getMessage());

        verify(jwtEncoder).encode(any(JwtEncoderParameters.class));
    }
}
