package br.com.aluguelcarros.controller;

import br.com.aluguelcarros.dto.VeiculoRequest;
import br.com.aluguelcarros.dto.VeiculoResponse;
import br.com.aluguelcarros.service.VeiculoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/veiculos")
@Tag(
        name = "Veículos",
        description = "Endpoints para gerenciamento e consulta de veículos disponíveis para aluguel."
)
public class VeiculoController {

    private final VeiculoService veiculoService;

    public VeiculoController(VeiculoService veiculoService) {
        this.veiculoService = veiculoService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Cadastrar veículo",
            description = "Cadastra um novo veículo. Acesso permitido para ADMIN e ATENDENTE."
    )
    @SecurityRequirement(name = "bearerAuth")
    public VeiculoResponse cadastrar(@Valid @RequestBody VeiculoRequest request) {
        return veiculoService.cadastrar(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Atualizar veículo",
            description = "Atualiza os dados de um veículo existente. Acesso permitido para ADMIN e ATENDENTE."
    )
    @SecurityRequirement(name = "bearerAuth")
    public VeiculoResponse atualizar(@PathVariable Long id, @Valid @RequestBody VeiculoRequest request) {
        return veiculoService.atualizar(id, request);
    }

    @GetMapping
    @Operation(
            summary = "Listar veículos",
            description = "Retorna a lista de todos os veículos cadastrados. Acesso permitido para usuários autenticados."
    )
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public List<VeiculoResponse> listarTodos() {
        return veiculoService.listarTodos();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar veículo por ID",
            description = "Retorna os dados de um veículo específico. Acesso permitido para usuários autenticados."
    )
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public VeiculoResponse buscarPorId(@PathVariable Long id) {
        return veiculoService.buscarPorId(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Excluir veículo",
            description = "Exclui um veículo pelo ID. Acesso permitido para ADMIN e ATENDENTE."
    )
    @SecurityRequirement(name = "bearerAuth")
    public void excluir(@PathVariable Long id) {
        veiculoService.excluir(id);
    }
}
