package com.lrcacambas.data.local.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.time.LocalDate;

@Entity(
        tableName = "locacoes",
        foreignKeys = {
                @ForeignKey(
                        entity = Cliente.class,
                        parentColumns = "idCliente",
                        childColumns = "idCliente",
                        onDelete = ForeignKey.RESTRICT
                ),
                @ForeignKey(
                        entity = EnderecoCliente.class,
                        parentColumns = "idEndereco",
                        childColumns = "idEndereco",
                        onDelete = ForeignKey.SET_NULL
                )
        },
        indices = {
                @Index(value = {"idCliente"}),
                @Index(value = {"idEndereco"})
        }
)
public class Locacao {

    @PrimaryKey(autoGenerate = true)
    private Long idLocacao;

    private Long idCliente;
    private Long idEndereco;

    private LocalDate dataInicio;
    private Long valorInicial;

    // Cópia dos dados do endereço no momento da locação
    private String logradouro;
    private String numero;
    private String bairro;
    private String cidade;
    private String complemento;
    private String referencia;

    private StatusLocacao status;

    private Long valorTotalFinal;
    private String justificativaAjuste;
    private LocalDate dataFinalizacao;
    private String observacoes;
    private Long valorTotalCalculado;

    public Locacao() {
    }

    public Long getIdLocacao() {
        return idLocacao;
    }

    public void setIdLocacao(Long idLocacao) {
        this.idLocacao = idLocacao;
    }

    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }

    public Long getIdEndereco() {
        return idEndereco;
    }

    public void setIdEndereco(Long idEndereco) {
        this.idEndereco = idEndereco;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public Long getValorInicial() {
        return valorInicial;
    }

    public void setValorInicial(Long valorInicial) {
        this.valorInicial = valorInicial;
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

    public StatusLocacao getStatus() {
        return status;
    }

    public void setStatus(StatusLocacao status) {
        this.status = status;
    }

    public Long getValorTotalFinal() {
        return valorTotalFinal;
    }

    public void setValorTotalFinal(Long valorTotalFinal) {
        this.valorTotalFinal = valorTotalFinal;
    }

    public String getJustificativaAjuste() {
        return justificativaAjuste;
    }

    public void setJustificativaAjuste(String justificativaAjuste) {
        this.justificativaAjuste = justificativaAjuste;
    }

    public LocalDate getDataFinalizacao() {
        return dataFinalizacao;
    }

    public void setDataFinalizacao(LocalDate dataFinalizacao) {
        this.dataFinalizacao = dataFinalizacao;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public Long getValorTotalCalculado() {
        return valorTotalCalculado;
    }

    public void setValorTotalCalculado(Long valorTotalCalculado) {
        this.valorTotalCalculado = valorTotalCalculado;
    }
}