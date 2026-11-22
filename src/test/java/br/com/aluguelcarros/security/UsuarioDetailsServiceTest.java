package br.com.aluguelcarros.security;

import br.com.aluguelcarros.entity.PerfilUsuario;
import br.com.aluguelcarros.entity.Usuario;
import br.com.aluguelcarros.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioDetailsServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioDetailsService usuarioDetailsService;

    @Test
    void deveCarregarUsuarioAtivoComSucesso() {

        Usuario usuario = new Usuario();
        usuario.setNome("Cliente teste");
        usuario.setEmail("cliente@email.com");
        usuario.setSenha("senha-criptografada");
        usuario.setPerfil(PerfilUsuario.CLIENTE);
        usuario.setAtivo(true);

        when(usuarioRepository.findByEmail("cliente@email.com"))
                .thenReturn(Optional.of(usuario));

        UserDetails resultado = usuarioDetailsService
                .loadUserByUsername("cliente@email.com");

        assertNotNull(resultado);
        assertEquals("cliente@email.com", resultado.getUsername());
        assertEquals("senha-criptografada", resultado.getPassword());
        assertTrue(resultado.isEnabled());
        assertTrue(resultado.getAuthorities().stream()
        .anyMatch(authority
                -> Objects.equals(authority.getAuthority(), "ROLE_CLIENTE")));

        verify(usuarioRepository).findByEmail("cliente@email.com");
    }

    @Test
    void deveCarregarUsuarioInativo() {

        Usuario usuario = new Usuario();
        usuario.setNome("Cliente inativo");
        usuario.setEmail("inativo@email.com");
        usuario.setSenha("senha-criptografada");
        usuario.setPerfil(PerfilUsuario.CLIENTE);
        usuario.setAtivo(false);

        when(usuarioRepository.findByEmail("inativo@email.com"))
                .thenReturn(Optional.of(usuario));

        UserDetails resultado = usuarioDetailsService
                .loadUserByUsername("inativo@email.com");

        assertNotNull(resultado);
        assertEquals("inativo@email.com",  resultado.getUsername());
        assertFalse(resultado.isEnabled());

        verify(usuarioRepository).findByEmail("inativo@email.com");

    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoForEncontrado() {

        when(usuarioRepository.findByEmail("inexistente@email.com"))
                .thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> usuarioDetailsService
                        .loadUserByUsername("inexistente@email.com")
        );

        assertEquals("Usuário não encontrado.", exception.getMessage());

        verify(usuarioRepository).findByEmail("inexistente@email.com");
    }
}
