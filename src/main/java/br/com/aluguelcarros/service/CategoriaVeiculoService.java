package br.com.aluguelcarros.service;

import br.com.aluguelcarros.dto.CategoriaVeiculoResponse;
import br.com.aluguelcarros.exception.CategoriaDuplicadaException;
import br.com.aluguelcarros.dto.CategoriaVeiculoRequest;
import br.com.aluguelcarros.entity.CategoriaVeiculo;
import br.com.aluguelcarros.repository.CategoriaVeiculoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaVeiculoService {

    private final CategoriaVeiculoRepository categoriaVeiculoRepository;

    private static final String JA_EXISTE_CATEGORIA_COM_ESSE_NOME = "Já existe uma categoria com esse nome.";

    public CategoriaVeiculoService(CategoriaVeiculoRepository categoriaVeiculoRepository) {
        this.categoriaVeiculoRepository = categoriaVeiculoRepository;
    }

    public List<CategoriaVeiculoResponse> listarTodas() {
        return this.categoriaVeiculoRepository.findAll()
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    public CategoriaVeiculoResponse cadastrar(CategoriaVeiculoRequest request) {
        if (categoriaVeiculoRepository.existsByNome(request.getNome())) {
            throw new CategoriaDuplicadaException(
                    JA_EXISTE_CATEGORIA_COM_ESSE_NOME
            );
        }

        CategoriaVeiculo categoria = new CategoriaVeiculo();
        categoria.setNome(request.getNome());
        categoria.setDescricao(request.getDescricao());

        CategoriaVeiculo categoriaSalva = categoriaVeiculoRepository.save(categoria);

        return converterParaResponse(categoriaSalva);
    }

    private CategoriaVeiculoResponse converterParaResponse(CategoriaVeiculo categoria) {

        CategoriaVeiculoResponse response = new CategoriaVeiculoResponse();
        response.setId(categoria.getId());
        response.setNome(categoria.getNome());
        response.setDescricao(categoria.getDescricao());
        response.setAtiva(categoria.isAtiva());
        return response;
    }
}
