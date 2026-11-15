package com.lrcacambas.data.repository;

import com.lrcacambas.data.local.dao.ClienteDao;
import com.lrcacambas.data.local.entity.Cliente;

import java.util.List;

public class ClienteRepository {

    private final ClienteDao clienteDao;

    public ClienteRepository(ClienteDao clienteDao) {
        this.clienteDao = clienteDao;
    }

    public long inserir(Cliente cliente) {
        return clienteDao.inserir(cliente);
    }

    public void atualizar(Cliente cliente) {
        clienteDao.atualizar(cliente);
    }

    public Cliente buscarPorId(Long id) {
        return clienteDao.buscarPorId(id);
    }

    public List<Cliente> listarAtivos() {
        return clienteDao.listarAtivos();
    }

    public List<Cliente> buscarPorNome(String nome) {
        return clienteDao.buscarPorNome(nome);
    }

    public void inativar(Long id) {
        clienteDao.inativar(id);
    }
}