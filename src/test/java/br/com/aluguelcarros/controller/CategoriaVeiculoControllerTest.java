package br.com.aluguelcarros.controller;

import br.com.aluguelcarros.dto.CategoriaVeiculoRequest;
import br.com.aluguelcarros.dto.CategoriaVeiculoResponse;
import br.com.aluguelcarros.service.CategoriaVeiculoService;
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

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoriaVeiculoController.class)
@Import({CategoriaVeiculoControllerTest.MethodSecurityTestConfig.class})
class CategoriaVeiculoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoriaVeiculoService categoriaVeiculoService;

    @Test
    @WithMockUser(roles = "CLIENTE")
    void devePermitirQueClienteListeTodasAsCategorias() throws Exception {

        when(categoriaVeiculoService.listarTodas())
            .thenReturn(List.of());

        mockMvc.perform(get("/api/categorias"))
                .andExpect(status().isOk());

        verify(categoriaVeiculoService).listarTodas();
    }

    @Test
    void naoDevePermitirAcessoAListagemSemAutenticacao() throws Exception {

        mockMvc.perform(get("/api/categorias"))
                .andExpect(status().isUnauthorized());

        verify(categoriaVeiculoService, never()).listarTodas();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void devePermitirQueAdminCadastreUmaCategoria() throws Exception {

        CategoriaVeiculoResponse response = new CategoriaVeiculoResponse();
        response.setId(1L);
        response.setNome("SUV");
        response.setDescricao("Veículos utilitários esportivos");
        response.setAtiva(false);

        when(categoriaVeiculoService.cadastrar(any(CategoriaVeiculoRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "nome": "SUV",
                                "descricao": "Veículos utilitários esportivos"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("SUV"))
                .andExpect(jsonPath("$.descricao").value("Veículos utilitários esportivos"));

        verify(categoriaVeiculoService).cadastrar(any(CategoriaVeiculoRequest.class));
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void naoDevePermitirQueClienteCadastreUmaCategoria() throws Exception {

        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "nome": "SUV",
                                "descricao": "Veículos utilitários esportivos"
                            }
                            """))
                .andExpect(status().isForbidden());

        verify(categoriaVeiculoService, never())
                .cadastrar(any(CategoriaVeiculoRequest.class));
    }

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void devePermitirQueAtendenteCadastreUmaCategoria() throws Exception {

        CategoriaVeiculoResponse response = new CategoriaVeiculoResponse();
        response.setId(2L);
        response.setNome("Sedan");
        response.setDescricao("Veículos sedan");
        response.setAtiva(false);

        when(categoriaVeiculoService.cadastrar(any(CategoriaVeiculoRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "nome": "Sedan",
                                "descricao": "Veículos sedan"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.nome").value("Sedan"))
                .andExpect(jsonPath("$.descricao").value("Veículos sedan"));

        verify(categoriaVeiculoService).cadastrar(any(CategoriaVeiculoRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void naoDevePermitirCadastroDeCategoriaSemNome() throws Exception {

        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "nome": "",
                                "descricao": "Veículos utilitários esportivos"
                            }
                            """))
                .andExpect(status().isBadRequest());

        verify(categoriaVeiculoService, never())
                .cadastrar(any(CategoriaVeiculoRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void naoDevePermitirCadastroComDescricaoMaiorQue255Caracteres() throws Exception {

        String descricao = "A".repeat(256);

        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "nome": "SUV",
                                "descricao": "%s"
                            }
                            """.formatted(descricao)))
                .andExpect(status().isBadRequest());

        verify(categoriaVeiculoService, never())
                .cadastrar(any(CategoriaVeiculoRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void devePermitirCadastroDeCategoriaSemDescricao() throws Exception {

        CategoriaVeiculoResponse response = new CategoriaVeiculoResponse();
        response.setId(3L);
        response.setNome("Hatch");
        response.setDescricao(null);
        response.setAtiva(false);

        when(categoriaVeiculoService.cadastrar(any(CategoriaVeiculoRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "nome": "Hatch"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.nome").value("Hatch"))
                .andExpect(jsonPath("$.descricao").doesNotExist());

        verify(categoriaVeiculoService).cadastrar(any(CategoriaVeiculoRequest.class));
    }

    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityTestConfig {

    }
}
