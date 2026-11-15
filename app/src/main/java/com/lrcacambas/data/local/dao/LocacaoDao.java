package com.lrcacambas.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.lrcacambas.data.local.entity.Locacao;
import com.lrcacambas.data.local.entity.StatusLocacao;

import java.util.List;

@Dao
public interface LocacaoDao {

    @Insert
    long inserir(Locacao locacao);

    @Update
    void atualizar(Locacao locacao);

    @Query("SELECT * FROM locacoes WHERE idLocacao = :id")
    Locacao buscarPorId(Long id);

    @Query("SELECT * FROM locacoes WHERE status = :status ORDER BY dataInicio DESC")
    List<Locacao> listarPorStatus(StatusLocacao status);

    @Query("SELECT * FROM locacoes WHERE idCliente = :idCliente ORDER BY dataInicio DESC")
    List<Locacao> listarPorCliente(Long idCliente);

    @Query("SELECT * FROM locacoes WHERE status = 'ATIVA' ORDER BY dataInicio DESC")
    List<Locacao> listarAtivas();

    @Query("UPDATE locacoes SET status = 'FINALIZADA', dataFinalizacao = :dataFinalizacao, valorTotalFinal = :valorTotalFinal, justificativaAjuste = :justificativaAjuste WHERE idLocacao = :id")
    void finalizar(
            Long id,
            String dataFinalizacao,
            Long valorTotalFinal,
            String justificativaAjuste
    );
}