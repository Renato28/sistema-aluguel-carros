package br.com.aluguelcarros.service;

import br.com.aluguelcarros.dto.ReservaResponse;
import br.com.aluguelcarros.entity.*;
import br.com.aluguelcarros.exception.*;
import br.com.aluguelcarros.repository.ClienteRepository;
import br.com.aluguelcarros.repository.ReservaRepository;
import br.com.aluguelcarros.repository.UsuarioRepository;
import br.com.aluguelcarros.repository.VeiculoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private VeiculoRepository veiculoRepository;

    @Mock
    private UsuarioRepository  usuarioRepository;

    private ReservaService reservaService;

    private Cliente cliente;
    private Veiculo veiculo;

    @BeforeEach
    void setUp() {
        reservaService = new ReservaService(
                reservaRepository,
                clienteRepository,
                veiculoRepository,
                usuarioRepository
        );

        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("João da Silva");
        cliente.setCpf("12345678901");
        cliente.setEmail("joao@email.com");
        cliente.setAtivo(true);

        veiculo = new Veiculo();
        veiculo.setId(1L);
        veiculo.setMarca("Toyota");
        veiculo.setModelo("Corolla");
        veiculo.setAnoFabricacao(2024);
        veiculo.setPlaca("ABC1D23");
        veiculo.setCor("Prata");
        veiculo.setQuilometragem(10000);
        veiculo.setValorDiaria(new BigDecimal("150.00"));
        veiculo.setStatus(StatusVeiculo.DISPONIVEL);
    }

    private void autenticarCliente(Cliente cliente) {

        Usuario usuario = new Usuario();
        usuario.setNome("João da Silva");
        usuario.setEmail("joao@email.com");
        usuario.setCliente(cliente);
        usuario.setPerfil(PerfilUsuario.CLIENTE);

        when(usuarioRepository.findByEmail("joao@email.com"))
                .thenReturn(Optional.of(usuario));

        var authentication = new UsernamePasswordAuthenticationToken(
                "joao@email.com", null,
                List.of(() -> "ROLE_CLIENTE"));

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void deveCadastrarReservaComSucesso() {
        LocalDate retirada = LocalDate.now().plusDays(5);
        LocalDate devolucao = retirada.plusDays(3);

        when(clienteRepository.findById(1L))
                .thenReturn(Optional.of(cliente));

        when(veiculoRepository.findById(1L))
                .thenReturn(Optional.of(veiculo));

        when(reservaRepository.
                existsByVeiculoIdAndStatusInAndDataRetiradaLessThanAndDataDevolucaoGreaterThan(
                        eq(1L), any(), eq(devolucao), eq(retirada)))
        .thenReturn(false);

        when(reservaRepository.save(any(Reserva.class)))
                .thenAnswer(invocation -> {
                    Reserva reserva = invocation.getArgument(0);
                    reserva.setId(1L);
                    return reserva;
                });

        var response = reservaService.cadastrar(
                1L,
                1L,
                retirada,
                devolucao);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(StatusReserva.PENDENTE, response.getStatus());
        assertEquals(new BigDecimal("150.00"), response.getValorDiaria());

        ArgumentCaptor<Reserva> reservaCaptor =
                ArgumentCaptor.forClass(Reserva.class);

        verify(reservaRepository).save(reservaCaptor.capture());

        Reserva reservaSalva = reservaCaptor.getValue();

        assertEquals(cliente, reservaSalva.getCliente());
        assertEquals(veiculo, reservaSalva.getVeiculo());
        assertEquals(retirada, reservaSalva.getDataRetirada());
        assertEquals(devolucao, reservaSalva.getDataDevolucao());
        assertEquals(new BigDecimal("150.00"), reservaSalva.getValorDiaria());
        assertEquals(StatusReserva.PENDENTE, reservaSalva.getStatus());
    }

    @Test
    void deveCadastrarReservaParaClienteAutenticado() {

        // Arrange
        cliente.setId(1L);
        veiculo.setId(1L);

        autenticarCliente(cliente);

        LocalDate retirada = LocalDate.now().plusDays(5);
        LocalDate devolucao = retirada.plusDays(3);

        when(veiculoRepository.findById(1L))
                .thenReturn(Optional.of(veiculo));

        when(reservaRepository
                .existsByVeiculoIdAndStatusInAndDataRetiradaLessThanAndDataDevolucaoGreaterThan(
                        eq(1L), any(), eq(devolucao), eq(retirada)))
                .thenReturn(false);

        when(reservaRepository.save(any(Reserva.class)))
                .thenAnswer(invocation -> {
                    Reserva reserva = invocation.getArgument(0);
                    reserva.setId(10L);
                    return reserva;
                });

        // Act
        ReservaResponse response = reservaService.cadastrar(
                999L,
                1L,
                retirada,
                devolucao
        );

        // Assert
        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals(1L, response.getClienteId());

        verify(usuarioRepository).findByEmail("joao@email.com");
        verify(clienteRepository, never()).findById(any());
        verify(reservaRepository).save(any(Reserva.class));
    }

    @Test
    void naoDeveCadastrarReservaQuandoUsuarioNaoPossuiClienteVinculado() {

        // Arrange
        Usuario usuario = new Usuario();
        usuario.setNome("Usuário sem cliente");
        usuario.setEmail("semcliente@email.com");
        usuario.setPerfil(PerfilUsuario.CLIENTE);
        usuario.setCliente(null);

        when(usuarioRepository.findByEmail("semcliente@email.com"))
                .thenReturn(Optional.of(usuario));

        var authentication = new UsernamePasswordAuthenticationToken(
                "semcliente@email.com",
                null,
                List.of(() -> "ROLE_CLIENTE"));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Act + Assert
        assertThrows(
                IllegalStateException.class,
                () -> reservaService.cadastrar(
                        1L,
                        1L,
                        LocalDate.now().plusDays(5),
                        LocalDate.now().plusDays(8)
                )
        );

        verify(usuarioRepository).findByEmail("semcliente@email.com");

        verifyNoInteractions(
                clienteRepository,
                veiculoRepository,
                reservaRepository
        );
    }

    @Test
    void naoDeveCadastrarReservaQuandoUsuarioAutenticadoNaoExiste() {

        // Arrange
        var authentication = new UsernamePasswordAuthenticationToken(
                "inexistente@email.com",
                null,
                List.of(() -> "ROLE_CLIENTE"));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(usuarioRepository.findByEmail("inexistente@email.com"))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(
                UsuarioNaoEncontradoException.class,
                () -> reservaService.cadastrar(
                        1L,
                        1L,
                        LocalDate.now().plusDays(5),
                        LocalDate.now().plusDays(8)
                )
        );

        verify(usuarioRepository).findByEmail("inexistente@email.com");

        verifyNoInteractions(
                clienteRepository,
                veiculoRepository,
                reservaRepository
        );
    }


    @Test
    void naoDeveCadastrarReservaQuandoExisteConflitoDeDatas() {

        LocalDate retirada = LocalDate.now().plusDays(5);
        LocalDate devolucao = retirada.plusDays(3);

        when(clienteRepository.findById(1L))
            .thenReturn(Optional.of(cliente));

        when(veiculoRepository.findById(1L))
                .thenReturn(Optional.of(veiculo));

        when(reservaRepository
                .existsByVeiculoIdAndStatusInAndDataRetiradaLessThanAndDataDevolucaoGreaterThan(
                        eq(1L), any(), eq(devolucao), eq(retirada)))
                .thenReturn(true);

        assertThrows(
                VeiculoIndisponivelException.class,
                () -> reservaService.cadastrar(
                       1L,
                       1L,
                       retirada,
                       devolucao
                )
        );

        verify(reservaRepository, never()).save(any(Reserva.class));

    }

    @Test
    void naoDeveCadastrarReservaComDataRetiradaNoPassado() {

        LocalDate retirada = LocalDate.now().minusDays(1);
        LocalDate devolucao = retirada.plusDays(3);

        assertThrows(
                PeriodoReservaInvalidoException.class,
                () -> reservaService.cadastrar(
                        1L,
                        1L,
                        retirada,
                        devolucao
                )
        );

        verifyNoInteractions(
                clienteRepository,
                veiculoRepository,
                reservaRepository
        );

    }

    @Test
    void naoDeveCadastrarReservaParaClienteInativo() {

        cliente.setAtivo(false);

        when(clienteRepository.findById(1L))
                .thenReturn(Optional.of(cliente));

        assertThrows(
                ClienteInativoException.class,
                () -> reservaService.cadastrar(
                        1L,
                        1L,
                        LocalDate.now().plusDays(5),
                        LocalDate.now().plusDays(8)
                )
        );

        verifyNoInteractions(
                veiculoRepository,
                reservaRepository);
    }

    @Test
    void naoDeveCadastrarReservaQuandoDataRetiradaEhNula() {

        LocalDate devolucao = LocalDate.now().plusDays(10);

        assertThrows(
                PeriodoReservaInvalidoException.class,
                () -> reservaService.cadastrar(
                        1L,
                        1L,
                        null,
                        devolucao
                )
        );

        verifyNoInteractions(
                clienteRepository,
                veiculoRepository,
                reservaRepository
        );
    }

    @Test
    void naoDeveCadastrarReservaQuandoDataDevolucaoEhNula() {

        LocalDate retirada = LocalDate.now().plusDays(10);

        assertThrows(
                PeriodoReservaInvalidoException.class,
                () -> reservaService.cadastrar(
                        1L,
                        1L,
                        retirada,
                        null
                )
        );

        verifyNoInteractions(
                clienteRepository,
                veiculoRepository,
                reservaRepository
        );
    }

    @Test
    void naoDeveCadastrarReservaQuandoDataDevolucaoEhAnteriorADataRetirada() {

        LocalDate retirada = LocalDate.now().plusDays(10);
        LocalDate devolucao = retirada.minusDays(1);

        assertThrows(
                PeriodoReservaInvalidoException.class,
                () -> reservaService.cadastrar(
                        1L,
                        1L,
                        retirada,
                        devolucao
                )
        );

        verifyNoInteractions(
                clienteRepository,
                veiculoRepository,
                reservaRepository
        );
    }

    @Test
    void naoDeveCadastrarReservaParaVeiculoInativo() {

        veiculo.setStatus(StatusVeiculo.INATIVO);

        when(clienteRepository.findById(1L))
                .thenReturn(Optional.of(cliente));

        when(veiculoRepository.findById(1L))
                .thenReturn(Optional.of(veiculo));

        assertThrows(
                VeiculoInativoException.class,
                () -> reservaService.cadastrar(
                        1L,
                        1L,
                        LocalDate.now().plusDays(5),
                        LocalDate.now().plusDays(8)
                )
        );

        verifyNoInteractions(reservaRepository);

    }

    @Test
    void naoDeveCadastrarReservaParaVeiculoAlugado() {

        veiculo.setStatus(StatusVeiculo.ALUGADO);

        when(clienteRepository.findById(1L))
                .thenReturn(Optional.of(cliente));

        when(veiculoRepository.findById(1L))
                .thenReturn(Optional.of(veiculo));

        assertThrows(
                VeiculoIndisponivelException.class,
                () -> reservaService.cadastrar(
                        1L,
                        1L,
                        LocalDate.now().plusDays(5),
                        LocalDate.now().plusDays(8)
                )
        );

        verifyNoInteractions(reservaRepository);
    }

    @Test
    void naoDeveCadastrarReservaQuandoDatasSaoIguais() {

        LocalDate retirada = LocalDate.now().plusDays(5);
        LocalDate devolucao = retirada;

        assertThrows(
                PeriodoReservaInvalidoException.class,
                () -> reservaService.cadastrar(
                        1L,
                        1L,
                        retirada,
                        devolucao
                )
        );

        verifyNoInteractions(
                clienteRepository,
                veiculoRepository,
                reservaRepository);
    }

    @Test
    void naoDeveCadastrarReservaQuandoClienteNaoExiste() {

        when(clienteRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ClienteNaoEncontradoException.class,
                () -> reservaService.cadastrar(
                        1L,
                        1L,
                        LocalDate.now().plusDays(5),
                        LocalDate.now().plusDays(8)
                )
        );

        verifyNoInteractions(
                veiculoRepository,
                reservaRepository);
    }

    @Test
    void naoDeveCadastrarReservaQuandoVeiculoNaoExiste() {

        when(clienteRepository.findById(1L))
                .thenReturn(Optional.of(cliente));

        when(veiculoRepository.findById(1L))
            .thenReturn(Optional.empty());

        assertThrows(
                VeiculoNaoEncontradoException.class,
                () -> reservaService.cadastrar(
                        1L,
                        1L,
                        LocalDate.now().plusDays(5),
                        LocalDate.now().plusDays(8)
                )
        );

        verifyNoInteractions(reservaRepository);

    }

    @Test
    void deveCancelarReservaConfirmadaComSucesso() {

        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("João");

        Veiculo veiculo = new Veiculo();
        veiculo.setMarca("Toyota");
        veiculo.setModelo("Corolla");

        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setStatus(StatusReserva.CONFIRMADA);
        reserva.setCliente(cliente);
        reserva.setVeiculo(veiculo);

        when(reservaRepository.findById(1L))
            .thenReturn(Optional.of(reserva));

        var response = reservaService.cancelar(1L);

        assertNotNull(response);
        assertEquals(StatusReserva.CANCELADA, response.getStatus());
        assertEquals(StatusReserva.CANCELADA, reserva.getStatus()); // a entidade foi alterada

        verify(reservaRepository).findById(1L);
    }

    @Test
    void naoDeveCancelarReservaQueJaFoiCancelada() {

        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setStatus(StatusReserva.CANCELADA);

        when(reservaRepository.findById(1L))
                .thenReturn(Optional.of(reserva));

        assertThrows(
                ReservaJaCanceladaException.class,
                () -> reservaService.cancelar(1L)
        );

        verify(reservaRepository).findById(1L);
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    void naoDeveCancelarReservaConcluida() {

        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setStatus(StatusReserva.CONCLUIDA);

        when(reservaRepository.findById(1L))
                .thenReturn(Optional.of(reserva));

        assertThrows(
                ReservaNaoPodeSerCanceladaException.class,
                () -> reservaService.cancelar(1L)
        );

        verify(reservaRepository).findById(1L);
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    void deveConfirmarReservaPendenteComSucesso() {

        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("João");

        Veiculo veiculo = new Veiculo();
        veiculo.setMarca("Toyota");
        veiculo.setModelo("Corolla");

        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setStatus(StatusReserva.PENDENTE);
        reserva.setCliente(cliente);
        reserva.setVeiculo(veiculo);

        when(reservaRepository.findById(1L))
                .thenReturn(Optional.of(reserva));

        var response = reservaService.confirmar(1L);

        assertNotNull(response);
        assertEquals(StatusReserva.CONFIRMADA, response.getStatus());
        assertEquals(StatusReserva.CONFIRMADA, reserva.getStatus()); // a entidade foi alterada

        verify(reservaRepository).findById(1L);
    }

    @Test
    void naoDeveConfirmarReservaQueJaEstaConfirmada() {

        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setStatus(StatusReserva.CONFIRMADA);

        when(reservaRepository.findById(1L))
            .thenReturn(Optional.of(reserva));

        assertThrows(
                ReservaNaoPodeSerConfirmadaException.class,
                () -> reservaService.confirmar(1L)
        );

        verify(reservaRepository).findById(1L);
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    void naoDeveConfirmarReservaCancelada() {

        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setStatus(StatusReserva.CANCELADA);

        when(reservaRepository.findById(1L))
            .thenReturn(Optional.of(reserva));

        assertThrows(
                ReservaNaoPodeSerConfirmadaException.class,
                () -> reservaService.confirmar(1L)
        );

        verify(reservaRepository).findById(1L);
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    void naoDeveConfirmarReservaConcluida() {

        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setStatus(StatusReserva.CONCLUIDA);

        when(reservaRepository.findById(1L))
            .thenReturn(Optional.of(reserva));

        assertThrows(
                ReservaNaoPodeSerConfirmadaException.class,
                () -> reservaService.confirmar(1L)
        );

        verify(reservaRepository).findById(1L);
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    void deveAtualizarPeriodoDaReservaComSucesso() {

        LocalDate retiradaAtual = LocalDate.now().plusDays(5);
        LocalDate devolucaoAtual = retiradaAtual.plusDays(3);

        LocalDate novaRetirada = LocalDate.now().plusDays(10);
        LocalDate novaDevolucao = novaRetirada.plusDays(4);

        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("João");

        Veiculo veiculo = new Veiculo();
        veiculo.setId(1L);
        veiculo.setMarca("Toyota");
        veiculo.setModelo("Corolla");

        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setStatus(StatusReserva.PENDENTE);
        reserva.setCliente(cliente);
        reserva.setVeiculo(veiculo);
        reserva.setDataRetirada(retiradaAtual);
        reserva.setDataDevolucao(devolucaoAtual);

        when(reservaRepository.findById(1L))
                .thenReturn(Optional.of(reserva));

        when(reservaRepository
                .existsByVeiculoIdAndIdNotAndStatusInAndDataRetiradaLessThanAndDataDevolucaoGreaterThan(
                        eq(1L),
                        eq(1L),
                        any(),
                        eq(novaDevolucao),
                        eq(novaRetirada)))
                .thenReturn(false);

        var response = reservaService.atualizarPeriodo(
                1L,
                novaRetirada,
                novaDevolucao
        );

        assertNotNull(response);
        assertEquals(novaRetirada, response.getDataRetirada());
        assertEquals(novaDevolucao, response.getDataDevolucao());
        assertEquals(StatusReserva.PENDENTE, response.getStatus());

        assertEquals(novaRetirada, reserva.getDataRetirada());
        assertEquals(novaDevolucao, reserva.getDataDevolucao());

        verify(reservaRepository).findById(1L);

        verify(reservaRepository)
                .existsByVeiculoIdAndIdNotAndStatusInAndDataRetiradaLessThanAndDataDevolucaoGreaterThan(
                        eq(1L),
                        eq(1L),
                        any(),
                        eq(novaDevolucao),
                        eq(novaRetirada));
    }

    @Test
    void naoDeveAtualizarPeriodoDeReservaConfirmada() {

        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setStatus(StatusReserva.CONFIRMADA);

        when(reservaRepository.findById(1L))
            .thenReturn(Optional.of(reserva));

        LocalDate novaRetirada = LocalDate.now().plusDays(10);
        LocalDate novaDevolucao = novaRetirada.plusDays(3);

        assertThrows(
                ReservaNaoPodeSerAlteradaException.class,
                () -> reservaService.atualizarPeriodo(
                        1L,
                        novaRetirada,
                        novaDevolucao
                )
        );

        verify(reservaRepository).findById(1L);
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    void naoDeveAtualizarPeriodoDeReservaCancelada() {

        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setStatus(StatusReserva.CANCELADA);

        when(reservaRepository.findById(1L))
                .thenReturn(Optional.of(reserva));

        LocalDate novaRetirada = LocalDate.now().plusDays(10);
        LocalDate novaDevolucao = novaRetirada.plusDays(3);

        assertThrows(ReservaNaoPodeSerAlteradaException.class,
                () -> reservaService.atualizarPeriodo(
                        1L,
                        novaRetirada,
                        novaDevolucao
                )
        );

        verify(reservaRepository).findById(1L);
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    void naoDeveAtualizarPeriodoDeReservaConcluida() {

        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setStatus(StatusReserva.CONCLUIDA);

        when(reservaRepository.findById(1L))
                .thenReturn(Optional.of(reserva));

        LocalDate novaRetirada = LocalDate.now().plusDays(10);
        LocalDate novaDevolucao = novaRetirada.plusDays(3);

        assertThrows(ReservaNaoPodeSerAlteradaException.class,
                () -> reservaService.atualizarPeriodo(
                        1L,
                        novaRetirada,
                        novaDevolucao
                )
        );

        verify(reservaRepository).findById(1L);
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    void naoDeveCancelarReservaQuandoNaoExiste() {

        when(reservaRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(ReservaNaoEncontradaException.class,
                () -> reservaService.cancelar(1L)
        );

        verify(reservaRepository).findById(1L);
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    void naoDeveConfirmarReservaQuandoNaoExiste() {

        when(reservaRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ReservaNaoEncontradaException.class,
                () -> reservaService.confirmar(1L)
        );

        verify(reservaRepository).findById(1L);
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    void naoDeveAtualizarPeriodoQuandoReservaNaoExiste() {

        when(reservaRepository.findById(1L))
                .thenReturn(Optional.empty());

        LocalDate novaRetirada = LocalDate.now().plusDays(10);
        LocalDate novaDevolucao = novaRetirada.plusDays(3);

        assertThrows(
                ReservaNaoEncontradaException.class,
                () -> reservaService.atualizarPeriodo(
                        1l,
                        novaRetirada,
                        novaDevolucao
                )
        );

        verify(reservaRepository).findById(1L);
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    void naoDeveAtualizarPeriodoQuandoExisteConflitoDeDatas() {

        LocalDate retiradaAtual = LocalDate.now().plusDays(5);
        LocalDate devolucaoAtual = retiradaAtual.plusDays(3);

        LocalDate novaRetirada = LocalDate.now().plusDays(10);
        LocalDate novaDevolucao = novaRetirada.plusDays(4);

        veiculo.setId(1L);

        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setStatus(StatusReserva.PENDENTE);
        reserva.setVeiculo(veiculo);
        reserva.setDataRetirada(retiradaAtual);
        reserva.setDataDevolucao(devolucaoAtual);

        when(reservaRepository.findById(1L))
                .thenReturn(Optional.of(reserva));

        when(reservaRepository.existsByVeiculoIdAndIdNotAndStatusInAndDataRetiradaLessThanAndDataDevolucaoGreaterThan(
                eq(1L),
                eq(1L),
                any(),
                eq(novaDevolucao),
                eq(novaRetirada)
        )).thenReturn(true);

        assertThrows(
                VeiculoIndisponivelException.class,
                () -> reservaService.atualizarPeriodo(
                        1L,
                        novaRetirada,
                        novaDevolucao
                )
        );

        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    void deveLancarExcecaoQuandoClienteConsultarReservaDeOutroCliente() {

        //Arrange
        cliente.setId(1L);

        Cliente outroCliente = new Cliente();
        outroCliente.setId(2L);

        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setCliente(outroCliente);

        autenticarCliente(cliente);

        when(reservaRepository.findById(1L))
                .thenReturn(Optional.of(reserva));

        // Act + Assert
        assertThrows(AccessDeniedException.class,
                () -> reservaService.buscarPorId(1L));
    }

    @Test
    void deveLancarExcecaoQuandoClienteCancelarReservaDeOutroCliente() {
        // Arrange
        cliente.setId(1L);

        Cliente outroCliente = new Cliente();
        outroCliente.setId(2L);

        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setCliente(outroCliente);
        reserva.setStatus(StatusReserva.PENDENTE);

        autenticarCliente(cliente);

        when(reservaRepository.findById(1L))
                .thenReturn(Optional.of(reserva));

        assertThrows(
                AccessDeniedException.class,
                () -> reservaService.cancelar(1L)
        );

        assertEquals(StatusReserva.PENDENTE, reserva.getStatus());

    }

    @Test
    void deveLancarExcecaoQuandoClienteAtualizarPeriodoDeReservaDeOutroCliente() {
        // Arrange
        cliente.setId(1L);

        Cliente outroCliente = new Cliente();
        outroCliente.setId(2L);

        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setCliente(outroCliente);
        reserva.setStatus(StatusReserva.PENDENTE);

        autenticarCliente(cliente);

        when(reservaRepository.findById(1L))
                .thenReturn(Optional.of(reserva));

        // Act + Assert
        assertThrows(
                AccessDeniedException.class,
                () -> reservaService.atualizarPeriodo(
                        1L,
                        LocalDate.of(2026, 12, 10),
                        LocalDate.of(2026, 12, 15)
                )
        );
    }

    @Test
    void naoDeveAtualizarPeriodoQuandoDataRetiradaEstaNoPassado() {

        // Arrange
        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setStatus(StatusReserva.PENDENTE);

        when(reservaRepository.findById(1L))
                .thenReturn(Optional.of(reserva));

        LocalDate retirada = LocalDate.now().minusDays(1);
        LocalDate devolucao = retirada.plusDays(3);

        // Act + Assert
        assertThrows(
                PeriodoReservaInvalidoException.class,
                () -> reservaService.atualizarPeriodo(
                        1L,
                        retirada,
                        devolucao
                )
        );

        verify(reservaRepository).findById(1L);

        verify(
                reservaRepository,
                never()
        ).existsByVeiculoIdAndIdNotAndStatusInAndDataRetiradaLessThanAndDataDevolucaoGreaterThan(
                any(),
                any(),
                any(),
                any(),
                any()
        );
    }

    @Test
    void naoDeveAtualizarPeriodoQuandoDataDevolucaoEhNula() {

        // Arrange
        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setStatus(StatusReserva.PENDENTE);

        when(reservaRepository.findById(1L))
                .thenReturn(Optional.of(reserva));

        // Act + Assert
        assertThrows(
                PeriodoReservaInvalidoException.class,
                () -> reservaService.atualizarPeriodo(
                        1L,
                        LocalDate.now().plusDays(10),
                        null
                )
        );

        verify(reservaRepository).findById(1L);

        verify(
                reservaRepository,
                never()
        ).existsByVeiculoIdAndIdNotAndStatusInAndDataRetiradaLessThanAndDataDevolucaoGreaterThan(
                any(),
                any(),
                any(),
                any(),
                any()
        );
    }

    @Test
    void naoDeveAtualizarPeriodoQuandoDataRetiradaEhNula() {

        // Arrange
        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setStatus(StatusReserva.PENDENTE);

        when(reservaRepository.findById(1L))
                .thenReturn(Optional.of(reserva));

        // Act + Assert
        assertThrows(
                PeriodoReservaInvalidoException.class,
                () -> reservaService.atualizarPeriodo(
                        1L,
                        null,
                        LocalDate.now().plusDays(10)
                )
        );

        verify(reservaRepository).findById(1L);

        verify(
                reservaRepository,
                never()
        ).existsByVeiculoIdAndIdNotAndStatusInAndDataRetiradaLessThanAndDataDevolucaoGreaterThan(
                any(),
                any(),
                any(),
                any(),
                any()
        );
    }

    @Test
    void naoDeveAtualizarPeriodoQuandoDataDevolucaoEhAnteriorADataRetirada() {

        // Arrange
        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setStatus(StatusReserva.PENDENTE);

        when(reservaRepository.findById(1L))
                .thenReturn(Optional.of(reserva));

        LocalDate retirada = LocalDate.now().plusDays(10);
        LocalDate devolucao = retirada.minusDays(2);

        // Act + Assert
        assertThrows(
                PeriodoReservaInvalidoException.class,
                () -> reservaService.atualizarPeriodo(
                        1L,
                        retirada,
                        devolucao
                )
        );

        verify(reservaRepository).findById(1L);

        verify(
                reservaRepository,
                never()
        ).existsByVeiculoIdAndIdNotAndStatusInAndDataRetiradaLessThanAndDataDevolucaoGreaterThan(
                any(),
                any(),
                any(),
                any(),
                any()
        );
    }

    @Test
    void deveListarTodasAsReservas() {

        // Arrange
        cliente.setId(1L);
        veiculo.setId(1L);

        Reserva reserva1 = new Reserva();
        reserva1.setId(1L);
        reserva1.setCliente(cliente);
        reserva1.setVeiculo(veiculo);
        reserva1.setDataRetirada(LocalDate.now().plusDays(5));
        reserva1.setDataDevolucao(LocalDate.now().plusDays(8));
        reserva1.setValorDiaria(new BigDecimal("200.00"));
        reserva1.setStatus(StatusReserva.PENDENTE);

        Reserva reserva2 = new Reserva();
        reserva2.setId(2L);
        reserva2.setCliente(cliente);
        reserva2.setVeiculo(veiculo);
        reserva2.setDataRetirada(LocalDate.now().plusDays(10));
        reserva2.setDataDevolucao(LocalDate.now().plusDays(13));
        reserva2.setValorDiaria(new BigDecimal("200.00"));
        reserva2.setStatus(StatusReserva.CONFIRMADA);

        when(reservaRepository.findAllByOrderByDataCriacaoDesc())
                .thenReturn(List.of(reserva2, reserva1));

        // Act
        List<ReservaResponse> response = reservaService.listarTodas();

        // Assert
        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals(2L, response.get(0).getId());
        assertEquals(1L, response.get(1).getId());

        verify(reservaRepository).findAllByOrderByDataCriacaoDesc();
    }

    @Test
    void naoDeveBuscarReservaQuandoNaoExiste() {

        // Arrange
        when(reservaRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(
                ReservaNaoEncontradaException.class,
                () -> reservaService.buscarPorId(999L)
        );

        verify(reservaRepository).findById(999L);
    }

    @Test
    void devePermitirClienteConsultarSuaPropriaReserva() {
        //Arrange
        cliente.setId(1L);
        veiculo.setId(1L);

        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setCliente(cliente);
        reserva.setVeiculo(veiculo);

        autenticarCliente(cliente);

        when(reservaRepository.findById(1L))
            .thenReturn(Optional.of(reserva));

        //Act
        ReservaResponse response = reservaService.buscarPorId(1L);

        assertNotNull(response);
        verify(reservaRepository).findById(1L);
    }

    @Test
    void deveListarSomenteReservasDoClienteAutenticado() {
        //Arrange
        cliente.setId(1L);

        autenticarCliente(cliente);

        when(reservaRepository.findAllByClienteIdOrderByDataCriacaoDesc(1L))
                .thenReturn(List.of());

        //Act
        List<ReservaResponse> response = reservaService.listarMinhasReservas();

        assertNotNull(response);
        assertTrue(response.isEmpty());

        verify(reservaRepository).findAllByClienteIdOrderByDataCriacaoDesc(1L);
    }

    @Test
    void deveRetornarSomenteReservasDoClienteAutenticado() {
        //Arrange
        cliente.setId(1L);
        veiculo.setId(1L);

        Reserva reserva1 =  new Reserva();
        reserva1.setId(1L);
        reserva1.setCliente(cliente);
        reserva1.setVeiculo(veiculo);
        reserva1.setDataRetirada(LocalDate.now().plusDays(5));
        reserva1.setDataDevolucao(LocalDate.now().plusDays(8));
        reserva1.setValorDiaria(new BigDecimal("200.00"));
        reserva1.setStatus(StatusReserva.PENDENTE);

        Reserva reserva2 =  new Reserva();
        reserva2.setId(2L);
        reserva2.setCliente(cliente);
        reserva2.setVeiculo(veiculo);
        reserva2.setDataRetirada(LocalDate.now().plusDays(10));
        reserva2.setDataDevolucao(LocalDate.now().plusDays(13));
        reserva2.setValorDiaria(new BigDecimal("200.00"));
        reserva2.setStatus(StatusReserva.PENDENTE);

        autenticarCliente(cliente);

        when(reservaRepository.findAllByClienteIdOrderByDataCriacaoDesc(1L))
                .thenReturn(List.of(reserva1, reserva2));

        //Act
        List<ReservaResponse> response = reservaService.listarMinhasReservas();

        //Assert
        assertNotNull(response);
        assertEquals(2, response.size());

        assertTrue(response.stream()
                .allMatch(reserva -> reserva.getClienteId().equals(1L)));

        verify(reservaRepository)
                .findAllByClienteIdOrderByDataCriacaoDesc(1L);
    }

    @Test
    void deveCadastrarReservaQuandoUsuarioNaoEstaAutenticado() {

        // Arrange
        var authentication = new UsernamePasswordAuthenticationToken(
                "joao@email.com",
                null
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        LocalDate retirada = LocalDate.now().plusDays(5);
        LocalDate devolucao = retirada.plusDays(3);

        when(clienteRepository.findById(1L))
                .thenReturn(Optional.of(cliente));

        when(veiculoRepository.findById(1L))
                .thenReturn(Optional.of(veiculo));

        when(reservaRepository
                .existsByVeiculoIdAndStatusInAndDataRetiradaLessThanAndDataDevolucaoGreaterThan(
                        eq(1L), any(), eq(devolucao), eq(retirada)))
                .thenReturn(false);

        when(reservaRepository.save(any(Reserva.class)))
                .thenAnswer(invocation -> {
                    Reserva reserva = invocation.getArgument(0);
                    reserva.setId(1L);
                    return reserva;
                });

        // Act
        ReservaResponse response = reservaService.cadastrar(
                1L,
                1L,
                retirada,
                devolucao
        );

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());

        verify(clienteRepository).findById(1L);
        verify(usuarioRepository, never()).findByEmail(any());
        verify(reservaRepository).save(any(Reserva.class));
    }

    @Test
    void naoDeveCadastrarReservaQuandoClienteAutenticadoEstaInativo() {

        cliente.setId(1L);
        cliente.setAtivo(false);

        autenticarCliente(cliente);

        assertThrows(
                ClienteInativoException.class,
                () -> reservaService.cadastrar(
                        999L,
                        1L,
                        LocalDate.now().plusDays(5),
                        LocalDate.now().plusDays(8)
                )
        );

        verify(usuarioRepository).findByEmail("joao@email.com");

        verifyNoInteractions(
                clienteRepository,
                veiculoRepository,
                reservaRepository
        );
    }

    @AfterEach
    void limparContextoSeguranca() {
        SecurityContextHolder.clearContext();
    }
}
