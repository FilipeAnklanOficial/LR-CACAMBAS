package com.lrcacambas.domain.service;

import com.lrcacambas.data.local.entity.CategoriaDespesa;
import com.lrcacambas.data.local.entity.Despesa;
import com.lrcacambas.data.repository.DespesaRepository;

import java.time.LocalDate;
import java.util.List;

public class DespesaService {

    private final DespesaRepository despesaRepository;

    public DespesaService(DespesaRepository despesaRepository) {
        this.despesaRepository = despesaRepository;
    }

    public long cadastrar(Despesa despesa) {

        validarDespesa(despesa);

        prepararDespesa(despesa);

        return despesaRepository.inserir(despesa);
    }

    public void atualizar(Despesa despesa) {

        validarDespesa(despesa);

        if (despesa.getIdDespesa() == null) {
            throw new IllegalArgumentException(
                    "A despesa precisa possuir um identificador.");
        }

        Despesa existente = despesaRepository.buscarPorId(
                despesa.getIdDespesa());

        if (existente == null) {
            throw new IllegalArgumentException(
                    "Despesa não encontrada.");
        }

        prepararDespesa(despesa);

        despesaRepository.atualizar(despesa);
    }

    public Despesa buscarPorId(Long id) {

        validarId(id);

        Despesa despesa = despesaRepository.buscarPorId(id);

        if (despesa == null) {
            throw new IllegalArgumentException(
                    "Despesa não encontrada.");
        }

        return despesa;
    }

    public List<Despesa> listarTodas() {
        return despesaRepository.listarTodas();
    }

    public List<Despesa> listarPorCategoria(CategoriaDespesa categoria) {

        if (categoria == null) {
            throw new IllegalArgumentException(
                    "Informe uma categoria válida.");
        }

        return despesaRepository.listarPorCategoria(categoria);
    }

    public List<Despesa> listarAbastecimentos() {
        return despesaRepository.listarAbastecimentos();
    }

    public double calcularMediaHistorica() {

        List<Despesa> abastecimentos = despesaRepository.listarAbastecimentos();

        Long kmTanqueCompletoAnterior = null;
        double litrosAcumulados = 0;

        long totalKm = 0;
        double totalLitros = 0;

        for (Despesa abastecimento : abastecimentos) {

            if (kmTanqueCompletoAnterior == null) {

                if (Boolean.TRUE.equals(abastecimento.getTanqueCompleto())) {
                    kmTanqueCompletoAnterior = abastecimento.getQuilometragem();
                }

                continue;
            }

            litrosAcumulados += abastecimento.getLitros();

            if (Boolean.TRUE.equals(abastecimento.getTanqueCompleto())) {

                long distancia = abastecimento.getQuilometragem() - kmTanqueCompletoAnterior;

                if (distancia > 0 && litrosAcumulados > 0) {
                    totalKm += distancia;
                    totalLitros += litrosAcumulados;
                }

                kmTanqueCompletoAnterior = abastecimento.getQuilometragem();
                litrosAcumulados = 0;
            }
        }

        if (totalLitros == 0) {
            return 0;
        }

        return totalKm / totalLitros;
    }

    public long calcularTotalPorCategoria(CategoriaDespesa categoria) {

        if (categoria == null) {
            throw new IllegalArgumentException(
                    "Informe uma categoria válida.");
        }

        Long total = despesaRepository.calcularTotalPorCategoria(categoria);

        return total == null ? 0L : total;
    }

    public void excluir(Long id) {

        validarId(id);

        Despesa despesa = despesaRepository.buscarPorId(id);

        if (despesa == null) {
            throw new IllegalArgumentException(
                    "Despesa não encontrada.");
        }

        despesaRepository.excluir(id);
    }

    private void validarDespesa(Despesa despesa) {

        if (despesa == null) {
            throw new IllegalArgumentException(
                    "Os dados da despesa não foram informados.");
        }

        if (despesa.getCategoria() == null) {
            throw new IllegalArgumentException(
                    "Selecione a categoria da despesa.");
        }

        if (despesa.getDescricao() == null ||
                despesa.getDescricao().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Informe a descrição da despesa.");
        }

        if (despesa.getValor() == null || despesa.getValor() <= 0) {
            throw new IllegalArgumentException(
                    "Informe um valor válido para a despesa.");
        }

        if (despesa.getCategoria() == CategoriaDespesa.COMBUSTIVEL) {

            if (despesa.getLitros() == null || despesa.getLitros() <= 0) {
                throw new IllegalArgumentException(
                        "Informe uma quantidade válida de litros.");
            }

            if (despesa.getQuilometragem() == null || despesa.getQuilometragem() <= 0) {
                throw new IllegalArgumentException(
                        "Informe a quilometragem do caminhão.");
            }

            if (despesa.getTanqueCompleto() == null) {
                throw new IllegalArgumentException(
                        "Informe se o tanque foi completado.");
            }
        }
    }

    private void prepararDespesa(Despesa despesa) {

        despesa.setDescricao(despesa.getDescricao().trim());

        if (despesa.getDataDespesa() == null) {
            despesa.setDataDespesa(LocalDate.now());
        }

        if (despesa.getCategoria() != CategoriaDespesa.COMBUSTIVEL) {
            despesa.setLitros(null);
            despesa.setQuilometragem(null);
            despesa.setTanqueCompleto(null);
        }
    }

    private void validarId(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "Identificador da despesa inválido.");
        }
    }


}