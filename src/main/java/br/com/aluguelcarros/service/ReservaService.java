package br.com.aluguelcarros.service;

import br.com.aluguelcarros.dto.ReservaResponse;
import br.com.aluguelcarros.entity.*;
import br.com.aluguelcarros.exception.*;
import br.com.aluguelcarros.repository.ClienteRepository;
import br.com.aluguelcarros.repository.ReservaRepository;
import br.com.aluguelcarros.repository.UsuarioRepository;
import br.com.aluguelcarros.repository.VeiculoRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final ClienteRepository clienteRepository;
    private final VeiculoRepository veiculoRepository;
    private final UsuarioRepository usuarioRepository;

    private static final String RESERVA_NAO_ENCONTRADA = "Reserva não encontrada com o ID: ";
    private static final String CLIENTE_NAO_ENCONTRADO = "Cliente não encontrado com o ID: ";
    private static final String VEICULO_NAO_ENCONTRADO = "Veiculo não encontrado com o ID: ";
    private static final String USUARIO_AUTENTICADO_NAO_ENCONTRADO = "Usuário autenticado não encontrado.";
    private static final String RESERVA_CANCELADA_NAO_PODE_SER_CONFIRMADA = "Não é possivel confirmar uma reserva cancelada.";
    private static final String RESERVA_CONCLUIDA_NAO_PODE_SER_CONFIRMADA = "Não é possivel confirmar uma reserva concluida.";
    private static final String RESERVA_JA_CONFIRMADA_NAO_PODE_SER_CONFIRMADA = "Esta reserva já está confirmada.";
    private static final String RESERVA_JA_CANCELADA = "Esta reserva já foi cancelada.";
    private static final String RESERVA_CONCLUIDA_NAO_PODE_SER_CANCELADA = "Não é possivel cancelar uma reserva concluida.";
    private static final String SOMENTE_RESERVAS_PENDENTES_PODEM_SER_ALTERADAS = "Somente reservas pendentes podem ser alteradas.";
    private static final String VEICULO_JA_POSSUI_RESERVA_PARA_PERIODO_INFORMADO = "O veiculo já possui uma reserva para o periodo informado.";
    private static final String RESERVA_NAO_PODE_SER_CRIADA_PARA_CLIENTE_INATIVO = "Não é possivel criar uma reserva para um cliente inativo.";
    private static final String RESERVA_NAO_PODE_SER_CRIADA_PARA_VEICULO_INATIVO = "Não é possivel criar uma reserva para um veiculo inativo.";
    private static final String VEICULO_NAO_DISPONIVEL_PARA_RESERVA = "o veiculo não está disponivel para reserva.";
    private static final String DATAS_DE_RETIRADA_E_DEVOLUCAO_SAO_OBRIGATORIAS = "As datas de retirada e devolução são obrigatórias.";
    private static final String DATA_RETIRADA_NAO_PODE_SER_ANTERIOR_A_DATA_ATUAL = "A data de retirada não pode ser anterior à data atual.";
    private static final String DATA_DEVOLUCAO_DEVE_SER_POSTERIOR_A_DATA_DE_RETIRADA = "A data de devolução deve ser posterior á data de retirada.";
    private static final String USUARIO_AUTENTICADO_NAO_POSSUI_CLIENTE_VINCULADO = "O usuário autenticado não possui um cliente vinculado.";

    public ReservaService(ReservaRepository reservaRepository,
                          ClienteRepository clienteRepository,

                          VeiculoRepository veiculoRepository,
                          UsuarioRepository usuarioRepository) {
        this.reservaRepository = reservaRepository;
        this.clienteRepository = clienteRepository;
        this.veiculoRepository = veiculoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public ReservaResponse cadastrar(
            Long clienteId,
            Long veiculoId,
            LocalDate dataRetirada,
            LocalDate dataDevolucao) {

        validarPeriodo(dataRetirada, dataDevolucao);

        Cliente cliente;

        if (usuarioAutenticadoEhCliente()) {
            cliente = validarClienteAtivo(buscarClienteAutenticado());
        } else {
            cliente = buscarClienteAtivo(clienteId);
        }

        Veiculo veiculo = buscarVeiculoAtivo(veiculoId);

        validarDisponibilidade(
                veiculoId,
                dataRetirada,
                dataDevolucao);

        Reserva reserva = new Reserva();
        reserva.setCliente(cliente);
        reserva.setVeiculo(veiculo);
        reserva.setDataRetirada(dataRetirada);
        reserva.setDataDevolucao(dataDevolucao);
        reserva.setValorDiaria(veiculo.getValorDiaria());

        Reserva reservaSalva = reservaRepository.save(reserva);

        return converterParaResponse(reservaSalva);
    }

    @Transactional(readOnly = true)
    public List<ReservaResponse> listarTodas() {

        return reservaRepository.findAllByOrderByDataCriacaoDesc()
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservaResponse> listarMinhasReservas() {
        Cliente cliente = buscarClienteAutenticado();

        return reservaRepository
                .findAllByClienteIdOrderByDataCriacaoDesc(cliente.getId())
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReservaResponse buscarPorId(Long id) {

        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ReservaNaoEncontradaException(
                        RESERVA_NAO_ENCONTRADA + id
                ));

        validarAcessoReserva(reserva);

        return converterParaResponse(reserva);
    }

    @Transactional
    public ReservaResponse confirmar(Long id) {

        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ReservaNaoEncontradaException(
                        RESERVA_NAO_ENCONTRADA + id
                ));

        if (reserva.getStatus() == StatusReserva.CANCELADA) {
            throw new ReservaNaoPodeSerConfirmadaException(
                    RESERVA_CANCELADA_NAO_PODE_SER_CONFIRMADA
            );
        }

        if (reserva.getStatus() == StatusReserva.CONCLUIDA) {
            throw new ReservaNaoPodeSerConfirmadaException(
                    RESERVA_CONCLUIDA_NAO_PODE_SER_CONFIRMADA
            );
        }

        if (reserva.getStatus() == StatusReserva.CONFIRMADA) {
            throw new ReservaNaoPodeSerConfirmadaException(
                    RESERVA_JA_CONFIRMADA_NAO_PODE_SER_CONFIRMADA
            );
        }

        reserva.setStatus(StatusReserva.CONFIRMADA);

        return  converterParaResponse(reserva);
    }

    @Transactional
    public ReservaResponse cancelar(Long id) {

        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ReservaNaoEncontradaException(
                        RESERVA_NAO_ENCONTRADA + id
                ));

        validarAcessoReserva(reserva);

        if (reserva.getStatus() == StatusReserva.CANCELADA) {
            throw new ReservaJaCanceladaException(
                    RESERVA_JA_CANCELADA
            );
        }

        if (reserva.getStatus() == StatusReserva.CONCLUIDA) {
            throw new ReservaNaoPodeSerCanceladaException(
                    RESERVA_CONCLUIDA_NAO_PODE_SER_CANCELADA
            );
        }

        reserva.setStatus(StatusReserva.CANCELADA);

        return converterParaResponse(reserva);
    }

    @Transactional
    public ReservaResponse atualizarPeriodo(
            Long id,
            LocalDate dataRetirada,
            LocalDate dataDevolucao) {

        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ReservaNaoEncontradaException(
                        RESERVA_NAO_ENCONTRADA + id
                ));

        validarAcessoReserva(reserva);

        if (reserva.getStatus() != StatusReserva.PENDENTE) {
            throw new ReservaNaoPodeSerAlteradaException(
                    SOMENTE_RESERVAS_PENDENTES_PODEM_SER_ALTERADAS
            );
        }

        validarPeriodo(dataRetirada, dataDevolucao);

        validarDisponibilidadeAtualizacao(
                reserva.getVeiculo().getId(),
                reserva.getId(),
                dataRetirada,
                dataDevolucao);

        reserva.setDataRetirada(dataRetirada);
        reserva.setDataDevolucao(dataDevolucao);

        return converterParaResponse(reserva);
    }

    private void validarDisponibilidade(
            Long veiculoId,
            LocalDate dataRetirada,
            LocalDate dataDevolucao) {

        List<StatusReserva> statusBloqueantes = List.of(StatusReserva.PENDENTE, StatusReserva.CONFIRMADA);

        boolean existeConflito = reservaRepository.
                existsByVeiculoIdAndStatusInAndDataRetiradaLessThanAndDataDevolucaoGreaterThan(
                        veiculoId,
                        statusBloqueantes,
                        dataDevolucao,
                        dataRetirada);
        if (existeConflito) {
            throw new VeiculoIndisponivelException(
                    VEICULO_JA_POSSUI_RESERVA_PARA_PERIODO_INFORMADO
            );
        }
    }

    private void validarDisponibilidadeAtualizacao(
            Long veiculoId,
            Long reservaId,
            LocalDate dataRetirada,
            LocalDate dataDevolucao) {

        List<StatusReserva> statusBloqueantes = List.of(
                StatusReserva.PENDENTE,
                StatusReserva.CONFIRMADA
        );

        boolean existeConflito = reservaRepository
                .existsByVeiculoIdAndIdNotAndStatusInAndDataRetiradaLessThanAndDataDevolucaoGreaterThan(
                    veiculoId,
                    reservaId,
                    statusBloqueantes,
                    dataDevolucao,
                    dataRetirada
                );
        if (existeConflito) {
            throw new VeiculoIndisponivelException(
                    VEICULO_JA_POSSUI_RESERVA_PARA_PERIODO_INFORMADO
            );
        }
    }

    private Cliente buscarClienteAtivo(Long clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ClienteNaoEncontradoException(
                        CLIENTE_NAO_ENCONTRADO + clienteId
                ));

        return validarClienteAtivo(cliente);
    }

    private Veiculo buscarVeiculoAtivo(Long veiculoId) {
        Veiculo veiculo = veiculoRepository.findById(veiculoId)
                .orElseThrow(() -> new VeiculoNaoEncontradoException(
                        VEICULO_NAO_ENCONTRADO + veiculoId
                ));

        if (veiculo.getStatus() == StatusVeiculo.INATIVO) {
            throw new VeiculoInativoException(
                    RESERVA_NAO_PODE_SER_CRIADA_PARA_VEICULO_INATIVO
            );
        }

        if (veiculo.getStatus() != StatusVeiculo.DISPONIVEL) {
            throw new VeiculoIndisponivelException(
                    VEICULO_NAO_DISPONIVEL_PARA_RESERVA
            );
        }

        return veiculo;
    }

    private void validarPeriodo(LocalDate dataRetirada, LocalDate dataDevolucao) {

        if (dataRetirada == null || dataDevolucao == null) {
            throw new PeriodoReservaInvalidoException(
                    DATAS_DE_RETIRADA_E_DEVOLUCAO_SAO_OBRIGATORIAS
            );
        }

        if (dataRetirada.isBefore(LocalDate.now(ZoneId.systemDefault()))) {
            throw new PeriodoReservaInvalidoException(
                    DATA_RETIRADA_NAO_PODE_SER_ANTERIOR_A_DATA_ATUAL
            );
        }

        if (!dataDevolucao.isAfter(dataRetirada)) {
            throw new PeriodoReservaInvalidoException(
                    DATA_DEVOLUCAO_DEVE_SER_POSTERIOR_A_DATA_DE_RETIRADA
            );
        }
    }

    private Cliente buscarClienteAutenticado() {

        String email = Objects.requireNonNull(SecurityContextHolder
                .getContext()
                .getAuthentication()).getName();

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(
                        USUARIO_AUTENTICADO_NAO_ENCONTRADO
                ));

        if (usuario.getCliente() == null) {
            throw new IllegalStateException(
                    USUARIO_AUTENTICADO_NAO_POSSUI_CLIENTE_VINCULADO
            );
        }

        return usuario.getCliente();

    }

    private boolean usuarioAutenticadoEhCliente() {

       var authentication = SecurityContextHolder.getContext()
               .getAuthentication();

       if (authentication == null || !authentication.isAuthenticated()) {
           return false;
       }

       return authentication.getAuthorities()
               .stream()
               .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"));
    }

    private void validarAcessoReserva(Reserva reserva) {

        if (!usuarioAutenticadoEhCliente()) {
            return;
        }

        Cliente clienteAutenticado = buscarClienteAutenticado();

        if (!clienteAutenticado.getId().equals(
                reserva.getCliente().getId())) {

            throw new AccessDeniedException(
                    "Você não tem permissão para acessar esta reserva."
            );
        }
    }

    private Cliente validarClienteAtivo(Cliente cliente) {
        if (!cliente.isAtivo()) {
            throw new ClienteInativoException(
                    RESERVA_NAO_PODE_SER_CRIADA_PARA_CLIENTE_INATIVO
            );
        }

        return cliente;
    }

    private ReservaResponse converterParaResponse(Reserva reserva) {

        ReservaResponse response = new ReservaResponse();

        response.setId(reserva.getId());

        response.setClienteId(reserva.getCliente().getId());
        response.setClienteNome(reserva.getCliente().getNome());

        response.setVeiculoId(reserva.getVeiculo().getId());
        response.setVeiculoDescricao(
                reserva.getVeiculo().getMarca() + " "
                        + reserva.getVeiculo().getModelo());
        response.setDataRetirada(reserva.getDataRetirada());
        response.setDataDevolucao(reserva.getDataDevolucao());
        response.setValorDiaria(reserva.getValorDiaria());
        response.setStatus(reserva.getStatus());
        response.setDataCriacao(reserva.getDataCriacao());

        return response;
    }
}
