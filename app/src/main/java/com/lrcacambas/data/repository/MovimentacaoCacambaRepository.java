package com.lrcacambas.data.repository;

import com.lrcacambas.data.local.dao.MovimentacaoCacambaDao;
import com.lrcacambas.data.local.entity.MovimentacaoCacamba;

import java.util.List;

public class MovimentacaoCacambaRepository {

    private final MovimentacaoCacambaDao movimentacaoCacambaDao;

    public MovimentacaoCacambaRepository(
            MovimentacaoCacambaDao movimentacaoCacambaDao
    ) {
        this.movimentacaoCacambaDao = movimentacaoCacambaDao;
    }

    public long inserir(MovimentacaoCacamba movimentacao) {
        return movimentacaoCacambaDao.inserir(movimentacao);
    }

    public void atualizar(MovimentacaoCacamba movimentacao) {
        movimentacaoCacambaDao.atualizar(movimentacao);
    }

    public MovimentacaoCacamba buscarPorId(Long id) {
        return movimentacaoCacambaDao.buscarPorId(id);
    }

    public List<MovimentacaoCacamba> listarPorLocacao(Long idLocacao) {
        return movimentacaoCacambaDao.listarPorLocacao(idLocacao);
    }

    public List<MovimentacaoCacamba> listarPorCacamba(Long idCacamba) {
        return movimentacaoCacambaDao.listarPorCacamba(idCacamba);
    }

    public MovimentacaoCacamba buscarMovimentacaoAtiva(Long idCacamba) {
        return movimentacaoCacambaDao.buscarMovimentacaoAtiva(idCacamba);
    }
}