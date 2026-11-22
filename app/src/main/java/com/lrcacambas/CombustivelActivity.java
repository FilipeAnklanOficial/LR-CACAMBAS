package com.lrcacambas;

import android.os.Bundle;
import android.app.DatePickerDialog;
import android.widget.EditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.lrcacambas.data.local.entity.CategoriaDespesa;
import com.lrcacambas.data.local.entity.Despesa;
import com.lrcacambas.domain.service.DespesaService;
import com.lrcacambas.util.AppContainer;
import com.lrcacambas.util.AppExecutors;
import com.lrcacambas.data.local.entity.Despesa;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.text.DecimalFormat;
import java.util.Collections;

public class CombustivelActivity extends AppCompatActivity {

    private EditText edtData;
    private EditText edtValor;
    private EditText edtLitros;
    private EditText edtQuilometragem;

    private RadioGroup rgTanqueCompleto;
    private DespesaService despesaService;

    private TextView txtMediaHistorica;
    private LinearLayout containerHistorico;

    private final DateTimeFormatter formatoData =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_combustivel);

        edtData = findViewById(R.id.edtData);

// Preenche automaticamente com a data atual
        edtData.setText(LocalDate.now().format(formatoData));

// Abre o calendário ao tocar no campo
        edtData.setOnClickListener(v -> abrirCalendario());

        edtQuilometragem = findViewById(R.id.edtQuilometragem);
        configurarMascaraQuilometragem(edtQuilometragem);

        edtLitros = findViewById(R.id.edtLitros);
        configurarMascaraLitros(edtLitros);

        edtValor = findViewById(R.id.edtValor);

        configurarMascaraMonetaria(edtValor);

        AppContainer appContainer = new AppContainer(getApplicationContext());
        despesaService = appContainer.despesaService;

        txtMediaHistorica = findViewById(R.id.txtMediaHistorica);
        containerHistorico = findViewById(R.id.containerHistorico);

        rgTanqueCompleto = findViewById(R.id.rgTanqueCompleto);

        findViewById(R.id.btnSalvarAbastecimento)
                .setOnClickListener(v -> salvarAbastecimento());

        carregarHistorico();
    }

    private void abrirCalendario() {
        LocalDate dataAtual = LocalDate.now();

        DatePickerDialog calendario = new DatePickerDialog(
                this,
                (view, ano, mes, dia) -> {
                    LocalDate dataSelecionada = LocalDate.of(ano, mes + 1, dia);
                    edtData.setText(dataSelecionada.format(formatoData));
                },
                dataAtual.getYear(),
                dataAtual.getMonthValue() - 1,
                dataAtual.getDayOfMonth()
        );

        calendario.show();
    }

    private void configurarMascaraQuilometragem(EditText campo) {
        campo.addTextChangedListener(new TextWatcher() {
            private boolean alterando = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (alterando) return;

                alterando = true;

                String numeros = s.toString().replaceAll("[^0-9]", "");

                // Limita a entrada a seis dígitos
                if (numeros.length() > 6) {
                    numeros = numeros.substring(0, 6);
                }

                if (numeros.isEmpty()) {
                    campo.setText("");
                } else {
                    long quilometragem = Long.parseLong(numeros);

                    NumberFormat formato = NumberFormat.getIntegerInstance(
                            new Locale("pt", "BR")
                    );

                    campo.setText(formato.format(quilometragem));
                }

                campo.setSelection(campo.getText().length());
                alterando = false;
            }
        });
    }

    private void configurarMascaraMonetaria(EditText campo) {
        campo.addTextChangedListener(new TextWatcher() {
            private boolean alterando = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (alterando) return;

                alterando = true;

                String numeros = s.toString().replaceAll("[^0-9]", "");

                if (numeros.isEmpty()) {
                    campo.setText("");
                } else {
                    long centavos = Long.parseLong(numeros);

                    NumberFormat formato = NumberFormat.getCurrencyInstance(
                            new Locale("pt", "BR")
                    );

                    campo.setText(formato.format(centavos / 100.0));
                }

                campo.setSelection(campo.getText().length());
                alterando = false;
            }
        });
    }

    private void configurarMascaraLitros(EditText campo) {
        campo.addTextChangedListener(new TextWatcher() {
            private boolean alterando = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (alterando) return;

                alterando = true;

                String numeros = s.toString().replaceAll("[^0-9]", "");

                if (numeros.isEmpty()) {
                    campo.setText("");
                } else {
                    long centesimos = Long.parseLong(numeros);

                    campo.setText(String.format(
                            new Locale("pt", "BR"),
                            "%.2f",
                            centesimos / 100.0
                    ));
                }

                campo.setSelection(campo.getText().length());
                alterando = false;
            }
        });
    }

    private void salvarAbastecimento() {

        try {
            // Verifica se todos os campos foram preenchidos
            if (edtData.getText().toString().isEmpty() ||
                    edtQuilometragem.getText().toString().isEmpty() ||
                    edtLitros.getText().toString().isEmpty() ||
                    edtValor.getText().toString().isEmpty()) {

                Toast.makeText(this,
                        "Preencha todos os campos.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Verifica a opção de tanque completo
            int opcaoSelecionada = rgTanqueCompleto.getCheckedRadioButtonId();

            if (opcaoSelecionada == -1) {
                Toast.makeText(this,
                        "Informe se o tanque foi completado.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Converte a data
            LocalDate data = LocalDate.parse(
                    edtData.getText().toString(),
                    formatoData
            );

            // Remove o ponto da quilometragem
            Long quilometragem = Long.parseLong(
                    edtQuilometragem.getText().toString().replace(".", "")
            );

            // Converte os litros para Double
            Double litros = Double.parseDouble(
                    edtLitros.getText().toString().replace(",", ".")
            );

            // Remove a formatação monetária e obtém os centavos
            Long valor = Long.parseLong(
                    edtValor.getText().toString().replaceAll("[^0-9]", "")
            );

            Boolean tanqueCompleto =
                    opcaoSelecionada == R.id.rbTanqueSim;

            // Monta a despesa
            Despesa despesa = new Despesa();

            despesa.setCategoria(CategoriaDespesa.COMBUSTIVEL);
            despesa.setDescricao("Abastecimento");
            despesa.setDataDespesa(data);
            despesa.setQuilometragem(quilometragem);
            despesa.setLitros(litros);
            despesa.setValor(valor);
            despesa.setTanqueCompleto(tanqueCompleto);

            // Salva no banco em segundo plano
            AppExecutors.getDatabaseExecutor().execute(() -> {

                try {
                    despesaService.cadastrar(despesa);

                    runOnUiThread(() -> {
                        Toast.makeText(this,
                                "Abastecimento registrado com sucesso!",
                                Toast.LENGTH_SHORT).show();

                        limparFormulario();
                        carregarHistorico();
                    });

                } catch (Exception e) {
                    runOnUiThread(() ->
                            Toast.makeText(this,
                                    e.getMessage(),
                                    Toast.LENGTH_LONG).show()
                    );
                }
            });

        } catch (Exception e) {
            Toast.makeText(this,
                    "Verifique os dados informados.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void carregarHistorico() {
        AppExecutors.getDatabaseExecutor().execute(() -> {
            List<Despesa> abastecimentos = new ArrayList<>(
                    despesaService.listarAbastecimentos()
            );

            double media = despesaService.calcularMediaHistorica();

            Collections.reverse(abastecimentos);

            runOnUiThread(() -> {
                if (media > 0) {
                    DecimalFormat formatoMedia = new DecimalFormat("#,##0.00");
                    txtMediaHistorica.setText(
                            formatoMedia.format(media) + " km/L"
                    );
                } else {
                    txtMediaHistorica.setText("Média ainda indisponível");
                }

                containerHistorico.removeAllViews();

                for (Despesa abastecimento : abastecimentos) {
                    adicionarCardHistorico(abastecimento);
                }

                // Os cards do histórico serão adicionados aqui no próximo passo.
            });
        });
    }

    private void adicionarCardHistorico(Despesa despesa) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_cards);
        card.setPadding(dp(16), dp(12), dp(16), dp(12));

        LinearLayout.LayoutParams parametrosCard =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        parametrosCard.bottomMargin = dp(10);
        card.setLayoutParams(parametrosCard);

        TextView titulo = new TextView(this);
        titulo.setText("Abastecimento • " +
                despesa.getDataDespesa().format(formatoData));
        titulo.setTextColor(getColor(R.color.azul_marinho));
        titulo.setTextSize(18);
        titulo.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView quilometragem = criarTextoHistorico(
                "Quilometragem: " +
                        NumberFormat.getIntegerInstance(new Locale("pt", "BR"))
                                .format(despesa.getQuilometragem()) + " km"
        );

        TextView litros = criarTextoHistorico(
                "Litros: " +
                        String.format(new Locale("pt", "BR"), "%.2f L",
                                despesa.getLitros())
        );

        TextView tanque = criarTextoHistorico(
                "Tanque completo: " +
                        (Boolean.TRUE.equals(despesa.getTanqueCompleto()) ? "Sim" : "Não")
        );

        TextView valor = criarTextoHistorico(
                "Valor: " +
                        NumberFormat.getCurrencyInstance(new Locale("pt", "BR"))
                                .format(despesa.getValor() / 100.0)
        );

        card.addView(titulo);
        card.addView(quilometragem);
        card.addView(litros);
        card.addView(tanque);
        card.addView(valor);

        containerHistorico.addView(card);
    }

    private TextView criarTextoHistorico(String texto) {
        TextView campo = new TextView(this);
        campo.setText(texto);
        campo.setTextColor(getColor(R.color.preto));
        campo.setTextSize(15);
        campo.setPadding(0, dp(5), 0, dp(5));
        return campo;
    }

    private int dp(int valor) {
        return (int) (valor * getResources().getDisplayMetrics().density);
    }

    private void limparFormulario() {

        edtData.setText(LocalDate.now().format(formatoData));
        edtQuilometragem.setText("");
        edtLitros.setText("");
        edtValor.setText("");
        rgTanqueCompleto.clearCheck();
    }
}