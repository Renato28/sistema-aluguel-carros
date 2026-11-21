package br.com.aluguelcarros.controller;

import br.com.aluguelcarros.dto.CadastroClienteRequest;
import br.com.aluguelcarros.dto.LoginRequest;
import br.com.aluguelcarros.dto.LoginResponse;
import br.com.aluguelcarros.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(
        name = "Autenticação",
        description = "Endpoints para login e cadastro de clientes."
)
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(
            summary = "Realizar login",
            description = "Autentica um usuário e retorna um token JWT."
    )
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Cadastrar cliente",
            description = "Realiza o cadastro de um novo cliente na aplicação."
    )
    public void cadastrarCliente(@Valid @RequestBody CadastroClienteRequest request) {
        authService.cadastrarCliente(request);
    }
}
