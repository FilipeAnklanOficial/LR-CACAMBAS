package com.lrcacambas.data.local.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.time.LocalDate;

@Entity(
        tableName = "movimentacoes_cacamba",
        foreignKeys = {
                @ForeignKey(
                        entity = Locacao.class,
                        parentColumns = "idLocacao",
                        childColumns = "idLocacao",
                        onDelete = ForeignKey.RESTRICT
                ),
                @ForeignKey(
                        entity = Cacamba.class,
                        parentColumns = "idCacamba",
                        childColumns = "idCacamba",
                        onDelete = ForeignKey.RESTRICT
                )
        },
        indices = {
                @Index(value = {"idLocacao"}),
                @Index(value = {"idCacamba"})
        }
)
public class MovimentacaoCacamba {

    @PrimaryKey(autoGenerate = true)
    private Long idMovimentacao;

    private Long idLocacao;
    private Long idCacamba;

    private LocalDate dataEntrega;
    private LocalDate dataRetirada;

    private MotivoTroca motivoTroca;
    private boolean geraCobranca;
    private Long valorAdicional;
    private String observacao;

    public MovimentacaoCacamba() {
    }

    public Long getIdMovimentacao() {
        return idMovimentacao;
    }

    public void setIdMovimentacao(Long idMovimentacao) {
        this.idMovimentacao = idMovimentacao;
    }

    public Long getIdLocacao() {
        return idLocacao;
    }

    public void setIdLocacao(Long idLocacao) {
        this.idLocacao = idLocacao;
    }

    public Long getIdCacamba() {
        return idCacamba;
    }

    public void setIdCacamba(Long idCacamba) {
        this.idCacamba = idCacamba;
    }

    public LocalDate getDataEntrega() {
        return dataEntrega;
    }

    public void setDataEntrega(LocalDate dataEntrega) {
        this.dataEntrega = dataEntrega;
    }

    public LocalDate getDataRetirada() {
        return dataRetirada;
    }

    public void setDataRetirada(LocalDate dataRetirada) {
        this.dataRetirada = dataRetirada;
    }

    public MotivoTroca getMotivoTroca() {
        return motivoTroca;
    }

    public void setMotivoTroca(MotivoTroca motivoTroca) {
        this.motivoTroca = motivoTroca;
    }

    public boolean isGeraCobranca() {
        return geraCobranca;
    }

    public void setGeraCobranca(boolean geraCobranca) {
        this.geraCobranca = geraCobranca;
    }

    public Long getValorAdicional() {
        return valorAdicional;
    }

    public void setValorAdicional(Long valorAdicional) {
        this.valorAdicional = valorAdicional;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}