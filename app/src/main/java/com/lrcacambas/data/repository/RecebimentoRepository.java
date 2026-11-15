package com.lrcacambas.data.repository;

import com.lrcacambas.data.local.dao.RecebimentoDao;
import com.lrcacambas.data.local.entity.Recebimento;

import java.util.List;

public class RecebimentoRepository {

    private final RecebimentoDao recebimentoDao;

    public RecebimentoRepository(RecebimentoDao recebimentoDao) {
        this.recebimentoDao = recebimentoDao;
    }

    public long inserir(Recebimento recebimento) {
        return recebimentoDao.inserir(recebimento);
    }

    public void atualizar(Recebimento recebimento) {
        recebimentoDao.atualizar(recebimento);
    }

    public Recebimento buscarPorId(Long id) {
        return recebimentoDao.buscarPorId(id);
    }

    public List<Recebimento> listarPorLocacao(Long idLocacao) {
        return recebimentoDao.listarPorLocacao(idLocacao);
    }

    public Long calcularTotalRecebido(Long idLocacao) {
        return recebimentoDao.calcularTotalRecebido(idLocacao);
    }
}