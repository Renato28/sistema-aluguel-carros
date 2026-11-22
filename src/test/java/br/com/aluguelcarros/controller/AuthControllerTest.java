package br.com.aluguelcarros.controller;

import br.com.aluguelcarros.dto.CadastroClienteRequest;
import br.com.aluguelcarros.dto.LoginRequest;
import br.com.aluguelcarros.dto.LoginResponse;
import br.com.aluguelcarros.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void deveRealizarLoginComSucesso() throws Exception {

        LoginResponse response = new LoginResponse(
                "token-jwt-teste",
                "Bearer",
                3600000L
        );

        when(authService.login(any(LoginRequest.class)))
            .thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "email": "cliente@email.com",
                            "senha": "12345678"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-jwt-teste"))
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600000L));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void deveRetornarBadRequestQuandoEmailEstiverAusente() throws Exception {

        mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "senha": "12345678"
                        }
                        """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    void deveRetornarBadRequestQuandoEmailForInvalido() throws Exception {

        mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                        {
                            "email": "email-invalido",
                            "senha": "12345678"
                        }
                      """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);

    }

    @Test
    void deveRetornarBadRequestQuandoSenhaEstiverAusente() throws Exception {

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "email": "cliente@email.com"
                            }
                            """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    void deveRetornarBadRequestQuandoEmailESenhaEstiveremAusentes() throws Exception {

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {}
                            """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    void deveCadastrarClienteComSucesso() throws Exception {

        mockMvc.perform(post("/api/auth/cadastro")
            .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "nome": "João da Silva",
                            "cpf": "12345678901",
                            "email": "joao@email.com",
                            "senha": "12345678",
                            "telefone": "83999999999",
                            "dataNascimento": "1995-05-10"
                        }
                        """))
                .andExpect(status().isCreated());

        verify(authService).cadastrarCliente(any(CadastroClienteRequest.class));
    }

    @Test
    void deveRetornarBadRequestQuandoNomeEstiverAusenteNoCadastro() throws Exception {

        mockMvc.perform(post("/api/auth/cadastro")
            .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "cpf": "12345678901",
                            "email": "joao@email.com",
                            "senha": "12345678",
                            "telefone": "83999999999",
                            "dataNascimento": "1995-05-10"
                        }
                        """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    void deveRetornarBadRequestQuandoCpfEstiverAusenteNoCadastro() throws Exception {

        mockMvc.perform(post("/api/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "nome": "João da Silva",
                                "email": "joao@email.com",
                                "senha": "12345678",
                                "telefone": "83999999999",
                                "dataNascimento": "1995-05-10"
                            }
                            """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    void deveRetornarBadRequestQuandoCpfTiverTamanhoInvalidoNoCadastro() throws Exception {

        mockMvc.perform(post("/api/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "nome": "João da Silva",
                                "cpf": "123456789",
                                "email": "joao@email.com",
                                "senha": "12345678",
                                "telefone": "83999999999",
                                "dataNascimento": "1995-05-10"
                            }
                            """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    void deveRetornarBadRequestQuandoEmailEstiverAusenteNoCadastro() throws Exception {

        mockMvc.perform(post("/api/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "nome": "João da Silva",
                                "cpf": "12345678901",
                                "senha": "12345678",
                                "telefone": "83999999999",
                                "dataNascimento": "1995-05-10"
                            }
                            """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    void deveRetornarBadRequestQuandoEmailForInvalidoNoCadastro() throws Exception {

        mockMvc.perform(post("/api/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "nome": "João da Silva",
                                "cpf": "12345678901",
                                "email": "email-invalido",
                                "senha": "12345678",
                                "telefone": "83999999999",
                                "dataNascimento": "1995-05-10"
                            }
                            """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    void deveRetornarBadRequestQuandoSenhaEstiverAusenteNoCadastro() throws Exception {

        mockMvc.perform(post("/api/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "nome": "João da Silva",
                                "cpf": "12345678901",
                                "email": "joao@email.com",
                                "telefone": "83999999999",
                                "dataNascimento": "1995-05-10"
                            }
                            """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    void deveRetornarBadRequestQuandoSenhaTiverMenosDeOitoCaracteresNoCadastro() throws Exception {

        mockMvc.perform(post("/api/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "nome": "João da Silva",
                                "cpf": "12345678901",
                                "email": "joao@email.com",
                                "senha": "1234567",
                                "telefone": "83999999999",
                                "dataNascimento": "1995-05-10"
                            }
                            """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    void deveRetornarBadRequestQuandoDataNascimentoForFuturaNoCadastro() throws Exception {

        mockMvc.perform(post("/api/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "nome": "João da Silva",
                                "cpf": "12345678901",
                                "email": "joao@email.com",
                                "senha": "12345678",
                                "telefone": "83999999999",
                                "dataNascimento": "2095-05-10"
                            }
                            """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    void deveRetornarBadRequestQuandoTelefoneUltrapassarVinteCaracteresNoCadastro() throws Exception {

        mockMvc.perform(post("/api/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "nome": "João da Silva",
                                "cpf": "12345678901",
                                "email": "joao@email.com",
                                "senha": "12345678",
                                "telefone": "123456789012345678901",
                                "dataNascimento": "1995-05-10"
                            }
                            """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    void deveCadastrarClienteSemTelefone() throws Exception {

        mockMvc.perform(post("/api/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "nome": "João da Silva",
                                "cpf": "12345678901",
                                "email": "joao@email.com",
                                "senha": "12345678",
                                "dataNascimento": "1995-05-10"
                            }
                            """))
                .andExpect(status().isCreated());

        verify(authService).cadastrarCliente(any(CadastroClienteRequest.class));
    }

    @Test
    void deveRetornarBadRequestQuandoNomeUltrapassarCentoECinquentaCaracteresNoCadastro() throws Exception {

        String nome = "A".repeat(151);

        mockMvc.perform(post("/api/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "nome": "%s",
                                "cpf": "12345678901",
                                "email": "joao@email.com",
                                "senha": "12345678",
                                "telefone": "83999999999",
                                "dataNascimento": "1995-05-10"
                            }
                            """.formatted(nome)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    void deveRetornarBadRequestQuandoEmailUltrapassarCentoECinquentaCaracteresNoCadastro() throws Exception {

        String email = "a".repeat(140) + "@email.com";

        mockMvc.perform(post("/api/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "nome": "João da Silva",
                                "cpf": "12345678901",
                                "email": "%s",
                                "senha": "12345678",
                                "telefone": "83999999999",
                                "dataNascimento": "1995-05-10"
                            }
                            """.formatted(email)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }
}
