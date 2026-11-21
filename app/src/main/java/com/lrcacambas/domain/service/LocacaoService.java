package com.lrcacambas.domain.service;

import com.lrcacambas.data.local.database.AppDatabase;
import com.lrcacambas.data.local.entity.Cacamba;
import com.lrcacambas.data.local.entity.Cliente;
import com.lrcacambas.data.local.entity.EnderecoCliente;
import com.lrcacambas.data.local.entity.Locacao;
import com.lrcacambas.data.local.entity.MovimentacaoCacamba;
import com.lrcacambas.data.local.entity.StatusCacamba;
import com.lrcacambas.data.local.entity.StatusLocacao;
import com.lrcacambas.data.repository.CacambaRepository;
import com.lrcacambas.data.repository.ClienteRepository;
import com.lrcacambas.data.repository.EnderecoClienteRepository;
import com.lrcacambas.data.repository.LocacaoRepository;
import com.lrcacambas.data.repository.MovimentacaoCacambaRepository;
import com.lrcacambas.data.local.entity.MotivoTroca;

import java.time.LocalDate;
import java.util.List;

public class LocacaoService {

    private final AppDatabase database;
    private final LocacaoRepository locacaoRepository;
    private final ClienteRepository clienteRepository;
    private final EnderecoClienteRepository enderecoRepository;
    private final CacambaRepository cacambaRepository;
    private final MovimentacaoCacambaRepository movimentacaoRepository;

    public LocacaoService(
            AppDatabase database,
            LocacaoRepository locacaoRepository,
            ClienteRepository clienteRepository,
            EnderecoClienteRepository enderecoRepository,
            CacambaRepository cacambaRepository,
            MovimentacaoCacambaRepository movimentacaoRepository) {

        this.database = database;
        this.locacaoRepository = locacaoRepository;
        this.clienteRepository = clienteRepository;
        this.enderecoRepository = enderecoRepository;
        this.cacambaRepository = cacambaRepository;
        this.movimentacaoRepository = movimentacaoRepository;
    }

    public long abrirLocacao(Locacao locacao, Integer codigoCacamba) {

        validarLocacao(locacao);

        Cliente cliente = clienteRepository.buscarPorId(
                locacao.getIdCliente());

        if (cliente == null || !cliente.isAtivo()) {
            throw new IllegalArgumentException(
                    "Cliente não encontrado ou inativo.");
        }

        prepararEndereco(locacao, cliente);

        if (locacao.getDataInicio() == null) {
            locacao.setDataInicio(LocalDate.now());
        }

        locacao.setStatus(StatusLocacao.ATIVA);
        locacao.setValorTotalFinal(null);
        locacao.setJustificativaAjuste(null);
        locacao.setDataFinalizacao(null);
        locacao.setValorTotalCalculado(null);

        return database.runInTransaction(() -> {

            // Revalida a caçamba dentro da transação
            Cacamba cacamba = cacambaRepository.buscarPorCodigo(codigoCacamba);

            if (cacamba == null) {
                throw new IllegalArgumentException("Caçamba não encontrada.");
            }

            if (cacamba.getStatus() != StatusCacamba.DISPONIVEL) {
                throw new IllegalArgumentException(
                        "A caçamba selecionada não está disponível.");
            }

            // Verifica se já existe uma movimentação ativa
            int movimentacoesAtivas =
                    movimentacaoRepository.contarMovimentacoesAtivasPorCacamba(
                            cacamba.getIdCacamba());

            if (movimentacoesAtivas > 0) {
                throw new IllegalArgumentException(
                        "Essa caçamba já está vinculada a uma locação.");
            }

            long idLocacao = locacaoRepository.inserir(locacao);

            MovimentacaoCacamba movimentacao =
                    new MovimentacaoCacamba();

            movimentacao.setIdLocacao(idLocacao);
            movimentacao.setIdCacamba(cacamba.getIdCacamba());
            movimentacao.setDataEntrega(locacao.getDataInicio());
            movimentacao.setDataRetirada(null);
            movimentacao.setMotivoTroca(null);
            movimentacao.setGeraCobranca(false);
            movimentacao.setValorAdicional(0L);
            movimentacao.setObservacao(null);

            movimentacaoRepository.inserir(movimentacao);

            cacamba.setStatus(StatusCacamba.EM_USO);
            cacamba.setObservacaoStatus(null);

            cacambaRepository.atualizar(cacamba);

            return idLocacao;
        });
    }

    public Locacao buscarPorId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID da locação inválido.");
        }

        return locacaoRepository.buscarPorId(id);
    }

    public void trocarCacamba(
            Long idLocacao,
            Integer codigoNovaCacamba,
            MotivoTroca motivo,
            Long valorAdicional,
            String observacao) {

        if (idLocacao == null || idLocacao <= 0) {
            throw new IllegalArgumentException("Locação inválida.");
        }

        if (codigoNovaCacamba == null || codigoNovaCacamba <= 0) {
            throw new IllegalArgumentException("Informe o código da nova caçamba.");
        }

        if (motivo == null) {
            throw new IllegalArgumentException("Informe o motivo da troca.");
        }

        if (valorAdicional == null || valorAdicional < 0) {
            throw new IllegalArgumentException(
                    "Informe um valor adicional válido.");
        }

        database.runInTransaction(() -> {

            // Verifica se a locação existe e está ativa
            Locacao locacao = locacaoRepository.buscarPorId(idLocacao);

            if (locacao == null || locacao.getStatus() != StatusLocacao.ATIVA) {
                throw new IllegalArgumentException(
                        "A locação não existe ou não está ativa.");
            }

            // Busca a movimentação atual da locação
            MovimentacaoCacamba movimentacaoAtual =
                    movimentacaoRepository.buscarMovimentacaoAtivaPorLocacao(idLocacao);

            if (movimentacaoAtual == null) {
                throw new IllegalArgumentException(
                        "Não foi encontrada uma caçamba ativa nessa locação.");
            }

            // Busca a nova caçamba pelo código
            Cacamba novaCacamba =
                    cacambaRepository.buscarPorCodigo(codigoNovaCacamba);

            if (novaCacamba == null) {
                throw new IllegalArgumentException(
                        "Caçamba substituta não encontrada.");
            }

            if (novaCacamba.getIdCacamba().equals(
                    movimentacaoAtual.getIdCacamba())) {
                throw new IllegalArgumentException(
                        "A caçamba selecionada já está nessa locação.");
            }

            if (novaCacamba.getStatus() != StatusCacamba.DISPONIVEL) {
                throw new IllegalArgumentException(
                        "A caçamba substituta não está disponível.");
            }

            // Verifica se a nova caçamba já possui movimentação ativa
            int movimentacoesAtivas =
                    movimentacaoRepository.contarMovimentacoesAtivasPorCacamba(
                            novaCacamba.getIdCacamba());

            if (movimentacoesAtivas > 0) {
                throw new IllegalArgumentException(
                        "Essa caçamba já está vinculada a outra locação.");
            }

            // Verifica se a caçamba atual existe
            Cacamba cacambaAtual = cacambaRepository.buscarPorId(
                    movimentacaoAtual.getIdCacamba());

            if (cacambaAtual == null ||
                    cacambaAtual.getStatus() != StatusCacamba.EM_USO) {
                throw new IllegalArgumentException(
                        "A situação da caçamba atual está inconsistente.");
            }

            LocalDate dataTroca = LocalDate.now();

            // Utiliza o valor adicional informado pelo usuário
            boolean geraCobranca = motivo == MotivoTroca.CHEIA;

            // Encerra a movimentação da caçamba retirada
            movimentacaoAtual.setDataRetirada(dataTroca);
            movimentacaoAtual.setMotivoTroca(motivo);
            movimentacaoAtual.setGeraCobranca(geraCobranca);
            movimentacaoAtual.setValorAdicional(valorAdicional);

            if (observacao != null && !observacao.trim().isEmpty()) {
                movimentacaoAtual.setObservacao(observacao.trim());
            }

            movimentacaoRepository.atualizar(movimentacaoAtual);

            // Libera a caçamba retirada
            cacambaAtual.setStatus(StatusCacamba.DISPONIVEL);
            cacambaAtual.setObservacaoStatus(null);

            cacambaRepository.atualizar(cacambaAtual);

            // Coloca a nova caçamba em uso
            novaCacamba.setStatus(StatusCacamba.EM_USO);
            novaCacamba.setObservacaoStatus(null);

            cacambaRepository.atualizar(novaCacamba);

            // Registra a movimentação da nova caçamba
            MovimentacaoCacamba novaMovimentacao =
                    new MovimentacaoCacamba();

            novaMovimentacao.setIdLocacao(idLocacao);
            novaMovimentacao.setIdCacamba(novaCacamba.getIdCacamba());
            novaMovimentacao.setDataEntrega(dataTroca);
            novaMovimentacao.setDataRetirada(null);
            novaMovimentacao.setMotivoTroca(null);
            novaMovimentacao.setGeraCobranca(false);
            novaMovimentacao.setValorAdicional(0L);
            novaMovimentacao.setObservacao(null);

            movimentacaoRepository.inserir(novaMovimentacao);
        });
    }

    private void validarLocacao(Locacao locacao) {

        if (locacao == null) {
            throw new IllegalArgumentException(
                    "Os dados da locação não foram informados.");
        }

        if (locacao.getIdCliente() == null ||
                locacao.getIdCliente() <= 0) {
            throw new IllegalArgumentException(
                    "Selecione um cliente.");
        }

        if (locacao.getValorInicial() == null ||
                locacao.getValorInicial() <= 0) {
            throw new IllegalArgumentException(
                    "Informe um valor inicial válido para a locação.");
        }
    }

    private void prepararEndereco(Locacao locacao, Cliente cliente) {

        if (locacao.getIdEndereco() != null) {

            EnderecoCliente endereco =
                    enderecoRepository.buscarPorId(locacao.getIdEndereco());

            if (endereco == null ||
                    !endereco.getIdCliente().equals(cliente.getIdCliente())) {
                throw new IllegalArgumentException(
                        "O endereço selecionado não pertence a esse cliente.");
            }

            locacao.setLogradouro(endereco.getLogradouro());
            locacao.setNumero(endereco.getNumero());
            locacao.setBairro(endereco.getBairro());
            locacao.setCidade(endereco.getCidade());
            locacao.setComplemento(endereco.getComplemento());
            locacao.setReferencia(endereco.getReferencia());

        } else {

            if (locacao.getLogradouro() == null ||
                    locacao.getLogradouro().trim().isEmpty() ||
                    locacao.getNumero() == null ||
                    locacao.getNumero().trim().isEmpty() ||
                    locacao.getBairro() == null ||
                    locacao.getBairro().trim().isEmpty() ||
                    locacao.getCidade() == null ||
                    locacao.getCidade().trim().isEmpty()) {

                throw new IllegalArgumentException(
                        "Informe o endereço da locação.");
            }

            locacao.setLogradouro(locacao.getLogradouro().trim());
            locacao.setBairro(locacao.getBairro().trim());
            locacao.setCidade(locacao.getCidade().trim());
        }
    }

    public List<Locacao> listarAtivas() {
        return locacaoRepository.listarAtivas();
    }

    public List<MovimentacaoCacamba> listarMovimentacoesPorLocacao(
            Long idLocacao
    ) {
        if (idLocacao == null || idLocacao <= 0) {
            throw new IllegalArgumentException("Locação inválida.");
        }

        return movimentacaoRepository.listarPorLocacao(idLocacao);
    }

    public long calcularValorTotal(Long idLocacao) {

        if (idLocacao == null || idLocacao <= 0) {
            throw new IllegalArgumentException("Locação inválida.");
        }

        Locacao locacao = locacaoRepository.buscarPorId(idLocacao);

        if (locacao == null) {
            throw new IllegalArgumentException("Locação não encontrada.");
        }

        long total = locacao.getValorInicial();

        List<MovimentacaoCacamba> movimentacoes =
                movimentacaoRepository.listarPorLocacao(idLocacao);

        for (MovimentacaoCacamba movimentacao : movimentacoes) {

            if (movimentacao.isGeraCobranca()) {
                Long adicional = movimentacao.getValorAdicional();

                if (adicional != null) {
                    total += adicional;
                }
            }
        }

        return total;
    }

    public void finalizarLocacao(Long idLocacao, Long valorInformado, String justificativa) {

        if (idLocacao == null || idLocacao <= 0) {
            throw new IllegalArgumentException("Locação inválida.");
        }

        database.runInTransaction(() -> {

            Locacao locacao = locacaoRepository.buscarPorId(idLocacao);

            if (locacao == null || locacao.getStatus() != StatusLocacao.ATIVA) {
                throw new IllegalArgumentException(
                        "Locação não encontrada ou já finalizada.");
            }

            MovimentacaoCacamba movimentacao =
                    movimentacaoRepository.buscarMovimentacaoAtivaPorLocacao(idLocacao);

            if (movimentacao == null) {
                throw new IllegalArgumentException(
                        "Não foi encontrada uma caçamba ativa nessa locação.");
            }

            Cacamba cacamba = cacambaRepository.buscarPorId(
                    movimentacao.getIdCacamba());

            if (cacamba == null || cacamba.getStatus() != StatusCacamba.EM_USO) {
                throw new IllegalArgumentException(
                        "A situação da caçamba está inconsistente.");
            }

            long valorCalculado = calcularValorTotal(idLocacao);

            long valorFinal = valorInformado == null
                    ? valorCalculado
                    : valorInformado;

            if (valorFinal < 0) {
                throw new IllegalArgumentException(
                        "O valor final não pode ser negativo.");
            }

            String justificativaFinal = null;

            if (valorFinal != valorCalculado) {

                if (justificativa == null || justificativa.trim().isEmpty()) {
                    throw new IllegalArgumentException(
                            "Informe uma justificativa para alterar o valor total.");
                }

                justificativaFinal = justificativa.trim();
            }

            LocalDate dataFinalizacao = LocalDate.now();

            movimentacao.setDataRetirada(dataFinalizacao);
            movimentacaoRepository.atualizar(movimentacao);

            cacamba.setStatus(StatusCacamba.DISPONIVEL);
            cacamba.setObservacaoStatus(null);
            cacambaRepository.atualizar(cacamba);

            locacao.setStatus(StatusLocacao.FINALIZADA);
            locacao.setDataFinalizacao(dataFinalizacao);
            locacao.setValorTotalFinal(valorFinal);
            locacao.setJustificativaAjuste(justificativaFinal);
            locacao.setValorTotalCalculado(valorCalculado);

            locacaoRepository.atualizar(locacao);
        });
    }

    public void marcarComoInadimplente(Long idLocacao) {

        if (idLocacao == null || idLocacao <= 0) {
            throw new IllegalArgumentException("Locação inválida.");
        }

        database.runInTransaction(() -> {

            Locacao locacao = locacaoRepository.buscarPorId(idLocacao);

            if (locacao == null) {
                throw new IllegalArgumentException("Locação não encontrada.");
            }

            if (locacao.getStatus() != StatusLocacao.FINALIZADA) {
                throw new IllegalArgumentException(
                        "Somente locações finalizadas podem ser marcadas como inadimplentes."
                );
            }

            locacao.setStatus(StatusLocacao.INADIMPLENTE);

            locacaoRepository.atualizar(locacao);
        });
    }

}