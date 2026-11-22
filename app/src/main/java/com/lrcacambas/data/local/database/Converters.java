package com.lrcacambas.data.local.database;

import androidx.room.TypeConverter;

import com.lrcacambas.data.local.entity.CategoriaDespesa;
import com.lrcacambas.data.local.entity.FormaPagamento;
import com.lrcacambas.data.local.entity.MotivoTroca;
import com.lrcacambas.data.local.entity.StatusCacamba;
import com.lrcacambas.data.local.entity.StatusLocacao;

import java.time.LocalDate;

public class Converters {

    // LocalDate
    @TypeConverter
    public static String fromLocalDate(LocalDate date) {
        return date == null ? null : date.toString();
    }

    @TypeConverter
    public static LocalDate toLocalDate(String date) {
        return date == null ? null : LocalDate.parse(date);
    }

    // StatusCacamba
    @TypeConverter
    public static String fromStatusCacamba(StatusCacamba status) {
        return status == null ? null : status.name();
    }

    @TypeConverter
    public static StatusCacamba toStatusCacamba(String status) {
        return status == null ? null : StatusCacamba.valueOf(status);
    }

    // StatusLocacao
    @TypeConverter
    public static String fromStatusLocacao(StatusLocacao status) {
        return status == null ? null : status.name();
    }

    @TypeConverter
    public static StatusLocacao toStatusLocacao(String status) {
        return status == null ? null : StatusLocacao.valueOf(status);
    }

    // MotivoTroca
    @TypeConverter
    public static String fromMotivoTroca(MotivoTroca motivo) {
        return motivo == null ? null : motivo.name();
    }

    @TypeConverter
    public static MotivoTroca toMotivoTroca(String motivo) {
        return motivo == null ? null : MotivoTroca.valueOf(motivo);
    }

    // FormaPagamento
    @TypeConverter
    public static String fromFormaPagamento(FormaPagamento forma) {
        return forma == null ? null : forma.name();
    }

    @TypeConverter
    public static FormaPagamento toFormaPagamento(String forma) {
        return forma == null ? null : FormaPagamento.valueOf(forma);
    }

    // CategoriaDespesa
    @TypeConverter
    public static String fromCategoriaDespesa(CategoriaDespesa categoria) {
        return categoria == null ? null : categoria.name();
    }

    @TypeConverter
    public static CategoriaDespesa toCategoriaDespesa(String categoria) {
        return categoria == null ? null : CategoriaDespesa.valueOf(categoria);
    }
}