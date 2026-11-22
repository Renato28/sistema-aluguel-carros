package br.com.aluguelcarros.repository;

import br.com.aluguelcarros.entity.Reserva;
import br.com.aluguelcarros.entity.StatusReserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    boolean existsByVeiculoIdAndStatusInAndDataRetiradaLessThanAndDataDevolucaoGreaterThan(
            Long veiculoId,
            List<StatusReserva> status,
            LocalDate dataDevolucao,
            LocalDate dataRetirada
    );

    boolean existsByVeiculoIdAndIdNotAndStatusInAndDataRetiradaLessThanAndDataDevolucaoGreaterThan(
            Long veiculoId,
            Long reservaId,
            List<StatusReserva> status,
            LocalDate dataDevolucao,
            LocalDate dataRetirada
    );

    List<Reserva> findAllByOrderByDataCriacaoDesc();

    List<Reserva> findAllByClienteIdOrderByDataCriacaoDesc(Long clienteId);
}
