package br.com.aluguelcarros;

import br.com.aluguelcarros.config.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class SistemaAluguelCarrosApplicationTests {

    @Test
    void contextLoads() {
    }

}
