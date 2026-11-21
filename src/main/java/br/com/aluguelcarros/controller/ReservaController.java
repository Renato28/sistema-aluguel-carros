package br.com.aluguelcarros.controller;

import br.com.aluguelcarros.dto.ReservaAtualizacaoRequest;
import br.com.aluguelcarros.dto.ReservaRequest;
import br.com.aluguelcarros.dto.ReservaResponse;
import br.com.aluguelcarros.service.ReservaService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservas")
@SecurityRequirement(name = "bearerAuth")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'CLIENTE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservaResponse cadastrar(@Valid @RequestBody ReservaRequest request) {

        return reservaService.cadastrar(
                request.getClienteId(),
                request.getVeiculoId(),
                request.getDataRetirada(),
                request.getDataDevolucao());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    public List<ReservaResponse> listarTodas() {
        return reservaService.listarTodas();
    }

    @GetMapping("/minhas")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<List<ReservaResponse>> listarMinhasReservas() {
        return ResponseEntity.ok(reservaService.listarMinhasReservas());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'CLIENTE')")
    public ReservaResponse buscarPorId(@PathVariable Long id) {
        return reservaService.buscarPorId(id);
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'CLIENTE')")
    public ReservaResponse cancelar(@PathVariable Long id) {
        return reservaService.cancelar(id);
    }

    @PatchMapping("/{id}/confirmar")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    public ReservaResponse confirmar(@PathVariable Long id) {
        return reservaService.confirmar(id);
    }

    @PutMapping("/{id}/periodo")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'CLIENTE')")
    public ReservaResponse atualizarPeriodo(
            @PathVariable Long id,
            @Valid @RequestBody ReservaAtualizacaoRequest request) {

        return reservaService.atualizarPeriodo(
                id,
                request.getDataRetirada(),
                request.getDataDevolucao()
        );
    }
}
