package br.com.aluguelcarros.repository;

import br.com.aluguelcarros.entity.*;
import org.junit.jupiter.api.Test;
import br.com.aluguelcarros.config.TestcontainersConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ReservaRepositoryTest {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private VeiculoRepository veiculoRepository;

    @Autowired
    private CategoriaVeiculoRepository categoriaVeiculoRepository;

    @Test
    void deveBuscarReservaDeUmClienteOrdenadaPorDataCriacao() {

        //1. Criar clientes
        Cliente cliente1 = criarCliente(
                "João Silva", "11111111111", "joao@email.com"
        );

        Cliente cliente2 = criarCliente(
                "Maria Souza", "22222222222", "maria@email.com"
        );

        // 2. Criar categoria
        CategoriaVeiculo categoria = new CategoriaVeiculo();
        categoria.setNome("SUV");
        categoria.setDescricao("Veiculos utilitarios esportivos");
        categoria.setAtiva(true);

        categoria = categoriaVeiculoRepository.saveAndFlush(categoria);

        // 3. Criar veiculos
        Veiculo veiculo1 = criarVeiculo(categoria, "ABC1D23");
        Veiculo veiculo2 = criarVeiculo(categoria, "ABC2D34");
        Veiculo veiculo3 = criarVeiculo(categoria, "ABC3D45");

        // 4. Criar reservas do primeiro cliente
        Reserva reserva1 = criarReserva(cliente1, veiculo1);
        ReflectionTestUtils.setField(
                reserva1, "dataCriacao",
                LocalDateTime.of(2026, 11, 20, 10, 0)
        );
        reservaRepository.saveAndFlush(reserva1);

        Reserva reserva2 = criarReserva(cliente1, veiculo2);
        ReflectionTestUtils.setField(
                reserva2, "dataCriacao",
                LocalDateTime.of(2026, 11, 20, 11, 0)
        );
        reservaRepository.saveAndFlush(reserva2);

        // 5. Criar Reserva do segundo cliente
        Reserva reserva3 = criarReserva(cliente2, veiculo3);
        reservaRepository.saveAndFlush(reserva3);

        // 6. Executar consulta
        List<Reserva> reservas =
                reservaRepository.findAllByClienteIdOrderByDataCriacaoDesc(
                        cliente1.getId());

        // 7. Verificar resultados
        assertThat(reservas).hasSize(2);

        assertThat(reservas)
                .extracting(reserva -> reserva.getCliente().getId())
                .containsOnly(cliente1.getId());

        assertThat(reservas.get(0).getDataCriacao())
        .isAfterOrEqualTo(reservas.get(1).getDataCriacao());

        assertThat(reservas.get(0).getId()).isEqualTo(reserva2.getId());


    }

    @Test
    void deveIdentificarReservaComPeriodoDeDatasSobreposto() {

        //1. Criar cliente
        Cliente cliente = criarCliente(
                "Carlos Oliveira",
                "33333333333",
                "carlos@rmail.com"
        );

        // 2. Criar categoria
        CategoriaVeiculo categoria = new CategoriaVeiculo();
        categoria.setNome("HATCH");
        categoria.setDescricao("Veiculos compactos");
        categoria.setAtiva(true);

        categoria = categoriaVeiculoRepository.saveAndFlush(categoria);

        // 3. Criar veiculo
        Veiculo veiculo = criarVeiculo(categoria, "DEF1G23");

        // 4. Criar reserva existente: dias 10 a 15
        Reserva reserva = criarReserva(cliente, veiculo);
        reserva.setDataRetirada(LocalDate.of(2026, 12, 10));
        reserva.setDataDevolucao(LocalDate.of(2026, 12, 15));

        reservaRepository.saveAndFlush(reserva);

        // 5. Consultar periodo que se sobrepôes: dias 12 a 17
        boolean existeConflito = reservaRepository
                .existsByVeiculoIdAndStatusInAndDataRetiradaLessThanAndDataDevolucaoGreaterThan(
                       veiculo.getId(),
                       List.of(reserva.getStatus()),
                       LocalDate.of(2026, 12, 17),
                       LocalDate.of(2026, 12, 12)
                );

        // 6. Verificar resultado
        assertThat(existeConflito).isTrue();
    }

    @Test
    void naoDeveIdentificarConflitoQuandoReservaComecaNaDataDeDevolucaoDaAnterior() {

        // 1. Criar cliente
        Cliente cliente = criarCliente(
                "Pedro Santos",
                "44444444444",
                "pedro@email.com"
        );

        // 2. Criar categoria
        CategoriaVeiculo categoria = new CategoriaVeiculo();
        categoria.setNome("SEDAN");
        categoria.setDescricao("Veiculos sedan");
        categoria.setAtiva(true);

        categoria = categoriaVeiculoRepository.saveAndFlush(categoria);

        //3. Criar veiculo
        Veiculo veiculo = criarVeiculo(categoria, "GHI1J23");

        //4. Criar reserva existente: dias 10 a 15
        Reserva reserva = criarReserva(cliente, veiculo);
        reserva.setDataRetirada(LocalDate.of(2026, 12, 10));
        reserva.setDataDevolucao(LocalDate.of(2026, 12, 15));

        reservaRepository.saveAndFlush(reserva);

        // 5. Consultar nova reserva: dias 15 a 18
        boolean existeConflito =
                reservaRepository.existsByVeiculoIdAndStatusInAndDataRetiradaLessThanAndDataDevolucaoGreaterThan(
                        veiculo.getId(),
                        List.of(reserva.getStatus()),
                        LocalDate.of(2026, 12, 18),
                        LocalDate.of(2026, 12, 15)
                );

        // 6. Não deve existir conflito
        assertThat(existeConflito).isFalse();

    }

    @Test
    void naoDeveIdentificarConflitoAoIgnorarPropriaReserva() {

        // 1. Criar cliente
        Cliente cliente = criarCliente(
                "Lucas Ferreira",
                "55555555555",
                "lucas@email.com"
        );

        // 2. Criar categoria
        CategoriaVeiculo categoria = new CategoriaVeiculo();
        categoria.setNome("PICKUP");
        categoria.setDescricao("Veiculos utilitarios");
        categoria.setAtiva(true);

        categoria = categoriaVeiculoRepository.saveAndFlush(categoria);

        // 3. Criar veiculo
        Veiculo veiculo = criarVeiculo(categoria, "JKL1M23");

        Reserva reserva = criarReserva(cliente, veiculo);
        reserva.setDataRetirada(LocalDate.of(2026, 12, 10));
        reserva.setDataDevolucao(LocalDate.of(2026, 12, 15));

        reserva = reservaRepository.saveAndFlush(reserva);

        // 5. Consultar o mesmo periodo, ignorando a própria reserva
        boolean existeConflito = reservaRepository
                .existsByVeiculoIdAndIdNotAndStatusInAndDataRetiradaLessThanAndDataDevolucaoGreaterThan(
                        veiculo.getId(),
                        reserva.getId(),
                        List.of(reserva.getStatus()),
                        LocalDate.of(2026, 12, 15),
                        LocalDate.of(2026, 12, 10)
                );

        //6. A própria reserva não deve gerar conflito
        assertThat(existeConflito).isFalse();

    }


    @Test
    void deveIdentificarOutraReservaConflitanteAoIgnorarPropriaReserva() {

        // 1. Criar cliente
        Cliente cliente = criarCliente(
                "Marcos Lima",
                "66666666666",
                "marcos@email.com"
        );

        // 2. Criar categoria
        CategoriaVeiculo categoria = new CategoriaVeiculo();
        categoria.setNome("MINIVAN");
        categoria.setDescricao("Veiculos familiares");
        categoria.setAtiva(true);

        categoria = categoriaVeiculoRepository.saveAndFlush(categoria);

        // 3. Criar veículo
        Veiculo veiculo = criarVeiculo(categoria, "MNO1P23");

        // 4. Criar a reserva que será editada
        Reserva reservaOriginal = criarReserva(cliente, veiculo);
        reservaOriginal.setDataRetirada(LocalDate.of(2026, 12, 10));
        reservaOriginal.setDataDevolucao(LocalDate.of(2026, 12, 15));

        reservaOriginal = reservaRepository.saveAndFlush(reservaOriginal);

        // 5. Criar outra reserva conflitante para o mesmo veículo
        Reserva outraReserva = criarReserva(cliente, veiculo);
        outraReserva.setDataRetirada(LocalDate.of(2026, 12, 12));
        outraReserva.setDataDevolucao(LocalDate.of(2026, 12, 17));

        reservaRepository.saveAndFlush(outraReserva);

        // 6. Consultar o período, ignorando apenas a reserva original
        boolean existeConflito =
                reservaRepository
                        .existsByVeiculoIdAndIdNotAndStatusInAndDataRetiradaLessThanAndDataDevolucaoGreaterThan(
                                veiculo.getId(),
                                reservaOriginal.getId(),
                                List.of(reservaOriginal.getStatus()),
                                LocalDate.of(2026, 12, 15),
                                LocalDate.of(2026, 12, 10)
                        );

        // 7. A outra reserva deve ser identificada
        assertThat(existeConflito).isTrue();
    }


    @Test
    void naoDeveIdentificarConflitoComReservaCancelada() {

        // 1. Criar cliente
        Cliente cliente = criarCliente(
                "André Costa",
                "77777777777",
                "andre@email.com"
        );

        // 2. Criar categoria
        CategoriaVeiculo categoria = new CategoriaVeiculo();
        categoria.setNome("CONVERSIVEL");
        categoria.setDescricao("Veiculos conversiveis");
        categoria.setAtiva(true);

        categoria = categoriaVeiculoRepository.saveAndFlush(categoria);

        // 3. Criar veículo
        Veiculo veiculo = criarVeiculo(categoria, "PQR1S23");

        // 4. Criar reserva cancelada
        Reserva reserva = criarReserva(cliente, veiculo);
        reserva.setDataRetirada(LocalDate.of(2026, 12, 10));
        reserva.setDataDevolucao(LocalDate.of(2026, 12, 15));
        reserva.setStatus(StatusReserva.CANCELADA);

        reservaRepository.saveAndFlush(reserva);

        // 5. Consultar período sobreposto usando status bloqueantes
        List<StatusReserva> statusBloqueantes = List.of(
                StatusReserva.PENDENTE,
                StatusReserva.CONFIRMADA
        );

        boolean existeConflito =
                reservaRepository
                        .existsByVeiculoIdAndStatusInAndDataRetiradaLessThanAndDataDevolucaoGreaterThan(
                                veiculo.getId(),
                                statusBloqueantes,
                                LocalDate.of(2026, 12, 17),
                                LocalDate.of(2026, 12, 12)
                        );

        // 6. Reserva cancelada não deve bloquear o veículo
        assertThat(existeConflito).isFalse();
    }


    @Test
    void naoDeveIdentificarConflitoComReservaConcluida() {

        // 1. Criar cliente
        Cliente cliente = criarCliente(
                "Felipe Almeida",
                "88888888888",
                "felipe@email.com"
        );

        // 2. Criar categoria
        CategoriaVeiculo categoria = new CategoriaVeiculo();
        categoria.setNome("ELETRICO");
        categoria.setDescricao("Veiculos eletricos");
        categoria.setAtiva(true);

        categoria = categoriaVeiculoRepository.saveAndFlush(categoria);

        // 3. Criar veículo
        Veiculo veiculo = criarVeiculo(categoria, "STU1V23");

        // 4. Criar reserva concluída
        Reserva reserva = criarReserva(cliente, veiculo);
        reserva.setDataRetirada(LocalDate.of(2026, 12, 10));
        reserva.setDataDevolucao(LocalDate.of(2026, 12, 15));
        reserva.setStatus(StatusReserva.CONCLUIDA);

        reservaRepository.saveAndFlush(reserva);

        // 5. Consultar período sobreposto
        List<StatusReserva> statusBloqueantes = List.of(
                StatusReserva.PENDENTE,
                StatusReserva.CONFIRMADA
        );

        boolean existeConflito =
                reservaRepository
                        .existsByVeiculoIdAndStatusInAndDataRetiradaLessThanAndDataDevolucaoGreaterThan(
                                veiculo.getId(),
                                statusBloqueantes,
                                LocalDate.of(2026, 12, 17),
                                LocalDate.of(2026, 12, 12)
                        );

        // 6. Reserva concluída não deve bloquear o veículo
        assertThat(existeConflito).isFalse();
    }


    @Test
    void deveIdentificarConflitoComReservaConfirmada() {

        // 1. Criar cliente
        Cliente cliente = criarCliente(
                "Roberto Alves",
                "99999999999",
                "roberto@email.com"
        );

        // 2. Criar categoria
        CategoriaVeiculo categoria = new CategoriaVeiculo();
        categoria.setNome("ESPORTIVO");
        categoria.setDescricao("Veiculos esportivos");
        categoria.setAtiva(true);

        categoria = categoriaVeiculoRepository.saveAndFlush(categoria);

        // 3. Criar veículo
        Veiculo veiculo = criarVeiculo(categoria, "VWX1Y23");

        // 4. Criar reserva confirmada
        Reserva reserva = criarReserva(cliente, veiculo);
        reserva.setDataRetirada(LocalDate.of(2026, 12, 10));
        reserva.setDataDevolucao(LocalDate.of(2026, 12, 15));
        reserva.setStatus(StatusReserva.CONFIRMADA);

        reservaRepository.saveAndFlush(reserva);

        // 5. Consultar período sobreposto
        List<StatusReserva> statusBloqueantes = List.of(
                StatusReserva.PENDENTE,
                StatusReserva.CONFIRMADA
        );

        boolean existeConflito =
                reservaRepository
                        .existsByVeiculoIdAndStatusInAndDataRetiradaLessThanAndDataDevolucaoGreaterThan(
                                veiculo.getId(),
                                statusBloqueantes,
                                LocalDate.of(2026, 12, 17),
                                LocalDate.of(2026, 12, 12)
                        );

        // 6. Reserva confirmada deve bloquear o veículo
        assertThat(existeConflito).isTrue();
    }

    @Test
    void deveIdentificarConflitoComReservaPendente() {

        // Arrange
        Cliente cliente = criarCliente(
                "Ana Costa",
                "10101010101",
                "ana@email.com"
        );

        CategoriaVeiculo categoria = new CategoriaVeiculo();
        categoria.setNome("COMPACTO");
        categoria.setDescricao("Veiculos compactos");
        categoria.setAtiva(true);

        categoria = categoriaVeiculoRepository.saveAndFlush(categoria);

        Veiculo veiculo = criarVeiculo(categoria, "ABC9Z99");

        Reserva reserva = criarReserva(cliente, veiculo);
        reserva.setDataRetirada(LocalDate.of(2026, 12, 10));
        reserva.setDataDevolucao(LocalDate.of(2026, 12, 15));
        reserva.setStatus(StatusReserva.PENDENTE);

        reservaRepository.saveAndFlush(reserva);

        List<StatusReserva> statusBloqueantes = List.of(
                StatusReserva.PENDENTE,
                StatusReserva.CONFIRMADA
        );

        // Act
        boolean existeConflito =
                reservaRepository
                        .existsByVeiculoIdAndStatusInAndDataRetiradaLessThanAndDataDevolucaoGreaterThan(
                                veiculo.getId(),
                                statusBloqueantes,
                                LocalDate.of(2026, 12, 17),
                                LocalDate.of(2026, 12, 12)
                        );

        // Assert
        assertThat(existeConflito).isTrue();
    }


    @Test
    void deveBuscarTodasAsReservasOrdenadasPorDataCriacaoDesc() {

        Cliente cliente = criarCliente(
                "Ana Souza",
                "12345678901",
                "ana@email.com"
        );

        CategoriaVeiculo categoria = new CategoriaVeiculo();
        categoria.setNome("HATCH");
        categoria.setDescricao("Veiculos compactos");
        categoria.setAtiva(true);

        categoria = categoriaVeiculoRepository.saveAndFlush(categoria);

        Veiculo veiculo1 = criarVeiculo(categoria, "AAA1B23");
        Veiculo veiculo2 = criarVeiculo(categoria, "BBB2C34");

        Reserva reserva1 = criarReserva(cliente, veiculo1);
        ReflectionTestUtils.setField(
                reserva1, "dataCriacao",
                LocalDateTime.of(2026, 11, 20, 10, 0)
        );
        reservaRepository.saveAndFlush(reserva1);

        Reserva reserva2 = criarReserva(cliente, veiculo2);
        ReflectionTestUtils.setField(
                reserva2, "dataCriacao",
                LocalDateTime.of(2026, 11, 20, 11, 0)
        );
        reservaRepository.saveAndFlush(reserva2);

        List<Reserva> reservas =
                reservaRepository.findAllByOrderByDataCriacaoDesc();

        assertThat(reservas).hasSize(2);

        assertThat(reservas.get(0).getDataCriacao())
                .isAfterOrEqualTo(reservas.get(1).getDataCriacao());

        assertThat(reservas.get(0).getId())
                .isEqualTo(reserva2.getId());
    }


    private Cliente criarCliente(String nome, String cpf, String email) {
        Cliente cliente = new Cliente();
        cliente.setNome(nome);
        cliente.setCpf(cpf);
        cliente.setEmail(email);
        cliente.setTelefone("83999999999");
        cliente.setAtivo(true);

        return clienteRepository.saveAndFlush(cliente);
    }

    private Veiculo criarVeiculo(CategoriaVeiculo categoria, String placa) {
        Veiculo veiculo = new Veiculo();
        veiculo.setCategoria(categoria);
        veiculo.setMarca("Toyota");
        veiculo.setModelo("Corolla");
        veiculo.setAnoFabricacao(2025);
        veiculo.setPlaca(placa);
        veiculo.setCor("Prata");
        veiculo.setQuilometragem(10000);
        veiculo.setValorDiaria(new BigDecimal("150.00"));

        return veiculoRepository.saveAndFlush(veiculo);
    }

    private Reserva criarReserva(Cliente cliente, Veiculo veiculo) {
        Reserva reserva = new Reserva();
        reserva.setCliente(cliente);
        reserva.setVeiculo(veiculo);
        reserva.setDataRetirada(LocalDate.now().plusDays(1));
        reserva.setDataDevolucao(LocalDate.now().plusDays(3));
        reserva.setValorDiaria(new BigDecimal("150.00"));

        return reserva;
    }
}
