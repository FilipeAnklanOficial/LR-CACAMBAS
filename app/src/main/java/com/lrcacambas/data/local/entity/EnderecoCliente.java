package com.lrcacambas.data.local.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "enderecos_cliente",
        foreignKeys = {
                @ForeignKey(
                        entity = Cliente.class,
                        parentColumns = "idCliente",
                        childColumns = "idCliente",
                        onDelete = ForeignKey.RESTRICT
                )
        },
        indices = {@Index(value = {"idCliente"})}
)
public class EnderecoCliente {

    @PrimaryKey(autoGenerate = true)
    private Long idEndereco;

    private Long idCliente;
    private String descricao;
    private String logradouro;
    private String numero;
    private String bairro;
    private String cidade;
    private String complemento;
    private String referencia;

    public EnderecoCliente() {
    }

    public Long getIdEndereco() {
        return idEndereco;
    }

    public void setIdEndereco(Long idEndereco) {
        this.idEndereco = idEndereco;
    }

    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }
}