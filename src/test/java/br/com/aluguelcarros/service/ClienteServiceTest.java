package br.com.aluguelcarros.service;

import br.com.aluguelcarros.dto.ClienteRequest;
import br.com.aluguelcarros.dto.ClienteResponse;
import br.com.aluguelcarros.entity.Cliente;
import br.com.aluguelcarros.exception.CpfClienteDuplicadoException;
import br.com.aluguelcarros.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    private ClienteService clienteService;

    @BeforeEach
    void setUp() {
        clienteService = new ClienteService(clienteRepository);
    }

    @Test
    void deveCadastrarClienteComSucesso() {

        ClienteRequest request = new ClienteRequest();
        request.setNome("João da Silva");
        request.setCpf("12345678901");
        request.setEmail("joao@email.com");
        request.setTelefone("83999999999");
        request.setDataNascimento(LocalDate.of(1990, 5, 10));

        when(clienteRepository.existsByCpf("12345678901"))
                .thenReturn(false);

        when(clienteRepository.save(any(Cliente.class)))
                .thenAnswer(invocation -> {
                    Cliente cliente = invocation.getArgument(0);
                    cliente.setId(1L);
                    return cliente;
                });

        ClienteResponse response = clienteService.cadastrar(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("João da Silva",  response.getNome());
        assertEquals("12345678901", response.getCpf());
        assertEquals("joao@email.com", response.getEmail());
        assertEquals("83999999999", response.getTelefone());
        assertEquals(LocalDate.of(1990, 5 , 10), response.getDataNascimento());
        assertTrue(response.isAtivo());

        verify(clienteRepository).existsByCpf("12345678901");
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    void naoDeveCadastrarClienteComCpfDuplicado() {

        ClienteRequest request = new ClienteRequest();
        request.setNome("João da Silva");
        request.setCpf("12345678901");
        request.setEmail("joao@email.com");
        request.setTelefone("83999999999");
        request.setDataNascimento(LocalDate.of(1990, 5 , 10));

        when(clienteRepository.existsByCpf("12345678901"))
                .thenReturn(true);

        CpfClienteDuplicadoException exception = assertThrows(
                CpfClienteDuplicadoException.class,
                () -> clienteService.cadastrar(request)
        );

        assertEquals(
                "Já existe um cliente cadastrado com esse CPF.",
                exception.getMessage()
        );

        verify(clienteRepository).existsByCpf("12345678901");
        verify(clienteRepository, never()).save(any(Cliente.class));
    }
}
