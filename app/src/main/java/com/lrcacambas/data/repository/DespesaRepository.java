package com.lrcacambas.data.repository;

import com.lrcacambas.data.local.dao.DespesaDao;
import com.lrcacambas.data.local.entity.Despesa;
import com.lrcacambas.data.local.entity.CategoriaDespesa;

import java.util.List;

public class DespesaRepository {

    private final DespesaDao despesaDao;

    public DespesaRepository(DespesaDao despesaDao) {
        this.despesaDao = despesaDao;
    }

    public long inserir(Despesa despesa) {
        return despesaDao.inserir(despesa);
    }

    public void atualizar(Despesa despesa) {
        despesaDao.atualizar(despesa);
    }

    public Despesa buscarPorId(Long id) {
        return despesaDao.buscarPorId(id);
    }

    public List<Despesa> listarTodas() {
        return despesaDao.listarTodas();
    }

    public List<Despesa> listarPorCategoria(CategoriaDespesa categoria) {
        return despesaDao.listarPorCategoria(categoria);
    }

    public List<Despesa> listarAbastecimentos() {
        return despesaDao.listarAbastecimentos();
    }

    public Long calcularTotalPorCategoria(CategoriaDespesa categoria) {
        return despesaDao.calcularTotalPorCategoria(categoria);
    }

    public void excluir(Long id) {
        despesaDao.excluir(id);
    }
}