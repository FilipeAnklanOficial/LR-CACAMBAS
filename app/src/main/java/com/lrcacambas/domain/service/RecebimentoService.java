package com.lrcacambas.domain.service;

import com.lrcacambas.data.local.database.AppDatabase;
import com.lrcacambas.data.local.entity.Locacao;
import com.lrcacambas.data.local.entity.Recebimento;
import com.lrcacambas.data.repository.LocacaoRepository;
import com.lrcacambas.data.repository.RecebimentoRepository;

import java.time.LocalDate;
import java.util.List;

public class RecebimentoService {

    private final AppDatabase database;
    private final RecebimentoRepository recebimentoRepository;
    private final LocacaoRepository locacaoRepository;
    private final LocacaoService locacaoService;

    public RecebimentoService(
            AppDatabase database,
            RecebimentoRepository recebimentoRepository,
            LocacaoRepository locacaoRepository,
            LocacaoService locacaoService) {

        this.database = database;
        this.recebimentoRepository = recebimentoRepository;
        this.locacaoRepository = locacaoRepository;
        this.locacaoService = locacaoService;;
    }

    public long registrar(Recebimento recebimento) {

        validarRecebimento(recebimento);

        Locacao locacao = locacaoRepository.buscarPorId(
                recebimento.getIdLocacao());

        if (locacao == null) {
            throw new IllegalArgumentException(
                    "Locação não encontrada.");
        }

        long valorBruto = recebimento.getValorBruto();
        long desconto = recebimento.getDescontoValor();

        if (desconto > valorBruto) {
            throw new IllegalArgumentException(
                    "O desconto não pode ser maior que o valor bruto.");
        }

        long valorLiquido = valorBruto - desconto;

        recebimento.setValorRecebido(valorLiquido);

        if (recebimento.getDataRecebimento() == null) {
            recebimento.setDataRecebimento(LocalDate.now());
        }

        return database.runInTransaction(() ->
                recebimentoRepository.inserir(recebimento)
        );
    }

    public double calcularPercentualDesconto(
            Long valorBruto, Long desconto) {

        if (valorBruto == null || valorBruto <= 0 ||
                desconto == null || desconto < 0) {
            throw new IllegalArgumentException(
                    "Valores informados para o desconto são inválidos.");
        }

        if (desconto > valorBruto) {
            throw new IllegalArgumentException(
                    "O desconto não pode ser maior que o valor bruto.");
        }

        return (desconto.doubleValue() / valorBruto.doubleValue()) * 100;
    }

    private void validarRecebimento(Recebimento recebimento) {

        if (recebimento == null) {
            throw new IllegalArgumentException(
                    "Os dados do recebimento não foram informados.");
        }

        if (recebimento.getIdLocacao() == null ||
                recebimento.getIdLocacao() <= 0) {
            throw new IllegalArgumentException(
                    "Selecione uma locação.");
        }

        if (recebimento.getValorBruto() == null ||
                recebimento.getValorBruto() <= 0) {
            throw new IllegalArgumentException(
                    "Informe um valor bruto válido.");
        }

        if (recebimento.getDescontoValor() == null ||
                recebimento.getDescontoValor() < 0) {
            throw new IllegalArgumentException(
                    "Informe um desconto válido.");
        }

        if (recebimento.getFormaPagamento() == null) {
            throw new IllegalArgumentException(
                    "Selecione a forma de pagamento.");
        }
    }

    public List<Recebimento> listarPorLocacao(Long idLocacao) {

        validarIdLocacao(idLocacao);

        Locacao locacao = locacaoRepository.buscarPorId(idLocacao);

        if (locacao == null) {
            throw new IllegalArgumentException("Locação não encontrada.");
        }

        return recebimentoRepository.listarPorLocacao(idLocacao);
    }

    public long calcularTotalRecebido(Long idLocacao) {

        validarIdLocacao(idLocacao);

        Locacao locacao = locacaoRepository.buscarPorId(idLocacao);

        if (locacao == null) {
            throw new IllegalArgumentException("Locação não encontrada.");
        }

        Long total = recebimentoRepository.calcularTotalRecebido(idLocacao);

        return total == null ? 0L : total;
    }

    public long calcularSaldoPendente(Long idLocacao) {

        validarIdLocacao(idLocacao);

        Locacao locacao = locacaoRepository.buscarPorId(idLocacao);

        if (locacao == null) {
            throw new IllegalArgumentException("Locação não encontrada.");
        }

        Long valorTotal;

        if (locacao.getStatus() == com.lrcacambas.data.local.entity.StatusLocacao.FINALIZADA) {
            valorTotal = locacao.getValorTotalFinal();
        } else {
            valorTotal = locacaoService.calcularValorTotal(idLocacao);
        }

        if (valorTotal == null) {
            throw new IllegalArgumentException(
                    "O valor total da locação não foi definido.");
        }

        long totalRecebido = calcularTotalRecebido(idLocacao);

        return Math.max(0L, valorTotal - totalRecebido);
    }

    private void validarIdLocacao(Long idLocacao) {

        if (idLocacao == null || idLocacao <= 0) {
            throw new IllegalArgumentException("Identificador da locação inválido.");
        }
    }
}