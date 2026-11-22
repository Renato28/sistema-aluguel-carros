package br.com.aluguelcarros.service;

import br.com.aluguelcarros.dto.VeiculoRequest;
import br.com.aluguelcarros.dto.VeiculoResponse;
import br.com.aluguelcarros.entity.CategoriaVeiculo;
import br.com.aluguelcarros.entity.StatusVeiculo;
import br.com.aluguelcarros.entity.Veiculo;
import br.com.aluguelcarros.exception.CategoriaInativaException;
import br.com.aluguelcarros.exception.CategoriaNaoEncontradaException;
import br.com.aluguelcarros.exception.PlacaVeiculoDuplicadaException;
import br.com.aluguelcarros.exception.VeiculoInativoException;
import br.com.aluguelcarros.exception.VeiculoNaoEncontradoException;
import br.com.aluguelcarros.repository.CategoriaVeiculoRepository;
import br.com.aluguelcarros.repository.VeiculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VeiculoServiceTest {

    @Mock
    private VeiculoRepository veiculoRepository;

    @Mock
    private CategoriaVeiculoRepository categoriaVeiculoRepository;

    private VeiculoService veiculoService;

    @BeforeEach
    void setUp() {
        veiculoService = new VeiculoService(
                veiculoRepository,
                categoriaVeiculoRepository
        );
    }

    @Test
    void deveCadastrarUmVeiculoComSucesso() {

        VeiculoRequest request = new VeiculoRequest();
        request.setCategoriaId(1L);
        request.setMarca("Toyota");
        request.setModelo("Corolla Cross");
        request.setAnoFabricacao(2025);
        request.setPlaca("ABC1D23");
        request.setCor("Preto");
        request.setQuilometragem(1000);
        request.setValorDiaria(new BigDecimal("250.00"));

        CategoriaVeiculo categoria = mock(CategoriaVeiculo.class);

        when(veiculoRepository.existsByPlaca("ABC1D23"))
                .thenReturn(false);

        when(categoriaVeiculoRepository.findById(1L))
                .thenReturn(Optional.of(categoria));

        when(categoria.isAtiva()).thenReturn(true);
        when(categoria.getId()).thenReturn(1L);
        when(categoria.getNome()).thenReturn("SUV");

        when(veiculoRepository.save(any(Veiculo.class)))
                .thenAnswer(invocation -> {
                    Veiculo veiculo = invocation.getArgument(0);
                    veiculo.setId(1L);
                    return veiculo;
                });

        VeiculoResponse response = veiculoService.cadastrar(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(1L, response.getCategoriaId());
        assertEquals("SUV", response.getCategoriaNome());
        assertEquals("Toyota", response.getMarca());
        assertEquals("Corolla Cross", response.getModelo());
        assertEquals(2025, response.getAnoFabricacao());
        assertEquals("ABC1D23", response.getPlaca());
        assertEquals("Preto", response.getCor());
        assertEquals(1000, response.getQuilometragem());
        assertEquals(new BigDecimal("250.00"), response.getValorDiaria());

        verify(veiculoRepository).existsByPlaca("ABC1D23");
        verify(categoriaVeiculoRepository).findById(1L);
        verify(veiculoRepository).save(any(Veiculo.class));
    }

    @Test
    void naoDeveCadastrarVeiculoComPlacaDuplicada() {

        VeiculoRequest request = new VeiculoRequest();
        request.setCategoriaId(1L);
        request.setMarca("Toyota");
        request.setModelo("Corolla Cross");
        request.setAnoFabricacao(2025);
        request.setPlaca("ABC1D23");
        request.setCor("Preto");
        request.setQuilometragem(1000);
        request.setValorDiaria(new BigDecimal("250.00"));

        when(veiculoRepository.existsByPlaca("ABC1D23"))
            .thenReturn(true);

        assertThrows(
                PlacaVeiculoDuplicadaException.class,
                () -> veiculoService.cadastrar(request)
        );

        verify(veiculoRepository).existsByPlaca("ABC1D23");

        verify(categoriaVeiculoRepository, never())
                .findById(anyLong());

        verify(veiculoRepository, never())
                .save(any(Veiculo.class));
    }

    @Test
    void naoDeveCadastrarVeiculoComCategoriaInexistente() {

        VeiculoRequest request = new VeiculoRequest();
        request.setCategoriaId(99L);
        request.setMarca("Toyota");
        request.setModelo("Corolla Cross");
        request.setAnoFabricacao(2025);
        request.setPlaca("ABC1D23");
        request.setCor("Preto");
        request.setQuilometragem(1000);
        request.setValorDiaria(new BigDecimal("250.00"));

        when(veiculoRepository.existsByPlaca("ABC1D23"))
                .thenReturn(false);

        when(categoriaVeiculoRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                CategoriaNaoEncontradaException.class,
                () -> veiculoService.cadastrar(request)
        );

        verify(veiculoRepository).existsByPlaca("ABC1D23");

        verify(categoriaVeiculoRepository).findById(99L);

        verify(veiculoRepository, never())
                .save(any(Veiculo.class));

    }

    @Test
    void naoDeveCadastrarVeiculoComCategoriaInativa() {

        VeiculoRequest request = new VeiculoRequest();
        request.setCategoriaId(1L);
        request.setMarca("Toyota");
        request.setModelo("Corolla Cross");
        request.setAnoFabricacao(2025);
        request.setPlaca("ABC1D23");
        request.setCor("Preto");
        request.setQuilometragem(1000);
        request.setValorDiaria(new BigDecimal("250.00"));

        CategoriaVeiculo categoria = mock(CategoriaVeiculo.class);

        when(veiculoRepository.existsByPlaca("ABC1D23"))
                .thenReturn(false);

        when(categoriaVeiculoRepository.findById(1L))
                .thenReturn(Optional.of(categoria));

        when(categoria.isAtiva()).thenReturn(false);

        assertThrows(
                CategoriaInativaException.class,
                () -> veiculoService.cadastrar(request)
        );

        verify(veiculoRepository).existsByPlaca("ABC1D23");

        verify(categoriaVeiculoRepository).findById(1L);

        verify(veiculoRepository, never())
                .save(any(Veiculo.class));
    }

    @Test
    void deveAtualizarVeiculoComSucesso() {

        VeiculoRequest request = new VeiculoRequest();
        request.setCategoriaId(1L);
        request.setMarca("Honda");
        request.setModelo("Civic");
        request.setAnoFabricacao(2024);
        request.setPlaca("XYZ1A23");
        request.setCor("Prata");
        request.setQuilometragem(5000);
        request.setValorDiaria(new BigDecimal("300.00"));

        Veiculo veiculo = new Veiculo();
        veiculo.setId(1L);
        veiculo.setMarca("Toyota");
        veiculo.setModelo("Corolla");
        veiculo.setPlaca("ABC1D23");

        CategoriaVeiculo categoria = mock(CategoriaVeiculo.class);
        veiculo.setCategoria(categoria);

        when(veiculoRepository.findById(1L))
                .thenReturn(Optional.of(veiculo));

        when(veiculoRepository.existsByPlacaAndIdNot("XYZ1A23", 1L))
                .thenReturn(false);

        when(categoriaVeiculoRepository.findById(1L))
                .thenReturn(Optional.of(categoria));

        when(categoria.isAtiva()).thenReturn(true);
        when(categoria.getId()).thenReturn(1L);
        when(categoria.getNome()).thenReturn("Sedan");

        when(veiculoRepository.save(any(Veiculo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        VeiculoResponse response = veiculoService.atualizar(1L, request);

        assertNotNull(response);
        assertEquals("Honda", response.getMarca());
        assertEquals("Civic", response.getModelo());
        assertEquals("XYZ1A23", response.getPlaca());
        assertEquals("Prata", response.getCor());
        assertEquals(2024, response.getAnoFabricacao());
        assertEquals(1L, response.getCategoriaId());
        assertEquals("Sedan", response.getCategoriaNome());
        assertEquals(new BigDecimal("300.00"), response.getValorDiaria());

        verify(veiculoRepository).findById(1L);
        verify(veiculoRepository).existsByPlacaAndIdNot("XYZ1A23", 1L);
        verify(categoriaVeiculoRepository).findById(1L);
        verify(veiculoRepository).save(veiculo);
    }

    @Test
    void naoDeveAtualizarVeiculoInexistente() {

        VeiculoRequest request = new VeiculoRequest();
        request.setCategoriaId(1L);
        request.setPlaca("XYZ1A23");

        when(veiculoRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                VeiculoNaoEncontradoException.class,
                () -> veiculoService.atualizar(99L, request)
        );

        verify(veiculoRepository).findById(99L);
        verify(veiculoRepository, never()).save(any(Veiculo.class));
        verify(categoriaVeiculoRepository, never()).findById(any());
    }

    @Test
    void naoDeveAtualizarVeiculoInativo() {

        VeiculoRequest request = new VeiculoRequest();
        request.setCategoriaId(1L);
        request.setPlaca("XYZ1A23");

        Veiculo veiculo = new Veiculo();
        veiculo.setId(1L);
        veiculo.setStatus(StatusVeiculo.INATIVO);

        when(veiculoRepository.findById(1L))
                .thenReturn(Optional.of(veiculo));

        assertThrows(
                VeiculoInativoException.class,
                () -> veiculoService.atualizar(1L, request)
        );

        verify(veiculoRepository).findById(1L);
        verify(veiculoRepository, never()).existsByPlacaAndIdNot(any(), any());
        verify(veiculoRepository, never()).save(any(Veiculo.class));
        verify(categoriaVeiculoRepository, never()).findById(any());
    }

    @Test
    void naoDeveAtualizarVeiculoComPlacaDuplicada() {

        VeiculoRequest request = new VeiculoRequest();
        request.setCategoriaId(1L);
        request.setPlaca("XYZ1A23");

        Veiculo veiculo = new Veiculo();
        veiculo.setId(1L);

        when(veiculoRepository.findById(1L))
                .thenReturn(Optional.of(veiculo));

        when(veiculoRepository.existsByPlacaAndIdNot("XYZ1A23", 1L))
                .thenReturn(true);

        assertThrows(
                PlacaVeiculoDuplicadaException.class,
                () -> veiculoService.atualizar(1L, request)
        );

        verify(veiculoRepository).findById(1L);
        verify(veiculoRepository).existsByPlacaAndIdNot("XYZ1A23", 1L);
        verify(categoriaVeiculoRepository, never()).findById(any());
        verify(veiculoRepository, never()).save(any(Veiculo.class));
    }

    @Test
    void naoDeveAtualizarVeiculoComCategoriaInexistente() {

        VeiculoRequest request = new VeiculoRequest();
        request.setCategoriaId(99L);
        request.setPlaca("XYZ1A23");

        Veiculo veiculo = new Veiculo();
        veiculo.setId(1L);

        when(veiculoRepository.findById(1L))
                .thenReturn(Optional.of(veiculo));

        when(veiculoRepository.existsByPlacaAndIdNot("XYZ1A23", 1L))
                .thenReturn(false);

        when(categoriaVeiculoRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                CategoriaNaoEncontradaException.class,
                () -> veiculoService.atualizar(1L, request)
        );

        verify(veiculoRepository).findById(1L);
        verify(veiculoRepository).existsByPlacaAndIdNot("XYZ1A23", 1L);
        verify(categoriaVeiculoRepository).findById(99L);
        verify(veiculoRepository, never()).save(any(Veiculo.class));
    }

    @Test
    void naoDeveAtualizarVeiculoComCategoriaInativa() {

        VeiculoRequest request = new VeiculoRequest();
        request.setCategoriaId(1L);
        request.setPlaca("XYZ1A23");

        Veiculo veiculo = new Veiculo();
        veiculo.setId(1L);

        CategoriaVeiculo categoria = mock(CategoriaVeiculo.class);

        when(veiculoRepository.findById(1L))
                .thenReturn(Optional.of(veiculo));

        when(veiculoRepository.existsByPlacaAndIdNot("XYZ1A23", 1L))
                .thenReturn(false);

        when(categoriaVeiculoRepository.findById(1L))
                .thenReturn(Optional.of(categoria));

        when(categoria.isAtiva()).thenReturn(false);

        assertThrows(
                CategoriaInativaException.class,
                () -> veiculoService.atualizar(1L, request)
        );

        verify(veiculoRepository).findById(1L);
        verify(veiculoRepository).existsByPlacaAndIdNot("XYZ1A23", 1L);
        verify(categoriaVeiculoRepository).findById(1L);
        verify(veiculoRepository, never()).save(any(Veiculo.class));
    }

    @Test
    void deveListarTodosOsVeiculos() {

        CategoriaVeiculo categoria = mock(CategoriaVeiculo.class);

        when(categoria.getId()).thenReturn(1L);
        when(categoria.getNome()).thenReturn("Sedan");

        Veiculo veiculo = new Veiculo();
        veiculo.setId(1L);
        veiculo.setCategoria(categoria);
        veiculo.setMarca("Honda");
        veiculo.setModelo("Civic");
        veiculo.setPlaca("ABC1D23");

        when(veiculoRepository.findAllByStatusNot(StatusVeiculo.INATIVO))
                .thenReturn(List.of(veiculo));

        List<VeiculoResponse> response = veiculoService.listarTodos();

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("Honda", response.getFirst().getMarca());
        assertEquals("Civic", response.getFirst().getModelo());
        assertEquals("Sedan", response.getFirst().getCategoriaNome());
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremVeiculos() {

        when(veiculoRepository.findAllByStatusNot(StatusVeiculo.INATIVO))
                .thenReturn(List.of());

        List<VeiculoResponse> response = veiculoService.listarTodos();

        assertNotNull(response);
        assertTrue(response.isEmpty());

        verify(veiculoRepository).findAllByStatusNot(StatusVeiculo.INATIVO);
    }

    @Test
    void deveBuscarVeiculoPorIdComSucesso() {

        CategoriaVeiculo categoria = mock(CategoriaVeiculo.class);

        when(categoria.getId()).thenReturn(1L);
        when(categoria.getNome()).thenReturn("Sedan");

        Veiculo veiculo = new Veiculo();
        veiculo.setId(1L);
        veiculo.setCategoria(categoria);
        veiculo.setMarca("Honda");
        veiculo.setModelo("Civic");
        veiculo.setAnoFabricacao(2024);
        veiculo.setPlaca("ABC1D23");
        veiculo.setCor("Preto");
        veiculo.setQuilometragem(5000);
        veiculo.setValorDiaria(new BigDecimal("300.00"));

        when(veiculoRepository.findById(1L))
                .thenReturn(Optional.of(veiculo));

        VeiculoResponse response = veiculoService.buscarPorId(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(1L, response.getCategoriaId());
        assertEquals("Sedan", response.getCategoriaNome());
        assertEquals("Honda", response.getMarca());
        assertEquals("Civic", response.getModelo());
        assertEquals("ABC1D23", response.getPlaca());
        assertEquals("Preto", response.getCor());
        assertEquals(2024, response.getAnoFabricacao());
        assertEquals(5000, response.getQuilometragem());
        assertEquals(new BigDecimal("300.00"), response.getValorDiaria());

        verify(veiculoRepository).findById(1L);
    }

    @Test
    void naoDeveBuscarVeiculoInexistente() {

        when(veiculoRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                VeiculoNaoEncontradoException.class,
                () -> veiculoService.buscarPorId(99L)
        );

        verify(veiculoRepository).findById(99L);
    }

    @Test
    void deveExcluirVeiculoComSucesso() {

        Veiculo veiculo = new Veiculo();
        veiculo.setId(1L);
        veiculo.setStatus(StatusVeiculo.DISPONIVEL);

        when(veiculoRepository.findById(1L))
                .thenReturn(Optional.of(veiculo));

        when(veiculoRepository.save(any(Veiculo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        veiculoService.excluir(1L);

        assertEquals(StatusVeiculo.INATIVO, veiculo.getStatus());

        verify(veiculoRepository).findById(1L);
        verify(veiculoRepository).save(veiculo);
    }

    @Test
    void naoDeveExcluirVeiculoInexistente() {

        when(veiculoRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                VeiculoNaoEncontradoException.class,
                () -> veiculoService.excluir(99L)
        );

        verify(veiculoRepository).findById(99L);
        verify(veiculoRepository, never()).save(any(Veiculo.class));
    }

    @Test
    void naoDeveExcluirVeiculoInativo() {

        Veiculo veiculo = new Veiculo();
        veiculo.setId(1L);
        veiculo.setStatus(StatusVeiculo.INATIVO);

        when(veiculoRepository.findById(1L))
                .thenReturn(Optional.of(veiculo));

        assertThrows(
                VeiculoInativoException.class,
                () -> veiculoService.excluir(1L)
        );

        verify(veiculoRepository).findById(1L);
        verify(veiculoRepository, never()).save(any(Veiculo.class));
    }
}
