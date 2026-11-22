package br.com.aluguelcarros.service;

import br.com.aluguelcarros.dto.CategoriaVeiculoRequest;
import br.com.aluguelcarros.dto.CategoriaVeiculoResponse;
import br.com.aluguelcarros.entity.CategoriaVeiculo;
import br.com.aluguelcarros.exception.CategoriaDuplicadaException;
import br.com.aluguelcarros.repository.CategoriaVeiculoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoriaVeiculoServiceTest {

    @Mock
    private CategoriaVeiculoRepository categoriaVeiculoRepository;

    @InjectMocks
    private CategoriaVeiculoService categoriaVeiculoService;

    @Test
    void deveListarTodasAsCategoriasConvertendoParaResponse() {
        //Arrange
        CategoriaVeiculo categoria = new CategoriaVeiculo();
        categoria.setNome("SUV");
        categoria.setDescricao("Veículos utilitários esportivos.");
        categoria.setAtiva(true);

        when(categoriaVeiculoRepository.findAll())
                .thenReturn(List.of(categoria));

        // Act
        List<CategoriaVeiculoResponse> resultado = categoriaVeiculoService.listarTodas();

        // Assert
        assertEquals(1, resultado.size());

        CategoriaVeiculoResponse response = resultado.getFirst();

        assertEquals("SUV", response.getNome());
        assertEquals("Veículos utilitários esportivos.", response.getDescricao());
        assertTrue(response.isAtiva());

        verify(categoriaVeiculoRepository, times(1)).findAll();

    }

    @Test
    void deveCadastrarCategoriaComSucesso() {
        // Arrange
        CategoriaVeiculoRequest request = new CategoriaVeiculoRequest();
        request.setNome("SUV");
        request.setDescricao("Veículos utilitários esportivos");

        when(categoriaVeiculoRepository.existsByNome("SUV"))
                .thenReturn(false);

        when(categoriaVeiculoRepository.save(any(CategoriaVeiculo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CategoriaVeiculoResponse resultado =
                categoriaVeiculoService.cadastrar(request);

        // Assert
        assertNotNull(resultado);
        assertEquals("SUV", resultado.getNome());
        assertEquals("Veículos utilitários esportivos", resultado.getDescricao());
        assertTrue(resultado.isAtiva());

        verify(categoriaVeiculoRepository, times(1)).existsByNome("SUV");
        verify(categoriaVeiculoRepository, times(1))
                .save(any(CategoriaVeiculo.class));
    }

    @Test
    void deveLancarExcecaoQuandoCategoriaJaExiste() {
        //Arrange
        CategoriaVeiculoRequest request = new CategoriaVeiculoRequest();
        request.setNome("SUV");
        request.setDescricao("Veículos utilitários esportivos.");

        when(categoriaVeiculoRepository.existsByNome("SUV"))
                .thenReturn(true);

        //Act & Assert
        CategoriaDuplicadaException exception = assertThrows(
                CategoriaDuplicadaException.class,
                () -> categoriaVeiculoService.cadastrar(request)
        );

        assertEquals("Já existe uma categoria com esse nome.", exception.getMessage());

        verify(categoriaVeiculoRepository, times(1)).existsByNome("SUV");
        verify(categoriaVeiculoRepository, never()).save(any(CategoriaVeiculo.class));
    }
}
