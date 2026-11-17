package com.lrcacambas.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.lrcacambas.data.local.entity.MovimentacaoCacamba;

import java.util.List;

@Dao
public interface MovimentacaoCacambaDao {

    @Insert
    long inserir(MovimentacaoCacamba movimentacao);

    @Update
    void atualizar(MovimentacaoCacamba movimentacao);

    @Query("SELECT * FROM movimentacoes_cacamba WHERE idMovimentacao = :id")
    MovimentacaoCacamba buscarPorId(Long id);

    @Query("SELECT * FROM movimentacoes_cacamba WHERE idLocacao = :idLocacao ORDER BY dataEntrega ASC")
    List<MovimentacaoCacamba> listarPorLocacao(Long idLocacao);

    @Query("SELECT * FROM movimentacoes_cacamba WHERE idCacamba = :idCacamba ORDER BY dataEntrega DESC")
    List<MovimentacaoCacamba> listarPorCacamba(Long idCacamba);

    @Query("SELECT * FROM movimentacoes_cacamba " +
            "WHERE idLocacao = :idLocacao " +
            "AND dataRetirada IS NULL LIMIT 1")
    MovimentacaoCacamba buscarMovimentacaoAtivaPorLocacao(Long idLocacao);

    @Query("SELECT * FROM movimentacoes_cacamba " +
            "WHERE idCacamba = :idCacamba " +
            "AND dataRetirada IS NULL LIMIT 1")
    MovimentacaoCacamba buscarMovimentacaoAtivaPorCacamba(Long idCacamba);

    @Query("SELECT COUNT(*) FROM movimentacoes_cacamba " +
            "WHERE idCacamba = :idCacamba " +
            "AND dataRetirada IS NULL")
    int contarMovimentacoesAtivasPorCacamba(Long idCacamba);
}