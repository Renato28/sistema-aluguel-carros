package br.com.aluguelcarros.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class SecurityConfigTest {

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig();
    }

    @Test
    void deveCriarPasswordEncoderBCrypt() {

        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

        String senha = "senha123";
        String senhaCriptografada = passwordEncoder.encode(senha);

        assertNotNull(passwordEncoder);
        assertNotEquals(senha, senhaCriptografada);
        assertTrue(passwordEncoder.matches(senha, senhaCriptografada));
    }

    @Test
    void deveCriarJwtSecretKey() {

        String segredo = Base64.getEncoder()
                .encodeToString("minha-chave-para-testes-1234566".getBytes());

        SecretKey secretKey = securityConfig.jwtSecretKey(segredo);

        assertNotNull(secretKey);
        assertEquals("HmacSHA256", secretKey.getAlgorithm());
        assertArrayEquals(
                Base64.getDecoder().decode(segredo),
                secretKey.getEncoded()
        );
    }

    @Test
    void deveCriarJwtEncoder() {

        SecretKey secretKey = securityConfig.jwtSecretKey(
                Base64.getEncoder().encodeToString(
                        "minha-chave-para-testes-1234566".getBytes()
                )
        );

        JwtEncoder jwtEncoder = securityConfig.jwtEncoder(secretKey);

        assertNotNull(jwtEncoder);
        assertInstanceOf(NimbusJwtEncoder.class, jwtEncoder);
    }

    @Test
    void deveCriarJwtDecoder() {

        SecretKey secretKey = securityConfig.jwtSecretKey(
                Base64.getEncoder().encodeToString(
                        "minha-chave-para-testes-1234566".getBytes()
                )
        );

        JwtDecoder jwtDecoder = securityConfig.jwtDecoder(secretKey);

        assertNotNull(jwtDecoder);
        assertInstanceOf(NimbusJwtDecoder.class, jwtDecoder);
    }

    @Test
    void deveConverterPerfilJwtEmAutoridade() {

        JwtAuthenticationConverter converter =
                securityConfig.jwtAuthenticationConverter();

        Jwt jwt = Jwt.withTokenValue("token-teste")
                .header("alg", "HS256")
                .claim("sub", "cliente@email.com")
                .claim("perfil", "CLIENTE")
                .build();

        AbstractAuthenticationToken authentication =
                converter.convert(jwt);

        assertNotNull(authentication);
        assertEquals("cliente@email.com", authentication.getName());

        assertTrue(authentication.getAuthorities().stream()
        .anyMatch(authority  ->
                Objects.equals(authority .getAuthority(), "ROLE_CLIENTE")));
    }

    @Test
    void deveObterAuthenticationManager() throws Exception {

        AuthenticationManager authenticationManagerEsperado =
                mock(AuthenticationManager.class);

        AuthenticationConfiguration configuration =
                mock(AuthenticationConfiguration.class);

        when(configuration.getAuthenticationManager())
                .thenReturn(authenticationManagerEsperado);

        AuthenticationManager resultado =
                securityConfig.authenticationManager(configuration);

        assertNotNull(resultado);
        assertSame(authenticationManagerEsperado, resultado);

        verify(configuration).getAuthenticationManager();

    }
}
