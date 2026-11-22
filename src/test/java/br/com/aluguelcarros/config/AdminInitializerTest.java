package br.com.aluguelcarros.config;

import br.com.aluguelcarros.entity.PerfilUsuario;
import br.com.aluguelcarros.entity.Usuario;
import br.com.aluguelcarros.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminInitializerTest {

    private UsuarioRepository usuarioRepository;
    private PasswordEncoder passwordEncoder;
    private AdminInitializer adminInitializer;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);

        adminInitializer = new AdminInitializer();
    }

    @Test
    void naoDeveCriarAdministradorQuandoJaExistir() throws Exception {

        when(usuarioRepository.findByEmail("admin@teste.com"))
            .thenReturn(Optional.of(new Usuario()));

        CommandLineRunner runner = adminInitializer.criarAdministradorInicial(
                usuarioRepository,
                passwordEncoder,
                "Administrador",
                "admin@teste.com",
                "123456"
        );

        runner.run();

        verify(usuarioRepository).findByEmail("admin@teste.com");
        verify(usuarioRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void deveCriarAdministradorQuandoNaoExistir() throws Exception {

        when(usuarioRepository.findByEmail("admin@teste.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("123456"))
                .thenReturn("senha-criptografada");

        CommandLineRunner runner = adminInitializer.criarAdministradorInicial(
                usuarioRepository,
                passwordEncoder,
                "Administrador",
                "admin@teste.com",
                "123456"
        );

        runner.run();

        verify(usuarioRepository).findByEmail("admin@teste.com");
        verify(passwordEncoder).encode("123456");

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);

        verify(usuarioRepository).save(captor.capture());

        Usuario administrador = captor.getValue();

        assertEquals("Administrador", administrador.getNome());
        assertEquals("admin@teste.com", administrador.getEmail());
        assertEquals("senha-criptografada", administrador.getSenha());
        assertEquals(PerfilUsuario.ADMIN, administrador.getPerfil());
        assertTrue(administrador.isAtivo());
    }
}
