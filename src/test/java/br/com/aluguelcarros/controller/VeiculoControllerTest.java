package br.com.aluguelcarros.controller;

import br.com.aluguelcarros.dto.VeiculoRequest;
import br.com.aluguelcarros.dto.VeiculoResponse;
import br.com.aluguelcarros.entity.StatusVeiculo;
import br.com.aluguelcarros.service.VeiculoService;
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

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VeiculoController.class)
@Import(VeiculoControllerTest.MethodSecurityTestConfig.class)
class VeiculoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VeiculoService veiculoService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void devePermitirQueAdminCadastreUmVeiculo() throws Exception {

        VeiculoResponse response = new VeiculoResponse();
        response.setId(1L);
        response.setCategoriaId(1L);
        response.setCategoriaNome("SUV");
        response.setMarca("Toyota");
        response.setModelo("Corolla Cross");
        response.setAnoFabricacao(2025);
        response.setPlaca("ABC1D23");
        response.setCor("Preto");
        response.setQuilometragem(1000);
        response.setValorDiaria(new BigDecimal("250.00"));
        response.setStatus(StatusVeiculo.DISPONIVEL);

        when(veiculoService.cadastrar(any(VeiculoRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/veiculos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "categoriaId": 1,
                            "marca": "Toyota",
                            "modelo": "Corolla Cross",
                            "anoFabricacao": 2025,
                            "placa": "ABC1D23",
                            "cor": "Preto",
                            "quilometragem": 1000,
                            "valorDiaria": 250.00
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.categoriaId").value(1))
                .andExpect(jsonPath("$.categoriaNome").value("SUV"))
                .andExpect(jsonPath("$.marca").value("Toyota"))
                .andExpect(jsonPath("$.modelo").value("Corolla Cross"))
                .andExpect(jsonPath("$.placa").value("ABC1D23"))
                .andExpect(jsonPath("$.cor").value("Preto"))
                .andExpect(jsonPath("$.quilometragem").value(1000))
                .andExpect(jsonPath("$.valorDiaria").value(250.00));

        verify(veiculoService).cadastrar(any(VeiculoRequest.class));

    }

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void devePermitirQueAtendenteCadastreUmVeiculo() throws Exception {

        VeiculoResponse response = new VeiculoResponse();
        response.setId(2L);
        response.setCategoriaId(1L);
        response.setCategoriaNome("SUV");
        response.setMarca("Honda");
        response.setModelo("HR-V");
        response.setAnoFabricacao(2024);
        response.setPlaca("DEF2G34");
        response.setCor("Branco");
        response.setQuilometragem(5000);
        response.setValorDiaria(new BigDecimal("220.00"));
        response.setStatus(StatusVeiculo.DISPONIVEL);

        when(veiculoService.cadastrar(any(VeiculoRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/veiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "categoriaId": 1,
                                "marca": "Honda",
                                "modelo": "HR-V",
                                "anoFabricacao": 2024,
                                "placa": "DEF2G34",
                                "cor": "Branco",
                                "quilometragem": 5000,
                                "valorDiaria": 220.00
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.marca").value("Honda"))
                .andExpect(jsonPath("$.modelo").value("HR-V"))
                .andExpect(jsonPath("$.status").value("DISPONIVEL"));

        verify(veiculoService).cadastrar(any(VeiculoRequest.class));
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void naoDevePermitirQueClienteCadastreUmVeiculo() throws Exception {

        mockMvc.perform(post("/api/veiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "categoriaId": 1,
                                "marca": "Toyota",
                                "modelo": "Corolla Cross",
                                "anoFabricacao": 2025,
                                "placa": "ABC1D23",
                                "cor": "Preto",
                                "quilometragem": 1000,
                                "valorDiaria": 250.00
                            }
                            """))
                .andExpect(status().isForbidden());

        verify(veiculoService, never())
                .cadastrar(any(VeiculoRequest.class));
    }

    @Test
    void naoDevePermitirCadastroDeVeiculoSemAutenticacao() throws Exception {

        mockMvc.perform(post("/api/veiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "categoriaId": 1,
                                "marca": "Toyota",
                                "modelo": "Corolla Cross",
                                "anoFabricacao": 2025,
                                "placa": "ABC1D23",
                                "cor": "Preto",
                                "quilometragem": 1000,
                                "valorDiaria": 250.00
                            }
                            """))
                .andExpect(status().isUnauthorized());

        verify(veiculoService, never())
                .cadastrar(any(VeiculoRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void devePermitirQueAdminAtualizeUmVeiculo() throws Exception {

        VeiculoResponse response = new VeiculoResponse();
        response.setId(1L);
        response.setCategoriaId(1L);
        response.setCategoriaNome("SUV");
        response.setMarca("Toyota");
        response.setModelo("Corolla Cross");
        response.setAnoFabricacao(2025);
        response.setPlaca("ABC1D23");
        response.setCor("Prata");
        response.setQuilometragem(2000);
        response.setValorDiaria(new BigDecimal("280.00"));
        response.setStatus(StatusVeiculo.DISPONIVEL);

        when(veiculoService.atualizar(eq(1L), any(VeiculoRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/veiculos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "categoriaId": 1,
                                "marca": "Toyota",
                                "modelo": "Corolla Cross",
                                "anoFabricacao": 2025,
                                "placa": "ABC1D23",
                                "cor": "Prata",
                                "quilometragem": 2000,
                                "valorDiaria": 280.00
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.marca").value("Toyota"))
                .andExpect(jsonPath("$.cor").value("Prata"))
                .andExpect(jsonPath("$.quilometragem").value(2000))
                .andExpect(jsonPath("$.valorDiaria").value(280.00));

        verify(veiculoService).atualizar(eq(1L), any(VeiculoRequest.class));
    }

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void devePermitirQueAtendenteAtualizeUmVeiculo() throws Exception {

        VeiculoResponse response = new VeiculoResponse();
        response.setId(1L);
        response.setCategoriaId(1L);
        response.setCategoriaNome("SUV");
        response.setMarca("Honda");
        response.setModelo("HR-V");
        response.setAnoFabricacao(2025);
        response.setPlaca("DEF2G34");
        response.setCor("Preto");
        response.setQuilometragem(3000);
        response.setValorDiaria(new BigDecimal("300.00"));
        response.setStatus(StatusVeiculo.DISPONIVEL);

        when(veiculoService.atualizar(eq(1L), any(VeiculoRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/veiculos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "categoriaId": 1,
                                "marca": "Honda",
                                "modelo": "HR-V",
                                "anoFabricacao": 2025,
                                "placa": "DEF2G34",
                                "cor": "Preto",
                                "quilometragem": 3000,
                                "valorDiaria": 300.00
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.marca").value("Honda"))
                .andExpect(jsonPath("$.modelo").value("HR-V"))
                .andExpect(jsonPath("$.cor").value("Preto"))
                .andExpect(jsonPath("$.valorDiaria").value(300.00));

        verify(veiculoService).atualizar(eq(1L), any(VeiculoRequest.class));
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void naoDevePermitirQueClienteAtualizeUmVeiculo() throws Exception {

        mockMvc.perform(put("/api/veiculos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "categoriaId": 1,
                                "marca": "Toyota",
                                "modelo": "Corolla Cross",
                                "anoFabricacao": 2025,
                                "placa": "ABC1D23",
                                "cor": "Prata",
                                "quilometragem": 2000,
                                "valorDiaria": 280.00
                            }
                            """))
                .andExpect(status().isForbidden());

        verify(veiculoService, never())
                .atualizar(anyLong(), any(VeiculoRequest.class));
    }

    @Test
    void naoDevePermitirAtualizacaoDeVeiculoSemAutenticacao() throws Exception {

        mockMvc.perform(put("/api/veiculos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "categoriaId": 1,
                                "marca": "Toyota",
                                "modelo": "Corolla Cross",
                                "anoFabricacao": 2025,
                                "placa": "ABC1D23",
                                "cor": "Prata",
                                "quilometragem": 2000,
                                "valorDiaria": 280.00
                            }
                            """))
                .andExpect(status().isUnauthorized());

        verify(veiculoService, never())
                .atualizar(anyLong(), any(VeiculoRequest.class));
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void devePermitirQueClienteListeTodosOsVeiculos() throws Exception {

        VeiculoResponse response = new VeiculoResponse();
        response.setId(1L);
        response.setCategoriaId(1L);
        response.setCategoriaNome("SUV");
        response.setMarca("Toyota");
        response.setModelo("Corolla Cross");
        response.setAnoFabricacao(2025);
        response.setPlaca("ABC1D23");
        response.setCor("Preto");
        response.setQuilometragem(1000);
        response.setValorDiaria(new BigDecimal("250.00"));
        response.setStatus(StatusVeiculo.DISPONIVEL);

        when(veiculoService.listarTodos()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/veiculos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].marca").value("Toyota"))
                .andExpect(jsonPath("$[0].modelo").value("Corolla Cross"))
                .andExpect(jsonPath("$[0].categoriaNome").value("SUV"))
                .andExpect(jsonPath("$[0].status").value("DISPONIVEL"));

        verify(veiculoService).listarTodos();
    }

    @Test
    void naoDevePermitirListagemDeVeiculosSemAutenticacao() throws Exception {

        mockMvc.perform(get("/api/veiculos"))
                .andExpect(status().isUnauthorized());

        verify(veiculoService, never()).listarTodos();
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void devePermitirQueClienteBusqueVeiculoPorId() throws Exception {

        VeiculoResponse response = new VeiculoResponse();
        response.setId(1L);
        response.setCategoriaId(1L);
        response.setCategoriaNome("SUV");
        response.setMarca("Toyota");
        response.setModelo("Corolla Cross");
        response.setAnoFabricacao(2025);
        response.setPlaca("ABC1D23");
        response.setCor("Preto");
        response.setQuilometragem(1000);
        response.setValorDiaria(new BigDecimal("250.00"));
        response.setStatus(StatusVeiculo.DISPONIVEL);

        when(veiculoService.buscarPorId(1L)).thenReturn(response);

        mockMvc.perform(get("/api/veiculos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.categoriaId").value(1))
                .andExpect(jsonPath("$.marca").value("Toyota"))
                .andExpect(jsonPath("$.modelo").value("Corolla Cross"))
                .andExpect(jsonPath("$.placa").value("ABC1D23"))
                .andExpect(jsonPath("$.status").value("DISPONIVEL"));

        verify(veiculoService).buscarPorId(1L);
    }

    @Test
    void naoDevePermitirBuscaDeVeiculoPorIdSemAutenticacao() throws Exception {

        mockMvc.perform(get("/api/veiculos/1"))
                .andExpect(status().isUnauthorized());

        verify(veiculoService, never()).buscarPorId(anyLong());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void devePermitirQueAdminExcluaUmVeiculo() throws Exception {

        mockMvc.perform(delete("/api/veiculos/1"))
                .andExpect(status().isNoContent());

        verify(veiculoService).excluir(1L);
    }

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void devePermitirQueAtendenteExcluaUmVeiculo() throws Exception {

        mockMvc.perform(delete("/api/veiculos/1"))
                .andExpect(status().isNoContent());

        verify(veiculoService).excluir(1L);
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void naoDevePermitirQueClienteExcluaUmVeiculo() throws Exception {

        mockMvc.perform(delete("/api/veiculos/1"))
                .andExpect(status().isForbidden());

        verify(veiculoService, never()).excluir(anyLong());
    }

    @Test
    void naoDevePermitirExclusaoDeVeiculoSemAutenticacao() throws Exception {

        mockMvc.perform(delete("/api/veiculos/1"))
                .andExpect(status().isUnauthorized());

        verify(veiculoService, never()).excluir(anyLong());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void naoDevePermitirCadastroDeVeiculoComMarcaVazia() throws Exception {

        mockMvc.perform(post("/api/veiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "categoriaId": 1,
                                "marca": "",
                                "modelo": "Corolla Cross",
                                "anoFabricacao": 2025,
                                "placa": "ABC1D23",
                                "cor": "Preto",
                                "quilometragem": 1000,
                                "valorDiaria": 250.00
                            }
                            """))
                .andExpect(status().isBadRequest());

        verify(veiculoService, never())
                .cadastrar(any(VeiculoRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void naoDevePermitirCadastroDeVeiculoComValorDiariaZero() throws Exception {

        mockMvc.perform(post("/api/veiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "categoriaId": 1,
                                "marca": "Toyota",
                                "modelo": "Corolla Cross",
                                "anoFabricacao": 2025,
                                "placa": "ABC1D23",
                                "cor": "Preto",
                                "quilometragem": 1000,
                                "valorDiaria": 0.00
                            }
                            """))
                .andExpect(status().isBadRequest());

        verify(veiculoService, never())
                .cadastrar(any(VeiculoRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void naoDevePermitirCadastroDeVeiculoComQuilometragemNegativa() throws Exception {

        mockMvc.perform(post("/api/veiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "categoriaId": 1,
                                "marca": "Toyota",
                                "modelo": "Corolla Cross",
                                "anoFabricacao": 2025,
                                "placa": "ABC1D23",
                                "cor": "Preto",
                                "quilometragem": -100,
                                "valorDiaria": 250.00
                            }
                            """))
                .andExpect(status().isBadRequest());

        verify(veiculoService, never())
                .cadastrar(any(VeiculoRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void naoDevePermitirCadastroDeVeiculoComAnoFabricacaoInvalido() throws Exception {

        mockMvc.perform(post("/api/veiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "categoriaId": 1,
                                "marca": "Toyota",
                                "modelo": "Corolla Cross",
                                "anoFabricacao": 1899,
                                "placa": "ABC1D23",
                                "cor": "Preto",
                                "quilometragem": 1000,
                                "valorDiaria": 250.00
                            }
                            """))
                .andExpect(status().isBadRequest());

        verify(veiculoService, never())
                .cadastrar(any(VeiculoRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void naoDevePermitirCadastroDeVeiculoSemCategoria() throws Exception {

        mockMvc.perform(post("/api/veiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "marca": "Toyota",
                                "modelo": "Corolla Cross",
                                "anoFabricacao": 2025,
                                "placa": "ABC1D23",
                                "cor": "Preto",
                                "quilometragem": 1000,
                                "valorDiaria": 250.00
                            }
                            """))
                .andExpect(status().isBadRequest());

        verify(veiculoService, never())
                .cadastrar(any(VeiculoRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void naoDevePermitirCadastroDeVeiculoSemPlaca() throws Exception {

        mockMvc.perform(post("/api/veiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "categoriaId": 1,
                                "marca": "Toyota",
                                "modelo": "Corolla Cross",
                                "anoFabricacao": 2025,
                                "cor": "Preto",
                                "quilometragem": 1000,
                                "valorDiaria": 250.00
                            }
                            """))
                .andExpect(status().isBadRequest());

        verify(veiculoService, never())
                .cadastrar(any(VeiculoRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void naoDevePermitirCadastroDeVeiculoComMarcaMaiorQue100Caracteres() throws Exception {

        String marcaInvalida = "A".repeat(101);

        mockMvc.perform(post("/api/veiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "categoriaId": 1,
                                "marca": "%s",
                                "modelo": "Corolla Cross",
                                "anoFabricacao": 2025,
                                "placa": "ABC1D23",
                                "cor": "Preto",
                                "quilometragem": 1000,
                                "valorDiaria": 250.00
                            }
                            """.formatted(marcaInvalida)))
                .andExpect(status().isBadRequest());

        verify(veiculoService, never())
                .cadastrar(any(VeiculoRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void naoDevePermitirCadastroDeVeiculoComPlacaMaiorQue10Caracteres() throws Exception {

        String placaInvalida = "A".repeat(11);

        mockMvc.perform(post("/api/veiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "categoriaId": 1,
                                "marca": "Toyota",
                                "modelo": "Corolla Cross",
                                "anoFabricacao": 2025,
                                "placa": "%s",
                                "cor": "Preto",
                                "quilometragem": 1000,
                                "valorDiaria": 250.00
                            }
                            """.formatted(placaInvalida)))
                .andExpect(status().isBadRequest());

        verify(veiculoService, never())
                .cadastrar(any(VeiculoRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void naoDevePermitirCadastroDeVeiculoComModeloMaiorQue100Caracteres() throws Exception {

        String modeloInvalido = "A".repeat(101);

        mockMvc.perform(post("/api/veiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "categoriaId": 1,
                                "marca": "Toyota",
                                "modelo": "%s",
                                "anoFabricacao": 2025,
                                "placa": "ABC1D23",
                                "cor": "Preto",
                                "quilometragem": 1000,
                                "valorDiaria": 250.00
                            }
                            """.formatted(modeloInvalido)))
                .andExpect(status().isBadRequest());

        verify(veiculoService, never())
                .cadastrar(any(VeiculoRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void naoDevePermitirCadastroDeVeiculoComCorMaiorQue50Caracteres() throws Exception {

        String corInvalida = "A".repeat(51);

        mockMvc.perform(post("/api/veiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "categoriaId": 1,
                                "marca": "Toyota",
                                "modelo": "Corolla Cross",
                                "anoFabricacao": 2025,
                                "placa": "ABC1D23",
                                "cor": "%s",
                                "quilometragem": 1000,
                                "valorDiaria": 250.00
                            }
                            """.formatted(corInvalida)))
                .andExpect(status().isBadRequest());

        verify(veiculoService, never())
                .cadastrar(any(VeiculoRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void naoDevePermitirAtualizacaoDeVeiculoComMarcaVazia() throws Exception {

        mockMvc.perform(put("/api/veiculos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "categoriaId": 1,
                                "marca": "",
                                "modelo": "Corolla Cross",
                                "anoFabricacao": 2025,
                                "placa": "ABC1D23",
                                "cor": "Preto",
                                "quilometragem": 1000,
                                "valorDiaria": 250.00
                            }
                            """))
                .andExpect(status().isBadRequest());

        verify(veiculoService, never())
                .atualizar(anyLong(), any(VeiculoRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void naoDevePermitirAtualizacaoDeVeiculoComValorDiariaZero() throws Exception {

        mockMvc.perform(put("/api/veiculos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "categoriaId": 1,
                                "marca": "Toyota",
                                "modelo": "Corolla Cross",
                                "anoFabricacao": 2025,
                                "placa": "ABC1D23",
                                "cor": "Preto",
                                "quilometragem": 1000,
                                "valorDiaria": 0.00
                            }
                            """))
                .andExpect(status().isBadRequest());

        verify(veiculoService, never())
                .atualizar(anyLong(), any(VeiculoRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void naoDevePermitirAtualizacaoDeVeiculoComQuilometragemNegativa() throws Exception {

        mockMvc.perform(put("/api/veiculos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "categoriaId": 1,
                                "marca": "Toyota",
                                "modelo": "Corolla Cross",
                                "anoFabricacao": 2025,
                                "placa": "ABC1D23",
                                "cor": "Preto",
                                "quilometragem": -100,
                                "valorDiaria": 250.00
                            }
                            """))
                .andExpect(status().isBadRequest());

        verify(veiculoService, never())
                .atualizar(anyLong(), any(VeiculoRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void naoDevePermitirAtualizacaoDeVeiculoComAnoFabricacaoInvalido() throws Exception {

        mockMvc.perform(put("/api/veiculos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "categoriaId": 1,
                                "marca": "Toyota",
                                "modelo": "Corolla Cross",
                                "anoFabricacao": 1899,
                                "placa": "ABC1D23",
                                "cor": "Preto",
                                "quilometragem": 1000,
                                "valorDiaria": 250.00
                            }
                            """))
                .andExpect(status().isBadRequest());

        verify(veiculoService, never())
                .atualizar(anyLong(), any(VeiculoRequest.class));
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    void naoDevePermitirAtualizacaoDeVeiculoSemCategoria() throws Exception {

        mockMvc.perform(put("/api/veiculos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "marca": "Toyota",
                                "modelo": "Corolla Cross",
                                "anoFabricacao": 2025,
                                "placa": "ABC1D23",
                                "cor": "Preto",
                                "quilometragem": 1000,
                                "valorDiaria": 250.00
                            }
                            """))
                .andExpect(status().isBadRequest());

        verify(veiculoService, never())
                .atualizar(anyLong(), any(VeiculoRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void naoDevePermitirAtualizacaoDeVeiculoSemPlaca() throws Exception {

        mockMvc.perform(put("/api/veiculos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "categoriaId": 1,
                                "marca": "Toyota",
                                "modelo": "Corolla Cross",
                                "anoFabricacao": 2025,
                                "cor": "Preto",
                                "quilometragem": 1000,
                                "valorDiaria": 250.00
                            }
                            """))
                .andExpect(status().isBadRequest());

        verify(veiculoService, never())
                .atualizar(anyLong(), any(VeiculoRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void naoDevePermitirAtualizacaoDeVeiculoComMarcaMaiorQue100Caracteres() throws Exception {

        String marcaInvalida = "A".repeat(101);

        mockMvc.perform(put("/api/veiculos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "categoriaId": 1,
                                "marca": "%s",
                                "modelo": "Corolla Cross",
                                "anoFabricacao": 2025,
                                "placa": "ABC1D23",
                                "cor": "Preto",
                                "quilometragem": 1000,
                                "valorDiaria": 250.00
                            }
                            """.formatted(marcaInvalida)))
                .andExpect(status().isBadRequest());

        verify(veiculoService, never())
                .atualizar(anyLong(), any(VeiculoRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void naoDevePermitirAtualizacaoDeVeiculoComPlacaMaiorQue10Caracteres() throws Exception {

        mockMvc.perform(put("/api/veiculos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "categoriaId": 1,
                                "marca": "Toyota",
                                "modelo": "Corolla Cross",
                                "anoFabricacao": 2025,
                                "placa": "ABC12345678",
                                "cor": "Preto",
                                "quilometragem": 1000,
                                "valorDiaria": 250.00
                            }
                            """))
                .andExpect(status().isBadRequest());

        verify(veiculoService, never())
                .atualizar(anyLong(), any(VeiculoRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void naoDevePermitirAtualizacaoDeVeiculoComModeloMaiorQue100Caracteres() throws Exception {

        String modeloInvalido = "A".repeat(101);

        mockMvc.perform(put("/api/veiculos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "categoriaId": 1,
                                "marca": "Toyota",
                                "modelo": "%s",
                                "anoFabricacao": 2025,
                                "placa": "ABC1D23",
                                "cor": "Preto",
                                "quilometragem": 1000,
                                "valorDiaria": 250.00
                            }
                            """.formatted(modeloInvalido)))
                .andExpect(status().isBadRequest());

        verify(veiculoService, never())
                .atualizar(anyLong(), any(VeiculoRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void naoDevePermitirAtualizacaoDeVeiculoComCorMaiorQue50Caracteres() throws Exception {

        String corInvalida = "A".repeat(51);

        mockMvc.perform(put("/api/veiculos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "categoriaId": 1,
                                "marca": "Toyota",
                                "modelo": "Corolla Cross",
                                "anoFabricacao": 2025,
                                "placa": "ABC1D23",
                                "cor": "%s",
                                "quilometragem": 1000,
                                "valorDiaria": 250.00
                            }
                            """.formatted(corInvalida)))
                .andExpect(status().isBadRequest());

        verify(veiculoService, never())
                .atualizar(anyLong(), any(VeiculoRequest.class));
    }


    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityTestConfig {

    }
}
