package com.lrcacambas.data.repository;

import com.lrcacambas.data.local.dao.LocacaoDao;
import com.lrcacambas.data.local.entity.Locacao;
import com.lrcacambas.data.local.entity.StatusLocacao;

import java.util.List;

public class LocacaoRepository {

    private final LocacaoDao locacaoDao;

    public LocacaoRepository(LocacaoDao locacaoDao) {
        this.locacaoDao = locacaoDao;
    }

    public long inserir(Locacao locacao) {
        return locacaoDao.inserir(locacao);
    }

    public void atualizar(Locacao locacao) {
        locacaoDao.atualizar(locacao);
    }

    public Locacao buscarPorId(Long id) {
        return locacaoDao.buscarPorId(id);
    }

    public List<Locacao> listarPorStatus(StatusLocacao status) {
        return locacaoDao.listarPorStatus(status);
    }

    public List<Locacao> listarPorCliente(Long idCliente) {
        return locacaoDao.listarPorCliente(idCliente);
    }

    public List<Locacao> listarAtivas() {
        return locacaoDao.listarAtivas();
    }

    public void finalizar(
            Long id,
            String dataFinalizacao,
            Long valorTotalFinal,
            String justificativaAjuste
    ) {
        locacaoDao.finalizar(
                id,
                dataFinalizacao,
                valorTotalFinal,
                justificativaAjuste
        );
    }
}