package com.lrcacambas;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.lrcacambas.data.local.entity.CategoriaDespesa;
import com.lrcacambas.data.local.entity.Despesa;
import com.lrcacambas.domain.service.DespesaService;
import com.lrcacambas.util.AppContainer;
import com.lrcacambas.util.AppExecutors;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class ManutencaoCacambasActivity extends AppCompatActivity {

    private EditText edtData, edtDescricao, edtValor;
    private LinearLayout containerHistorico;
    private DespesaService despesaService;

    private final DateTimeFormatter formatoData =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private boolean alterando = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manutencao_cacambas);

        AppContainer appContainer =
                new AppContainer(getApplicationContext());
        despesaService = appContainer.despesaService;

        edtData = findViewById(R.id.edtData);
        edtDescricao = findViewById(R.id.edtDescricao);
        edtValor = findViewById(R.id.edtValor);
        containerHistorico = findViewById(R.id.containerHistorico);

        edtData.setText(LocalDate.now().format(formatoData));
        edtData.setOnClickListener(v -> abrirCalendario());

        configurarMascaraMonetaria();

        findViewById(R.id.btnSalvar)
                .setOnClickListener(v -> salvarManutencao());

        carregarHistorico();
    }

    private void abrirCalendario() {
        LocalDate hoje = LocalDate.now();

        DatePickerDialog calendario = new DatePickerDialog(
                this,
                (view, ano, mes, dia) -> {
                    LocalDate data = LocalDate.of(ano, mes + 1, dia);
                    edtData.setText(data.format(formatoData));
                },
                hoje.getYear(),
                hoje.getMonthValue() - 1,
                hoje.getDayOfMonth()
        );

        calendario.show();
    }

    private void configurarMascaraMonetaria() {
        edtValor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (alterando) return;

                alterando = true;

                String numeros = s.toString().replaceAll("[^0-9]", "");

                if (numeros.isEmpty()) {
                    edtValor.setText("");
                } else {
                    long centavos = Long.parseLong(numeros);

                    String valorFormatado =
                            NumberFormat.getCurrencyInstance(
                                    new Locale("pt", "BR")
                            ).format(centavos / 100.0);

                    edtValor.setText(valorFormatado);
                    edtValor.setSelection(edtValor.length());
                }

                alterando = false;
            }
        });
    }

    private void salvarManutencao() {
        String descricao = edtDescricao.getText().toString().trim();
        String dataTexto = edtData.getText().toString();
        String valorTexto = edtValor.getText().toString();

        if (descricao.isEmpty() || valorTexto.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        LocalDate data;
        long valor;

        try {
            data = LocalDate.parse(dataTexto, formatoData);
            valor = Long.parseLong(
                    valorTexto.replaceAll("[^0-9]", "")
            );
        } catch (Exception e) {
            Toast.makeText(this, "Verifique a data e o valor.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Despesa despesa = new Despesa();
        despesa.setCategoria(CategoriaDespesa.MANUTENCAO_CACAMBAS);
        despesa.setDescricao(descricao);
        despesa.setDataDespesa(data);
        despesa.setValor(valor);

        AppExecutors.getDatabaseExecutor().execute(() -> {
            try {
                despesaService.cadastrar(despesa);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Manutenção cadastrada!",
                            Toast.LENGTH_SHORT).show();

                    edtDescricao.setText("");
                    edtValor.setText("");
                    edtData.setText(LocalDate.now().format(formatoData));

                    carregarHistorico();
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this,
                                "Erro ao cadastrar manutenção.",
                                Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void carregarHistorico() {
        AppExecutors.getDatabaseExecutor().execute(() -> {
            List<Despesa> despesas = new ArrayList<>(
                    despesaService.listarPorCategoria(
                            CategoriaDespesa.MANUTENCAO_CACAMBAS
                    )
            );

            Collections.sort(despesas,
                    Comparator.comparing(Despesa::getDataDespesa).reversed());

            runOnUiThread(() -> {
                containerHistorico.removeAllViews();

                for (Despesa despesa : despesas) {
                    adicionarCardHistorico(despesa);
                }
            });
        });
    }

    private void adicionarCardHistorico(Despesa despesa) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_cards);
        card.setPadding(dp(16), dp(12), dp(16), dp(12));

        LinearLayout.LayoutParams parametros =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        parametros.bottomMargin = dp(10);
        card.setLayoutParams(parametros);

        TextView titulo = criarTexto(
                "Manutenção • " + despesa.getDataDespesa().format(formatoData),
                18
        );
        titulo.setTextColor(getColor(R.color.azul_marinho));
        titulo.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView descricao = criarTexto(
                "Descrição: " + despesa.getDescricao(), 15
        );

        TextView valor = criarTexto(
                "Valor: " + NumberFormat.getCurrencyInstance(
                        new Locale("pt", "BR")
                ).format(despesa.getValor() / 100.0), 15
        );

        card.addView(titulo);
        card.addView(descricao);
        card.addView(valor);

        containerHistorico.addView(card);
    }

    private TextView criarTexto(String texto, int tamanho) {
        TextView campo = new TextView(this);
        campo.setText(texto);
        campo.setTextColor(getColor(R.color.preto));
        campo.setTextSize(tamanho);
        campo.setPadding(0, dp(5), 0, dp(5));
        return campo;
    }

    private int dp(int valor) {
        return (int) (valor *
                getResources().getDisplayMetrics().density);
    }
}