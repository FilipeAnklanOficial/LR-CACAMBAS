package com.lrcacambas.domain.service;

import com.lrcacambas.data.local.database.AppDatabase;
import com.lrcacambas.data.local.entity.Locacao;
import com.lrcacambas.data.local.entity.Recebimento;
import com.lrcacambas.data.local.entity.StatusLocacao;
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
        this.locacaoService = locacaoService;
    }

    public long registrar(Recebimento recebimento) {
        validarRecebimento(recebimento);

        Locacao locacao = locacaoRepository.buscarPorId(recebimento.getIdLocacao());

        if (locacao == null) {
            throw new IllegalArgumentException("Locação não encontrada.");
        }

        long valorRecebido = recebimento.getValorBruto();
        long desconto = recebimento.getDescontoValor();

        // O recebimento e o desconto juntos não podem ultrapassar a dívida.
        long saldoPendente = calcularSaldoPendente(recebimento.getIdLocacao());

        if (valorRecebido + desconto > saldoPendente) {
            throw new IllegalArgumentException(
                    "O recebimento somado ao desconto não pode ultrapassar o saldo pendente."
            );
        }

        // O valor recebido é exatamente o valor informado no campo bruto.
        recebimento.setValorRecebido(valorRecebido);

        if (recebimento.getDataRecebimento() == null) {
            recebimento.setDataRecebimento(LocalDate.now());
        }

        return database.runInTransaction(() -> recebimentoRepository.inserir(recebimento));
    }

    public double calcularPercentualDesconto(Long valorRecebido, Long desconto) {

        if (valorRecebido == null || valorRecebido < 0 ||
                desconto == null || desconto < 0 ||
                valorRecebido + desconto <= 0) {
            throw new IllegalArgumentException(
                    "Valores informados para o desconto são inválidos.");
        }

        long valorTotalOperacao = valorRecebido + desconto;

        return (desconto.doubleValue() / valorTotalOperacao) * 100;
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

    public List<Recebimento> listarTodos() {
        return recebimentoRepository.listarTodos();
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

        if (locacao.getStatus() == StatusLocacao.FINALIZADA ||
                locacao.getStatus() == StatusLocacao.INADIMPLENTE) {

            valorTotal = locacao.getValorTotalFinal();

        } else {
            valorTotal = locacaoService.calcularValorTotal(idLocacao);
        }

        if (valorTotal == null) {
            throw new IllegalArgumentException(
                    "O valor total da locação não foi definido.");
        }

        long totalRecebido = calcularTotalRecebido(idLocacao);

        Long descontos = recebimentoRepository.calcularTotalDescontos(idLocacao);
        long totalDescontos = descontos != null ? descontos : 0L;

        return Math.max(0L, valorTotal - totalRecebido - totalDescontos);
    }

    private void validarIdLocacao(Long idLocacao) {

        if (idLocacao == null || idLocacao <= 0) {
            throw new IllegalArgumentException("Identificador da locação inválido.");
        }
    }

    public long calcularTotalRecebidoGeral() {
        Long total = recebimentoRepository.calcularTotalRecebidoGeral();

        return total != null ? total : 0L;
    }

    public long calcularTotalPendentePorStatus(StatusLocacao status) {
        List<Locacao> locacoes = locacaoRepository.listarPorStatus(status);

        long total = 0;

        for (Locacao locacao : locacoes) {
            total += calcularSaldoPendente(locacao.getIdLocacao());
        }

        return total;
    }

    public long calcularTotalAReceber() {

        long total = 0;

        List<Locacao> locacoesAtivas =
                locacaoRepository.listarPorStatus(StatusLocacao.ATIVA);

        List<Locacao> locacoesFinalizadas =
                locacaoRepository.listarPorStatus(StatusLocacao.FINALIZADA);

        for (Locacao locacao : locacoesAtivas) {
            total += calcularSaldoPendente(locacao.getIdLocacao());
        }

        for (Locacao locacao : locacoesFinalizadas) {
            total += calcularSaldoPendente(locacao.getIdLocacao());
        }

        return total;
    }
}