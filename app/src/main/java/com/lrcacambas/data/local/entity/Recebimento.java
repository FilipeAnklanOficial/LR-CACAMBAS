package com.lrcacambas.data.local.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.time.LocalDate;

@Entity(
        tableName = "recebimentos",
        foreignKeys = {
                @ForeignKey(
                        entity = Locacao.class,
                        parentColumns = "idLocacao",
                        childColumns = "idLocacao",
                        onDelete = ForeignKey.RESTRICT
                )
        },
        indices = {
                @Index(value = {"idLocacao"})
        }
)
public class Recebimento {

    @PrimaryKey(autoGenerate = true)
    private Long idRecebimento;

    private Long idLocacao;

    private Long valorBruto;
    private Long descontoValor;
    private Long valorRecebido;

    private LocalDate dataRecebimento;
    private FormaPagamento formaPagamento;

    public Recebimento() {
    }

    public Long getIdRecebimento() {
        return idRecebimento;
    }

    public void setIdRecebimento(Long idRecebimento) {
        this.idRecebimento = idRecebimento;
    }

    public Long getIdLocacao() {
        return idLocacao;
    }

    public void setIdLocacao(Long idLocacao) {
        this.idLocacao = idLocacao;
    }

    public Long getValorBruto() {
        return valorBruto;
    }

    public void setValorBruto(Long valorBruto) {
        this.valorBruto = valorBruto;
    }

    public Long getDescontoValor() {
        return descontoValor;
    }

    public void setDescontoValor(Long descontoValor) {
        this.descontoValor = descontoValor;
    }

    public Long getValorRecebido() {
        return valorRecebido;
    }

    public void setValorRecebido(Long valorRecebido) {
        this.valorRecebido = valorRecebido;
    }

    public LocalDate getDataRecebimento() {
        return dataRecebimento;
    }

    public void setDataRecebimento(LocalDate dataRecebimento) {
        this.dataRecebimento = dataRecebimento;
    }

    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(FormaPagamento formaPagamento) {
        this.formaPagamento = formaPagamento;
    }
}