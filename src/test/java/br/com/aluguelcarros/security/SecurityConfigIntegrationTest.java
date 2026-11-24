package br.com.aluguelcarros.security;

import br.com.aluguelcarros.config.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import({
        TestcontainersConfiguration.class,
        SecurityConfigIntegrationTest.TestController.class
})
@TestPropertySource(
        properties = "jwt.secret=bWluaGEtY2hhdmUtc2VjcmV0YS1wYXJhLXRlc3Rlcw=="
)
class SecurityConfigIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @RestController
    static class TestController {

        @GetMapping("/api/auth/teste")
        String rotaPublica() {
            return "publico";
        }

        @GetMapping("/api/teste")
        String rotaProtegida() {
            return "protegido";
        }
    }

    @Test
    void devePermitirAcessoSemAutenticacaoEmRotaPublica() throws Exception {
        mockMvc.perform(get("/api/auth/teste"))
                .andExpect(status().isOk());
    }

    @Test
    void deveBloquearAcessoSemAutenticacaoEmRotaProtegida() throws Exception {
        mockMvc.perform(get("/api/teste"))
                .andExpect(status().isUnauthorized());
    }
}
