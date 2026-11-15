package com.lrcacambas.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.lrcacambas.data.local.entity.Despesa;
import com.lrcacambas.data.local.entity.CategoriaDespesa;

import java.util.List;

@Dao
public interface DespesaDao {

    @Insert
    long inserir(Despesa despesa);

    @Update
    void atualizar(Despesa despesa);

    @Query("SELECT * FROM despesas WHERE idDespesa = :id")
    Despesa buscarPorId(Long id);

    @Query("SELECT * FROM despesas ORDER BY dataDespesa DESC")
    List<Despesa> listarTodas();

    @Query("SELECT * FROM despesas WHERE categoria = :categoria ORDER BY dataDespesa DESC")
    List<Despesa> listarPorCategoria(CategoriaDespesa categoria);

    @Query("SELECT SUM(valor) FROM despesas WHERE categoria = :categoria")
    Long calcularTotalPorCategoria(CategoriaDespesa categoria);

    @Query("DELETE FROM despesas WHERE idDespesa = :id")
    void excluir(Long id);
}