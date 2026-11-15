package com.lrcacambas.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.lrcacambas.data.local.entity.EnderecoCliente;

import java.util.List;

@Dao
public interface EnderecoClienteDao {

    @Insert
    long inserir(EnderecoCliente endereco);

    @Update
    void atualizar(EnderecoCliente endereco);

    @Delete
    void excluir(EnderecoCliente endereco);

    @Query("SELECT * FROM enderecos_cliente WHERE idEndereco = :id")
    EnderecoCliente buscarPorId(Long id);

    @Query("SELECT * FROM enderecos_cliente WHERE idCliente = :idCliente")
    List<EnderecoCliente> listarPorCliente(Long idCliente);
}