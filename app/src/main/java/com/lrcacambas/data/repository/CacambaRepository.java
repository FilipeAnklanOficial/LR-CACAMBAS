package com.lrcacambas.data.repository;

import com.lrcacambas.data.local.dao.CacambaDao;
import com.lrcacambas.data.local.entity.Cacamba;
import com.lrcacambas.data.local.entity.StatusCacamba;

import java.util.List;

public class CacambaRepository {

    private final CacambaDao cacambaDao;

    public CacambaRepository(CacambaDao cacambaDao) {
        this.cacambaDao = cacambaDao;
    }

    public long inserir(Cacamba cacamba) {
        return cacambaDao.inserir(cacamba);
    }

    public void atualizar(Cacamba cacamba) {
        cacambaDao.atualizar(cacamba);
    }

    public void excluir(Cacamba cacamba) {
        cacambaDao.excluir(cacamba);
    }

    public Cacamba buscarPorId(Long id) {
        return cacambaDao.buscarPorId(id);
    }

    public List<Cacamba> listarTodas() {
        return cacambaDao.listarTodas();
    }

    public List<Cacamba> listarPorStatus(StatusCacamba status) {
        return cacambaDao.listarPorStatus(status);
    }

    public Integer buscarMaiorCodigo() {
        return cacambaDao.buscarMaiorCodigo();
    }

    public Cacamba buscarPorCodigo(Integer codigo) {
        return cacambaDao.buscarPorCodigo(codigo);
    }
}