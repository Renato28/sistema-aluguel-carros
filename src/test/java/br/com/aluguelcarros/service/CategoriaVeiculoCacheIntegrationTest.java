package br.com.aluguelcarros.service;

import br.com.aluguelcarros.config.TestcontainersConfiguration;
import br.com.aluguelcarros.dto.CategoriaVeiculoRequest;
import br.com.aluguelcarros.dto.CategoriaVeiculoResponse;
import br.com.aluguelcarros.entity.CategoriaVeiculo;
import br.com.aluguelcarros.repository.CategoriaVeiculoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class CategoriaVeiculoCacheIntegrationTest {

    @Autowired
    private CacheManager cacheManager;

    @MockitoBean
    private CategoriaVeiculoRepository categoriaVeiculoRepository;

    @Autowired
    private CategoriaVeiculoService categoriaVeiculoService;

    @BeforeEach
    void setUp() {
        Objects.requireNonNull(cacheManager.getCache("categorias")).clear();
        reset(categoriaVeiculoRepository);
    }

    @Test
    void deveUtilizarCacheAoListarCategoriasMaisDeUmaVez() {

        when(categoriaVeiculoRepository.findAll())
                .thenReturn(List.of());

        List<CategoriaVeiculoResponse> primeiraConsulta =
                categoriaVeiculoService.listarTodas();

        List<CategoriaVeiculoResponse> segundaConsulta =
                categoriaVeiculoService.listarTodas();

        assertEquals(primeiraConsulta, segundaConsulta);

        verify(categoriaVeiculoRepository, times(1)).findAll();
    }

    @Test
    void deveInvalidarCacheAoCadastrarCategoria() {

        when(categoriaVeiculoRepository.findAll())
                .thenReturn(List.of());

        when(categoriaVeiculoRepository.existsByNome("SUV"))
                .thenReturn(false);

        CategoriaVeiculo categoriaSalva = new CategoriaVeiculo();
        categoriaSalva.setId(1L);
        categoriaSalva.setNome("SUV");
        categoriaSalva.setDescricao("Veículos utilitários esportivos");

        when(categoriaVeiculoRepository.save(any(CategoriaVeiculo.class)))
                .thenReturn(categoriaSalva);

        // Primeira consulta: preenche o cache.
        categoriaVeiculoService.listarTodas();

        // Cadastro: deve invalidar o cache.
        CategoriaVeiculoRequest request = new CategoriaVeiculoRequest();
        request.setNome("SUV");
        request.setDescricao("Veículos utilitários esportivos");

        categoriaVeiculoService.cadastrar(request);

        assertNull(
                Objects.requireNonNull(cacheManager.getCache("categorias"))
                        .get(SimpleKey.EMPTY),
                "O cache deveria ser invalidado após o cadastro."
        );

        // Segunda consulta: deve consultar novamente o repositório.
        categoriaVeiculoService.listarTodas();

        verify(categoriaVeiculoRepository, times(2)).findAll();
        verify(categoriaVeiculoRepository, times(1))
                .save(any(CategoriaVeiculo.class));
    }

    @AfterEach
    void limparCacheAposTeste() {
        Objects.requireNonNull(cacheManager.getCache("categorias")).clear();
    }
}
