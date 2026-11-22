package br.com.aluguelcarros.repository;

import br.com.aluguelcarros.entity.StatusVeiculo;
import br.com.aluguelcarros.entity.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {

    boolean existsByPlaca(String placa);
    boolean existsByPlacaAndIdNot(String placa, Long id);
    List<Veiculo> findAllByStatusNot(StatusVeiculo status);
}
