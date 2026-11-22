package com.lrcacambas.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "cacambas",
        indices = {
                @Index(value = {"codigo"}, unique = true)
        }
)

public class Cacamba {

    @PrimaryKey(autoGenerate = true)
    private Long idCacamba;

    private Integer codigo;
    private StatusCacamba status;
    private String observacaoStatus;

    public Cacamba() {
    }

    public Long getIdCacamba() {
        return idCacamba;
    }

    public void setIdCacamba(Long idCacamba) {
        this.idCacamba = idCacamba;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public StatusCacamba getStatus() {
        return status;
    }

    public void setStatus(StatusCacamba status) {
        this.status = status;
    }

    public String getObservacaoStatus() {
        return observacaoStatus;
    }

    public void setObservacaoStatus(String observacaoStatus) {
        this.observacaoStatus = observacaoStatus;
    }
}