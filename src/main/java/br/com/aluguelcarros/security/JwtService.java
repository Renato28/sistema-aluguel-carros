package br.com.aluguelcarros.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final long expiration;

    public JwtService(
            @Value("${jwt.expiration}") long expiration,
            JwtEncoder jwtEncoder
    ) {
        this.jwtEncoder = jwtEncoder;
        this.expiration = expiration;
    }

    public String gerarToken(String email, String perfil) {

        Instant agora = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("sistema-aluguel-carros")
                .issuedAt(agora)
                .expiresAt(agora.plusMillis(expiration))
                .subject(email)
                .claim("perfil", perfil)
                .build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(
                        org.springframework.security.oauth2.jwt.JwsHeader
                                .with(MacAlgorithm.HS256)
                                .build(),
                        claims
                )
        ).getTokenValue();
    }
}