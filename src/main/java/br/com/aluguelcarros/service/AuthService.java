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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final PasswordEncoder encoder;
    private final JwtService  jwtService;
    private final long expiration;

    private static final String CLIENTE_JA_CADASTRADO_COM_ESTE_CPF = "Já existe um cliente cadastrado com esse CPF.";
    private static final String CLIENTE_JA_CADASTRADO_COM_ESTE_EMAIL = "Já existe um usuário cadastrado com esse email.";

    public AuthService(AuthenticationManager authenticationManager,
                       UsuarioRepository usuarioRepository,
                       ClienteRepository clienteRepository,
                       PasswordEncoder encoder,
                       JwtService jwtService,
                       @Value("${jwt.expiration}") long expiration) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.encoder = encoder;
        this.jwtService = jwtService;
        this.expiration = expiration;
    }

    public LoginResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.senha()
                )
        );

        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow();

        String token = jwtService.gerarToken(
                usuario.getEmail(),
                usuario.getPerfil().name()
        );

        return new LoginResponse(token, "Bearer", expiration);

    }

    @Transactional
    public void cadastrarCliente(CadastroClienteRequest request) {

        if (clienteRepository.existsByCpf(request.getCpf())) {
            throw new CpfClienteDuplicadoException(
                    CLIENTE_JA_CADASTRADO_COM_ESTE_CPF
            );
        }

        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new EmailUsuarioDuplicadoException(
                    CLIENTE_JA_CADASTRADO_COM_ESTE_EMAIL
            );
        }

        Cliente cliente = new Cliente();
        cliente.setNome(request.getNome());
        cliente.setCpf(request.getCpf());
        cliente.setEmail(request.getEmail());
        cliente.setTelefone(request.getTelefone());
        cliente.setDataNascimento(request.getDataNascimento());

        Cliente clienteSalvo = clienteRepository.save(cliente);

        Usuario usuario = new Usuario();
        usuario.setNome(request.getNome());
        usuario.setEmail(request.getEmail());
        usuario.setSenha(encoder.encode(request.getSenha()));
        usuario.setPerfil(PerfilUsuario.CLIENTE);
        usuario.setAtivo(true);
        usuario.setCliente(clienteSalvo);

        usuarioRepository.save(usuario);

    }
}
