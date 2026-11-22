package br.com.aluguelcarros.service;

import br.com.aluguelcarros.dto.ClienteRequest;
import br.com.aluguelcarros.dto.ClienteResponse;
import br.com.aluguelcarros.entity.Cliente;
import br.com.aluguelcarros.exception.CpfClienteDuplicadoException;
import br.com.aluguelcarros.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    private static final String JA_EXISTE_CLIENTE_CADASTRADO_COM_ESTE_CPF = "Já existe um cliente cadastrado com esse CPF.";

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Transactional
    public ClienteResponse cadastrar(ClienteRequest request) {

        if (clienteRepository.existsByCpf(request.getCpf())) {
            throw new CpfClienteDuplicadoException(
                    JA_EXISTE_CLIENTE_CADASTRADO_COM_ESTE_CPF
            );
        }

        Cliente cliente = new Cliente();
        cliente.setNome(request.getNome());
        cliente.setCpf(request.getCpf());
        cliente.setEmail(request.getEmail());
        cliente.setTelefone(request.getTelefone());
        cliente.setDataNascimento(request.getDataNascimento());

        Cliente clienteSalvo = clienteRepository.save(cliente);

        return converterParaResponse(clienteSalvo);

    }

    private ClienteResponse converterParaResponse(Cliente cliente) {

        ClienteResponse response = new ClienteResponse();

        response.setId(cliente.getId());
        response.setNome(cliente.getNome());
        response.setCpf(cliente.getCpf());
        response.setEmail(cliente.getEmail());
        response.setTelefone(cliente.getTelefone());
        response.setDataNascimento(cliente.getDataNascimento());
        response.setAtivo(cliente.isAtivo());

        return response;
    }
}
