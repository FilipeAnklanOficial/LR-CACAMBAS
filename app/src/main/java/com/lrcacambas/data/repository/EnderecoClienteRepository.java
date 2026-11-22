package com.lrcacambas.data.repository;

import com.lrcacambas.data.local.dao.EnderecoClienteDao;
import com.lrcacambas.data.local.entity.EnderecoCliente;

import java.util.List;

public class EnderecoClienteRepository {

    private final EnderecoClienteDao enderecoClienteDao;

    public EnderecoClienteRepository(EnderecoClienteDao enderecoClienteDao) {
        this.enderecoClienteDao = enderecoClienteDao;
    }

    public long inserir(EnderecoCliente endereco) {
        return enderecoClienteDao.inserir(endereco);
    }

    public void atualizar(EnderecoCliente endereco) {
        enderecoClienteDao.atualizar(endereco);
    }

    public void excluir(EnderecoCliente endereco) {
        enderecoClienteDao.excluir(endereco);
    }

    public EnderecoCliente buscarPorId(Long id) {
        return enderecoClienteDao.buscarPorId(id);
    }

    public List<EnderecoCliente> listarPorCliente(Long idCliente) {
        return enderecoClienteDao.listarPorCliente(idCliente);
    }
}