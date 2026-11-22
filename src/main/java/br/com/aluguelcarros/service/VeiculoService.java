package br.com.aluguelcarros.service;

import br.com.aluguelcarros.dto.VeiculoRequest;
import br.com.aluguelcarros.dto.VeiculoResponse;
import br.com.aluguelcarros.entity.CategoriaVeiculo;
import br.com.aluguelcarros.entity.StatusVeiculo;
import br.com.aluguelcarros.entity.Veiculo;
import br.com.aluguelcarros.exception.*;
import br.com.aluguelcarros.repository.CategoriaVeiculoRepository;
import br.com.aluguelcarros.repository.VeiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VeiculoService {

    private final VeiculoRepository veiculoRepository;
    private final CategoriaVeiculoRepository categoriaVeiculoRepository;

    private static final String VEICULO_NAO_ENCONTRADO = "Veiculo não encontrado com o ID: ";
    private static final String CATEGORIA_NAO_ENCONTRADA = "Categoria não encontrada com o ID: ";
    private static final String VEICULO_NAO_PODE_CADASTRADO_EM_CATEGORIA_INATIVA = "Não foi possivel cadastrar um veiculo em uma categoria inativa.";
    private static final String VEICULO_CADASTRADO_COM_ESSA_PLACA = "Já existe um veiculo cadastrado com essa placa.";
    private static final String OUTRO_VEICULO_CADASTRADO_COM_ESSA_PLACA = "Já existe outro veiculo cadastrado com essa placa.";
    private static final String VEICULO_INATIVO_NAO_PODE_SER_ATUALIZADO = "Não é possivel atualizar um veiculo inativo.";
    private static final String VEICULO_JA_INATIVO = "Este veiculo já está inativo.";

    public VeiculoService(
            VeiculoRepository veiculoRepository,
            CategoriaVeiculoRepository categoriaVeiculoRepository) {
        this.veiculoRepository = veiculoRepository;
        this.categoriaVeiculoRepository = categoriaVeiculoRepository;
    }

    @Transactional
    public VeiculoResponse cadastrar(VeiculoRequest request) {

        validarPlacaDisponivel(request.getPlaca());

        CategoriaVeiculo categoria = buscarCategoriaAtiva(request.getCategoriaId());

        Veiculo veiculo = new Veiculo();

        veiculo.setCategoria(categoria);
        veiculo.setMarca(request.getMarca());
        veiculo.setPlaca(request.getPlaca());
        veiculo.setModelo(request.getModelo());
        veiculo.setAnoFabricacao(request.getAnoFabricacao());
        veiculo.setCor(request.getCor());
        veiculo.setQuilometragem(request.getQuilometragem());
        veiculo.setValorDiaria(request.getValorDiaria());

        Veiculo veiculoSalvo = veiculoRepository.save(veiculo);

        return converterParaResponse(veiculoSalvo);
    }

    @Transactional
    public VeiculoResponse atualizar(Long id, VeiculoRequest request) {

        Veiculo veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> new VeiculoNaoEncontradoException(
                        VEICULO_NAO_ENCONTRADO + id
                ));

        validarVeiculoAtivo(veiculo);
        validarPlacaDisponivel(request.getPlaca(), id);

        CategoriaVeiculo categoria = buscarCategoriaAtiva(request.getCategoriaId());

        veiculo.setCategoria(categoria);
        veiculo.setMarca(request.getMarca());
        veiculo.setModelo(request.getModelo());
        veiculo.setAnoFabricacao(request.getAnoFabricacao());
        veiculo.setPlaca(request.getPlaca());
        veiculo.setCor(request.getCor());
        veiculo.setQuilometragem(request.getQuilometragem());
        veiculo.setValorDiaria(request.getValorDiaria());

        Veiculo veiculoAtualizado = veiculoRepository.save(veiculo);

        return converterParaResponse(veiculoAtualizado);

    }

    @Transactional(readOnly = true)
    public List<VeiculoResponse> listarTodos() {
        return veiculoRepository.findAllByStatusNot(StatusVeiculo.INATIVO)
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public VeiculoResponse buscarPorId(Long id) {
        Veiculo veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> new VeiculoNaoEncontradoException(
                        VEICULO_NAO_ENCONTRADO + id
                ));
        return converterParaResponse(veiculo);
    }

    @Transactional
    public void excluir(Long id) {

        Veiculo veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> new VeiculoNaoEncontradoException(
                        VEICULO_NAO_ENCONTRADO + id
                ));

        validarVeiculoNaoInativo(veiculo);
        veiculo.setStatus(StatusVeiculo.INATIVO);

        veiculoRepository.save(veiculo);
    }

    private CategoriaVeiculo buscarCategoriaAtiva(Long categoriaId) {
        CategoriaVeiculo categoria = categoriaVeiculoRepository
                .findById(categoriaId)
                .orElseThrow(() -> new CategoriaNaoEncontradaException(
                        CATEGORIA_NAO_ENCONTRADA + categoriaId
                ));

        if (!categoria.isAtiva()) {
            throw new CategoriaInativaException(
                    VEICULO_NAO_PODE_CADASTRADO_EM_CATEGORIA_INATIVA
            );
        }

        return categoria;
    }

    private void validarPlacaDisponivel(String placa) {
        if (veiculoRepository.existsByPlaca(placa)) {
            throw new PlacaVeiculoDuplicadaException(
                    VEICULO_CADASTRADO_COM_ESSA_PLACA
            );
        }
    }

    private void validarPlacaDisponivel(String placa, Long veiculoId) {
        if (veiculoRepository.existsByPlacaAndIdNot(placa, veiculoId)) {
            throw new PlacaVeiculoDuplicadaException(
                    OUTRO_VEICULO_CADASTRADO_COM_ESSA_PLACA
            );
        }
    }

    private void validarVeiculoAtivo(Veiculo veiculo) {
        if (veiculo.getStatus() == StatusVeiculo.INATIVO) {
            throw new VeiculoInativoException(
                    VEICULO_INATIVO_NAO_PODE_SER_ATUALIZADO
            );
        }
    }

    private void validarVeiculoNaoInativo(Veiculo veiculo) {
        if (veiculo.getStatus() == StatusVeiculo.INATIVO) {
            throw new VeiculoInativoException(
                    VEICULO_JA_INATIVO
            );
        }
    }

    private VeiculoResponse converterParaResponse(Veiculo veiculo) {
        VeiculoResponse response = new VeiculoResponse();

        response.setId(veiculo.getId());
        response.setCategoriaId(veiculo.getCategoria().getId());
        response.setCategoriaNome(veiculo.getCategoria().getNome());
        response.setMarca(veiculo.getMarca());
        response.setModelo(veiculo.getModelo());
        response.setAnoFabricacao(veiculo.getAnoFabricacao());
        response.setPlaca(veiculo.getPlaca());
        response.setCor(veiculo.getCor());
        response.setQuilometragem(veiculo.getQuilometragem());
        response.setValorDiaria(veiculo.getValorDiaria());
        response.setStatus(veiculo.getStatus());

        return response;

    }
}
