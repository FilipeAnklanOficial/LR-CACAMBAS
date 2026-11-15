package com.lrcacambas.domain.service;

import com.lrcacambas.data.local.entity.Cliente;
import com.lrcacambas.data.local.entity.EnderecoCliente;
import com.lrcacambas.data.repository.ClienteRepository;
import com.lrcacambas.data.repository.EnderecoClienteRepository;

import java.util.List;

public class EnderecoClienteService {

    private final EnderecoClienteRepository enderecoRepository;
    private final ClienteRepository clienteRepository;

    public EnderecoClienteService(
            EnderecoClienteRepository enderecoRepository,
            ClienteRepository clienteRepository) {

        this.enderecoRepository = enderecoRepository;
        this.clienteRepository = clienteRepository;
    }

    public long cadastrar(EnderecoCliente endereco) {
        validarEndereco(endereco);

        Cliente cliente = validarCliente(endereco.getIdCliente());

        List<EnderecoCliente> enderecos =
                enderecoRepository.listarPorCliente(cliente.getIdCliente());

        if (enderecos.size() >= 2) {
            throw new IllegalArgumentException(
                    "O cliente já possui o limite de dois endereços cadastrados.");
        }

        prepararEndereco(endereco);

        return enderecoRepository.inserir(endereco);
    }

    public void atualizar(EnderecoCliente endereco) {
        validarEndereco(endereco);

        if (endereco.getIdEndereco() == null) {
            throw new IllegalArgumentException(
                    "O endereço precisa possuir um identificador.");
        }

        EnderecoCliente enderecoExistente =
                enderecoRepository.buscarPorId(endereco.getIdEndereco());

        if (enderecoExistente == null) {
            throw new IllegalArgumentException(
                    "Endereço não encontrado.");
        }

        if (!enderecoExistente.getIdCliente().equals(endereco.getIdCliente())) {
            throw new IllegalArgumentException(
                    "Não é permitido transferir um endereço para outro cliente.");
        }

        validarCliente(endereco.getIdCliente());
        prepararEndereco(endereco);

        enderecoRepository.atualizar(endereco);
    }

    public EnderecoCliente buscarPorId(Long id) {
        validarId(id);

        return enderecoRepository.buscarPorId(id);
    }

    public List<EnderecoCliente> listarPorCliente(Long idCliente) {
        validarCliente(idCliente);

        return enderecoRepository.listarPorCliente(idCliente);
    }

    public void excluir(Long id) {
        validarId(id);

        EnderecoCliente endereco =
                enderecoRepository.buscarPorId(id);

        if (endereco == null) {
            throw new IllegalArgumentException(
                    "Endereço não encontrado.");
        }

        enderecoRepository.excluir(endereco);
    }

    private Cliente validarCliente(Long idCliente) {
        if (idCliente == null || idCliente <= 0) {
            throw new IllegalArgumentException(
                    "Identificador do cliente inválido.");
        }

        Cliente cliente = clienteRepository.buscarPorId(idCliente);

        if (cliente == null) {
            throw new IllegalArgumentException(
                    "Cliente não encontrado.");
        }

        if (!cliente.isAtivo()) {
            throw new IllegalArgumentException(
                    "Não é possível cadastrar ou alterar endereços de um cliente inativo.");
        }

        return cliente;
    }

    private void validarEndereco(EnderecoCliente endereco) {
        if (endereco == null) {
            throw new IllegalArgumentException(
                    "Os dados do endereço não foram informados.");
        }

        if (endereco.getLogradouro() == null ||
                endereco.getLogradouro().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "O logradouro é obrigatório.");
        }

        if (endereco.getBairro() == null ||
                endereco.getBairro().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "O bairro é obrigatório.");
        }

        if (endereco.getCidade() == null ||
                endereco.getCidade().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "A cidade é obrigatória.");
        }
    }

    private void prepararEndereco(EnderecoCliente endereco) {
        endereco.setLogradouro(endereco.getLogradouro().trim());
        endereco.setBairro(endereco.getBairro().trim());
        endereco.setCidade(endereco.getCidade().trim());

        if (endereco.getNumero() != null) {
            endereco.setNumero(endereco.getNumero().trim());
        }

        if (endereco.getComplemento() != null) {
            endereco.setComplemento(endereco.getComplemento().trim());
        }

        if (endereco.getReferencia() != null) {
            endereco.setReferencia(endereco.getReferencia().trim());
        }

        if (endereco.getDescricao() != null) {
            endereco.setDescricao(endereco.getDescricao().trim());
        }
    }

    private void validarId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "Identificador do endereço inválido.");
        }
    }
}