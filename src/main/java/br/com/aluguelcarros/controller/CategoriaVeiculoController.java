package br.com.aluguelcarros.controller;

import br.com.aluguelcarros.dto.CategoriaVeiculoRequest;
import br.com.aluguelcarros.dto.CategoriaVeiculoResponse;
import br.com.aluguelcarros.service.CategoriaVeiculoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@Tag(
        name = "Categorias de Veículos",
        description = "Endpoints para consulta e cadastro de categorias de veículos."
)
public class CategoriaVeiculoController {

    private final CategoriaVeiculoService categoriaVeiculoService;

    public CategoriaVeiculoController(CategoriaVeiculoService categoriaVeiculoService) {
        this.categoriaVeiculoService = categoriaVeiculoService;
    }

    @GetMapping
    @Operation(
            summary = "Listar categorias",
            description = "Retorna todas as categorias de veículos cadastradas. Acesso permitido para usuários autenticados."
    )
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public List<CategoriaVeiculoResponse> listarTodas() {
        return this.categoriaVeiculoService.listarTodas();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @PostMapping
    @Operation(
            summary = "Cadastrar categoria",
            description = "Cadastra uma nova categoria de veículo. Acesso permitido para ADMIN e ATENDENTE."
    )
    @SecurityRequirement(name = "bearerAuth")
    public CategoriaVeiculoResponse cadastrar(@Valid @RequestBody CategoriaVeiculoRequest request) {
        return this.categoriaVeiculoService.cadastrar(request);
    }
}
