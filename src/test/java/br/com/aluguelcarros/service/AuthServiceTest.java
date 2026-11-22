package br.com.aluguelcarros.service;

import br.com.aluguelcarros.dto.CadastroClienteRequest;
import br.com.aluguelcarros.dto.LoginRequest;
import br.com.aluguelcarros.dto.LoginResponse;
import br.com.aluguelcarros.entity.Cliente;
import br.com.aluguelcarros.entity.PerfilUsuario;
import br.com.aluguelcarros.entity.Usuario;
import br.com.aluguelcarros.exception.CpfClienteDuplicadoException;
import br.com.aluguelcarros.exception.EmailUsuarioDuplicadoException;
import br.com.aluguelcarros.repository.ClienteRepository;
import br.com.aluguelcarros.repository.UsuarioRepository;
import br.com.aluguelcarros.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                authenticationManager,
                usuarioRepository,
                clienteRepository,
                encoder,
                jwtService,
                3600000L
        );
    }

    @Test
    void deveCadastrarClienteComSucesso() {

        CadastroClienteRequest request = new CadastroClienteRequest();
        request.setNome("João Silva");
        request.setCpf("12345678901");
        request.setEmail("joao@email.com");
        request.setSenha("senha123");
        request.setTelefone("83999999999");
        request.setDataNascimento(LocalDate.of(1990, 5, 10));

        Cliente clienteSalvo = new Cliente();
        clienteSalvo.setId(1L);

        when(clienteRepository.existsByCpf(request.getCpf()))
                .thenReturn(false);

        when(usuarioRepository.existsByEmail(request.getEmail()))
            .thenReturn(false);

        when(clienteRepository.save(any(Cliente.class)))
            .thenReturn(clienteSalvo);

        when(encoder.encode("senha123"))
                .thenReturn("senha-criptografada");

        authService.cadastrarCliente(request);

        verify(clienteRepository).existsByCpf("12345678901");
        verify(usuarioRepository).existsByEmail("joao@email.com");
        ArgumentCaptor<Cliente> clienteCaptor =
                ArgumentCaptor.forClass(Cliente.class);

        verify(clienteRepository).save(clienteCaptor.capture());

        Cliente clienteSalvoNoBanco = clienteCaptor.getValue();

        assertEquals("João Silva", clienteSalvoNoBanco.getNome());
        assertEquals("12345678901", clienteSalvoNoBanco.getCpf());
        assertEquals("joao@email.com", clienteSalvoNoBanco.getEmail());
        assertEquals("83999999999", clienteSalvoNoBanco.getTelefone());
        assertEquals(
                LocalDate.of(1990, 5, 10),
                clienteSalvoNoBanco.getDataNascimento()
        );

        ArgumentCaptor<Usuario> usuarioCaptor =
                ArgumentCaptor.forClass(Usuario.class);

        verify(usuarioRepository).save(usuarioCaptor.capture());

        Usuario usuarioSalvo = usuarioCaptor.getValue();

        assertEquals("João Silva", usuarioSalvo.getNome());
        assertEquals("joao@email.com", usuarioSalvo.getEmail());
        assertEquals("senha-criptografada", usuarioSalvo.getSenha());
        assertEquals(PerfilUsuario.CLIENTE, usuarioSalvo.getPerfil());
        assertTrue(usuarioSalvo.isAtivo());
        assertSame(clienteSalvo, usuarioSalvo.getCliente());
    }

    @Test
    void naoDeveCadastrarClienteComCpfDuplicado() {

        CadastroClienteRequest request = new CadastroClienteRequest();
        request.setNome("João Silva");
        request.setCpf("12345678901");
        request.setEmail("joao@email.com");
        request.setSenha("senha123");

        when(clienteRepository.existsByCpf(request.getCpf()))
            .thenReturn(true);

        assertThrows(
                CpfClienteDuplicadoException.class,
                () -> authService.cadastrarCliente(request)
        );

        verify(clienteRepository).existsByCpf("12345678901");

        verify(clienteRepository, never()).save(any(Cliente.class));
        verify(usuarioRepository, never()).existsByEmail(anyString());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void naoDeveCadastrarClienteComEmailDuplicado() {

        CadastroClienteRequest request = new CadastroClienteRequest();
        request.setNome("João Silva");
        request.setCpf("12345678901");
        request.setEmail("joao@email.com");
        request.setSenha("senha123");

        when(clienteRepository.existsByCpf(request.getCpf()))
                .thenReturn(false);

        when(usuarioRepository.existsByEmail(request.getEmail()))
                .thenReturn(true);

        assertThrows(
                EmailUsuarioDuplicadoException.class,
                () -> authService.cadastrarCliente(request)
        );

        verify(clienteRepository).existsByCpf("12345678901");
        verify(usuarioRepository).existsByEmail("joao@email.com");
        verify(clienteRepository, never()).save(any(Cliente.class));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void deveRealizarLoginComSucesso() {

        LoginRequest request = new LoginRequest(
                "joao@email.com",
                "senha123"
        );

        Usuario usuario =  new Usuario();
        usuario.setEmail("joao@email.com");
        usuario.setPerfil(PerfilUsuario.CLIENTE);

        when(usuarioRepository.findByEmail(request.email()))
                .thenReturn(Optional.of(usuario));

        when(jwtService.gerarToken("joao@email.com", "CLIENTE"))
            .thenReturn("token-jwt");

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("token-jwt", response.token());
        assertEquals("Bearer", response.tipo());
        assertEquals(3600000L, response.expiresIn());

        ArgumentCaptor<UsernamePasswordAuthenticationToken> authenticationCaptor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);

        verify(authenticationManager).authenticate(authenticationCaptor.capture());

        UsernamePasswordAuthenticationToken authentication =
                authenticationCaptor.getValue();

        assertEquals("joao@email.com", authentication.getPrincipal());
        assertEquals("senha123", authentication.getCredentials());
        verify(usuarioRepository).findByEmail("joao@email.com");
        verify(jwtService).gerarToken("joao@email.com",  "CLIENTE");
    }

    @Test
    void naoDeveRealizarLoginComCredenciaisInvalidas() {
        LoginRequest request = new LoginRequest(
                "joao@email.com",
                "senhaerrada"
        );

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Credenciais inválidas"));

        assertThrows(
                BadCredentialsException.class,
                () -> authService.login(request)
        );

        verify(authenticationManager).authenticate(any());
        verify(usuarioRepository, never()).findByEmail(anyString());
        verify(jwtService, never()).gerarToken(anyString(), anyString());
    }

    @Test
    void naoDeveRealizarLoginQuandoUsuarioNaoForEncontrado() {
        LoginRequest request = new LoginRequest(
                "joao@email.com",
                "senha123"
        );

        when(usuarioRepository.findByEmail(request.email()))
                .thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> authService.login(request)
        );

        verify(authenticationManager).authenticate(any());
        verify(usuarioRepository).findByEmail("joao@email.com");
        verify(jwtService, never()).gerarToken(anyString(), anyString());
    }
}
