package br.com.aluguelcarros.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UsuarioTest {

    @Test
    void deveCriarUsuarioComValoresIniciais() {
        Usuario usuario = new Usuario();

        assertNotNull(usuario);
        assertTrue(usuario.isAtivo());
    }

    @Test
    void deveDefinirEConsultarTodosOsAtributos() {
        Usuario usuario = new Usuario();
        Cliente cliente = new Cliente();

        usuario.setNome("João Silva");
        usuario.setEmail("joao@teste.com");
        usuario.setSenha("senha-criptografada");
        usuario.setPerfil(PerfilUsuario.CLIENTE);
        usuario.setAtivo(false);
        usuario.setCliente(cliente);

        assertEquals("João Silva", usuario.getNome());
        assertEquals("joao@teste.com", usuario.getEmail());
        assertEquals("senha-criptografada", usuario.getSenha());
        assertEquals(PerfilUsuario.CLIENTE, usuario.getPerfil());
        assertEquals(cliente, usuario.getCliente());
        assertFalse(usuario.isAtivo());

    }

    @Test
    void deveRetornarIdNuloQuandoUsuarioNaoEstiverPersistido() {
        Usuario usuario = new Usuario();

        assertNull(usuario.getId());
    }

}
