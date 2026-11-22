package br.com.aluguelcarros.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class CategoriaVeiculoTest {

    @Test
    void deveCriarCategoriaComValoresIniciais() {
        CategoriaVeiculo categoria = new CategoriaVeiculo();

        assertTrue(categoria.isAtiva());
    }

    @Test
    void deveDefinirEConsultarTodosOsAtributos() {
        CategoriaVeiculo categoria = new CategoriaVeiculo();

        categoria.setId(1L);
        categoria.setNome("SUV");
        categoria.setDescricao("Veículos utilitários esportivos");
        categoria.setAtiva(true);

        assertEquals(1L, categoria.getId());
        assertEquals("SUV", categoria.getNome());
        assertEquals("Veículos utilitários esportivos", categoria.getDescricao());
        assertTrue(categoria.isAtiva());


    }
}
