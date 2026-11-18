package com.lrcacambas.domain.service;

import com.lrcacambas.data.local.entity.Cliente;
import com.lrcacambas.data.repository.ClienteRepository;

import java.util.List;

public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public long cadastrar(Cliente cliente) {

        validarCliente(cliente);

        cliente.setNome(cliente.getNome().trim());
        cliente.setTelefone(normalizarDigitos(cliente.getTelefone()));
        cliente.setAtivo(true);

        String documento = normalizarDigitos(cliente.getDocumento());

        cliente.setDocumento(
                documento.isEmpty() ? null : documento
        );

        if (cliente.getApelido() != null) {
            cliente.setApelido(cliente.getApelido().trim());

            if (cliente.getApelido().isEmpty()) {
                cliente.setApelido(null);
            }
        }

        return clienteRepository.inserir(cliente);
    }

    public void atualizar(Cliente cliente) {

        validarCliente(cliente);

        if (cliente.getIdCliente() == null) {
            throw new IllegalArgumentException(
                    "O cliente precisa possuir um identificador."
            );
        }

        cliente.setNome(cliente.getNome().trim());
        cliente.setTelefone(normalizarDigitos(cliente.getTelefone()));

        String documento = normalizarDigitos(cliente.getDocumento());

        cliente.setDocumento(
                documento.isEmpty() ? null : documento
        );

        if (cliente.getApelido() != null) {
            cliente.setApelido(cliente.getApelido().trim());

            if (cliente.getApelido().isEmpty()) {
                cliente.setApelido(null);
            }
        }

        cliente.setAtivo(true);
        clienteRepository.atualizar(cliente);
    }

    public Cliente buscarPorId(Long id) {
        validarId(id);
        return clienteRepository.buscarPorId(id);
    }

    public List<Cliente> listarAtivos() {
        return clienteRepository.listarAtivos();
    }

    public List<Cliente> buscarPorNome(String nome) {

        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Informe nome, sobrenome ou apelido para pesquisar."
            );
        }

        return clienteRepository.buscarPorNome(nome.trim());
    }

    public void inativar(Long id) {
        validarId(id);
        clienteRepository.inativar(id);
    }

    private void validarCliente(Cliente cliente) {

        if (cliente == null) {
            throw new IllegalArgumentException(
                    "Os dados do cliente não foram informados."
            );
        }

        if (cliente.getNome() == null ||
                cliente.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "O nome do cliente é obrigatório."
            );
        }

        if (cliente.getTelefone() == null ||
                normalizarDigitos(cliente.getTelefone()).isEmpty()) {
            throw new IllegalArgumentException(
                    "O telefone do cliente é obrigatório."
            );
        }
    }

    private void validarId(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "Identificador do cliente inválido."
            );
        }
    }

    private String normalizarDigitos(String valor) {

        if (valor == null) {
            return "";
        }

        return valor.replaceAll("\\D", "");
    }
}