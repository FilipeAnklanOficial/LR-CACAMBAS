package com.lrcacambas.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.lrcacambas.data.local.entity.Cacamba;
import com.lrcacambas.data.local.entity.StatusCacamba;

import java.util.List;

@Dao
public interface CacambaDao {

    @Insert
    long inserir(Cacamba cacamba);

    @Update
    void atualizar(Cacamba cacamba);

    @Query("SELECT * FROM cacambas WHERE idCacamba = :id")
    Cacamba buscarPorId(Long id);

    @Query("SELECT * FROM cacambas ORDER BY codigo ASC")
    List<Cacamba> listarTodas();

    @Query("SELECT * FROM cacambas WHERE status = :status ORDER BY codigo ASC")
    List<Cacamba> listarPorStatus(StatusCacamba status);

    @Query("SELECT MAX(codigo) FROM cacambas")
    Integer buscarMaiorCodigo();

    @Query("SELECT * FROM cacambas WHERE codigo = :codigo LIMIT 1")
    Cacamba buscarPorCodigo(Integer codigo);
}