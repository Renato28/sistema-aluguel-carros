package br.com.aluguelcarros.repository;

import br.com.aluguelcarros.entity.CategoriaVeiculo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaVeiculoRepository extends JpaRepository<CategoriaVeiculo, Long> {

    boolean existsByNome(String nome);
}
