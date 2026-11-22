package br.com.aluguelcarros.controller;

import br.com.aluguelcarros.service.ReservaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservaController.class)
@Import(ReservaControllerTest.MethodSecurityTestConfig.class)
class ReservaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservaService reservaService;

    @Test
    @WithMockUser(roles = "CLIENTE")
    void naoDevePermitirQueClienteListeTodasAsReservas() throws Exception {

        mockMvc.perform(get("/api/reservas"))
                .andExpect(status().isForbidden());
    }
    @Test
    @WithMockUser(roles = "CLIENTE")
    void naoDevePermitirQueClienteConfirmeReserva() throws Exception {

        mockMvc.perform(patch("/api/reservas/1/confirmar"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void devePermitirQueAdminConfirmeReserva() throws Exception {

        mockMvc.perform(patch("/api/reservas/1/confirmar"))
                .andExpect(status().isOk());

        verify(reservaService).confirmar(1L);
    }

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void devePermitirQueAtendenteConfirmeReserva() throws Exception {

        mockMvc.perform(patch("/api/reservas/1/confirmar"))
                .andExpect(status().isOk());

        verify(reservaService).confirmar(1L);
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void devePermitirQueClienteListeSuasPropriasReservas() throws Exception {

        when(reservaService.listarMinhasReservas())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/reservas/minhas"))
                .andExpect(status().isOk());

        verify(reservaService).listarMinhasReservas();
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void devePermitirQueClienteBusqueReservaPorId() throws Exception {

        mockMvc.perform(get("/api/reservas/1"))
                .andExpect(status().isOk());

        verify(reservaService).buscarPorId(1L);
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void devePermitirQueClienteCanceleReserva() throws Exception {

        mockMvc.perform(patch("/api/reservas/1/cancelar"))
                .andExpect(status().isOk());

        verify(reservaService).cancelar(1L);
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void devePermitirQueClienteAtualizePeriodoDaReserva() throws Exception {

        String json = """
                {
                    "dataRetirada": "2026-12-10",
                    "dataDevolucao": "2026-12-15"
                }
                """;

        mockMvc.perform(put("/api/reservas/1/periodo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());

        verify(reservaService).atualizarPeriodo(
                1L,
                LocalDate.of(2026,12,10),
                LocalDate.of(2026,12,15)
        );
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void devePermitirQueClienteCadastreReserva() throws Exception {

        String json = """
                {
                    "clienteId": 1,
                    "veiculoId": 2,
                    "dataRetirada": "2026-12-10",
                    "dataDevolucao": "2026-12-15"
                }
                """;

        mockMvc.perform(post("/api/reservas")
            .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated());

        verify(reservaService).cadastrar(
                1L,
                2L,
                LocalDate.of(2026, 12, 10),
                LocalDate.of(2026, 12, 15));
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void deveRetornarBadRequestQuandoDadosObrigatoriosEstiveremAusentes() throws Exception {
        String json = """
            {
                "clienteId": null,
                "veiculoId": 2,
                "dataRetirada": "2026-12-10",
                "dataDevolucao": "2026-12-15"
            }
            """;

        mockMvc.perform(post("/api/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(reservaService, never()).cadastrar(
                any(),
                any(),
                any(),
                any()
        );
    }

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void devePermitirQueAtendenteCadastreReservaParaUmCliente() throws Exception {
        String json = """
            {
                "clienteId": 3,
                "veiculoId": 2,
                "dataRetirada": "2026-12-10",
                "dataDevolucao": "2026-12-15"
            }
            """;

        mockMvc.perform(post("/api/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());

        verify(reservaService).cadastrar(
                3L,
                2L,
                LocalDate.of(2026, 12, 10),
                LocalDate.of(2026, 12, 15)
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void devePermitirQueAdminCadastreReservaParaUmCliente() throws Exception {
        String json = """
            {
                "clienteId": 3,
                "veiculoId": 2,
                "dataRetirada": "2026-12-10",
                "dataDevolucao": "2026-12-15"
            }
            """;

        mockMvc.perform(post("/api/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());

        verify(reservaService).cadastrar(
                3L,
                2L,
                LocalDate.of(2026, 12, 10),
                LocalDate.of(2026, 12, 15)
        );
    }

    @Test
    @WithMockUser(roles = "OUTRO")
    void naoDevePermitirQueUsuarioSemPermissaoCadastreReserva() throws Exception {
        String json = """
            {
                "clienteId": 3,
                "veiculoId": 2,
                "dataRetirada": "2026-12-10",
                "dataDevolucao": "2026-12-15"
            }
            """;

        mockMvc.perform(post("/api/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());

        verify(reservaService, never()).cadastrar(
                any(),
                any(),
                any(),
                any()
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void devePermitirQueAdminListeTodasAsReservas() throws Exception {

        when(reservaService.listarTodas())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/reservas"))
                .andExpect(status().isOk());

        verify(reservaService).listarTodas();
    }

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void devePermitirQueAtendenteListeTodasAsReservas() throws Exception {

        when(reservaService.listarTodas())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/reservas"))
                .andExpect(status().isOk());

        verify(reservaService).listarTodas();
    }

    @Test
    void naoDevePermitirAcessoAListagemSemAutenticacao() throws Exception {

        mockMvc.perform(get("/api/reservas"))
                .andExpect(status().isUnauthorized());

        verify(reservaService, never()).listarTodas();
    }

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void naoDevePermitirQueAtendenteListeReservasDoCliente() throws Exception {

        mockMvc.perform(get("/api/reservas/minhas"))
                .andExpect(status().isForbidden());

        verify(reservaService, never()).listarMinhasReservas();
    }

    @Test
    @WithMockUser(roles = "OUTRO")
    void naoDevePermitirQueUsuarioSemPermissaoBusqueReservaPorId() throws Exception {

        mockMvc.perform(get("/api/reservas/1"))
                .andExpect(status().isForbidden());

        verify(reservaService, never()).buscarPorId(1L);
    }

    @Test
    @WithMockUser(roles = "OUTRO")
    void naoDevePermitirQueUsuarioSemPermissaoCanceleReserva() throws Exception {

        mockMvc.perform(patch("/api/reservas/1/cancelar"))
                .andExpect(status().isForbidden());

        verify(reservaService, never()).cancelar(1L);
    }

    @Test
    @WithMockUser(roles = "OUTRO")
    void naoDevePermitirQueUsuarioSemPermissaoAtualizePeriodo() throws Exception {

        String json = """
            {
                "dataRetirada": "2026-12-10",
                "dataDevolucao": "2026-12-15"
            }
            """;

        mockMvc.perform(put("/api/reservas/1/periodo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());

        verify(reservaService, never()).atualizarPeriodo(
                any(),
                any(),
                any()
        );
    }

    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityTestConfig {

    }
}
