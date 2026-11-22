package com.lrcacambas.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.lrcacambas.data.local.entity.Cliente;

import java.util.List;

@Dao
public interface ClienteDao {

    @Insert
    long inserir(Cliente cliente);

    @Update
    void atualizar(Cliente cliente);

    @Query("SELECT * FROM clientes WHERE idCliente = :id")
    Cliente buscarPorId(Long id);

    @Query("SELECT * FROM clientes WHERE ativo = 1 ORDER BY nome ASC")
    List<Cliente> listarAtivos();

    @Query("SELECT * FROM clientes " +
            "WHERE ativo = 1 " +
            "AND (LOWER(nome) LIKE '%' || LOWER(:termo) || '%' " +
            "OR LOWER(apelido) LIKE '%' || LOWER(:termo) || '%')")
    List<Cliente> buscarPorNome(String termo);

    @Query("UPDATE clientes SET ativo = 0 WHERE idCliente = :id")
    void inativar(Long id);
}