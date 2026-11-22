package com.lrcacambas.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.lrcacambas.data.local.entity.Recebimento;

import java.util.List;

@Dao
public interface RecebimentoDao {

    @Insert
    long inserir(Recebimento recebimento);

    @Update
    void atualizar(Recebimento recebimento);

    @Query("SELECT * FROM recebimentos WHERE idRecebimento = :id")
    Recebimento buscarPorId(Long id);

    @Query("SELECT * FROM recebimentos WHERE idLocacao = :idLocacao ORDER BY dataRecebimento DESC")
    List<Recebimento> listarPorLocacao(Long idLocacao);

    @Query("SELECT * FROM recebimentos ORDER BY dataRecebimento DESC, idRecebimento DESC")
    List<Recebimento> listarTodos();

    @Query("SELECT SUM(valorRecebido) FROM recebimentos WHERE idLocacao = :idLocacao")
    Long calcularTotalRecebido(Long idLocacao);

    @Query("SELECT COALESCE(SUM(valorRecebido), 0) FROM recebimentos")
    Long calcularTotalRecebidoGeral();

    @Query("SELECT COALESCE(SUM(descontoValor), 0) FROM recebimentos WHERE idLocacao = :idLocacao")
    Long calcularTotalDescontos(Long idLocacao);
}