package com.lrcacambas.domain.service;

import com.lrcacambas.data.local.entity.Cacamba;
import com.lrcacambas.data.local.entity.StatusCacamba;
import com.lrcacambas.data.repository.CacambaRepository;

import java.util.List;

public class CacambaService {

    private final CacambaRepository cacambaRepository;

    public CacambaService(CacambaRepository cacambaRepository) {
        this.cacambaRepository = cacambaRepository;
    }

    public long cadastrar() {
        Integer maiorCodigo = cacambaRepository.buscarMaiorCodigo();

        int proximoCodigo = (maiorCodigo == null) ? 1 : maiorCodigo + 1;

        Cacamba cacamba = new Cacamba();
        cacamba.setCodigo(proximoCodigo);
        cacamba.setStatus(StatusCacamba.DISPONIVEL);
        cacamba.setObservacaoStatus(null);

        return cacambaRepository.inserir(cacamba);
    }

    public Cacamba buscarPorId(Long id) {
        validarId(id);

        Cacamba cacamba = cacambaRepository.buscarPorId(id);

        if (cacamba == null) {
            throw new IllegalArgumentException("Caçamba não encontrada.");
        }

        return cacamba;
    }

    public List<Cacamba> listarTodas() {
        return cacambaRepository.listarTodas();
    }

    public List<Cacamba> listarPorStatus(StatusCacamba status) {
        if (status == null) {
            throw new IllegalArgumentException("Informe uma situação válida.");
        }

        return cacambaRepository.listarPorStatus(status);
    }

    public void alterarStatus(Long id, StatusCacamba novoStatus, String observacao) {
        validarId(id);

        if (novoStatus == null) {
            throw new IllegalArgumentException("Informe a nova situação da caçamba.");
        }

        Cacamba cacamba = buscarPorId(id);

        if (cacamba.getStatus() == StatusCacamba.EM_USO) {
            throw new IllegalArgumentException(
                    "Não é possível alterar manualmente a situação de uma caçamba em uso.");
        }

        if (novoStatus == StatusCacamba.EM_USO) {
            throw new IllegalArgumentException(
                    "A situação EM USO será definida automaticamente ao iniciar uma locação.");
        }

        if (novoStatus == StatusCacamba.INDISPONIVEL ||
                novoStatus == StatusCacamba.MANUTENCAO) {

            if (observacao == null || observacao.trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "Informe uma observação para essa situação.");
            }

            cacamba.setObservacaoStatus(observacao.trim());

        } else {
            cacamba.setObservacaoStatus(null);
        }

        cacamba.setStatus(novoStatus);

        cacambaRepository.atualizar(cacamba);
    }

    private void validarId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "Identificador da caçamba inválido.");
        }
    }

    public Cacamba buscarPorCodigo(Integer codigo) {
        if (codigo == null || codigo <= 0) {
            throw new IllegalArgumentException(
                    "Código da caçamba inválido.");
        }

        Cacamba cacamba = cacambaRepository.buscarPorCodigo(codigo);

        if (cacamba == null) {
            throw new IllegalArgumentException(
                    "Caçamba não encontrada.");
        }

        return cacamba;
    }
}