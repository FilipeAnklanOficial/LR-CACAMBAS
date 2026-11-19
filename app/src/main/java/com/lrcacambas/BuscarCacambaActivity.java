package com.lrcacambas;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.lrcacambas.data.local.entity.Cacamba;
import com.lrcacambas.util.AppContainer;
import com.lrcacambas.util.AppExecutors;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BuscarCacambaActivity extends AppCompatActivity {

    private AppContainer appContainer;
    private EditText edtBuscarCacamba;
    private LinearLayout containerCacambas;
    private TextView txtNenhumaCacamba;
    private TextView txtResultadoBusca;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_buscar_cacamba);

        appContainer = new AppContainer(getApplicationContext());

        edtBuscarCacamba = findViewById(R.id.edtBuscarCacamba);
        containerCacambas = findViewById(R.id.containerCacambas);
        txtNenhumaCacamba = findViewById(R.id.txtNenhumaCacamba);
        txtResultadoBusca = findViewById(R.id.txtResultadoBusca);

        configurarInsets();
        configurarPesquisa();

        carregarCacambas("");
    }

    private void configurarInsets() {
        View root = findViewById(R.id.main);

        int paddingLeft = root.getPaddingLeft();
        int paddingTop = root.getPaddingTop();
        int paddingRight = root.getPaddingRight();
        int paddingBottom = root.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
            );

            Insets teclado = insets.getInsets(
                    WindowInsetsCompat.Type.ime()
            );

            int paddingBottomFinal = Math.max(
                    systemBars.bottom,
                    teclado.bottom
            );

            v.setPadding(
                    paddingLeft + systemBars.left,
                    paddingTop + systemBars.top,
                    paddingRight + systemBars.right,
                    paddingBottom + paddingBottomFinal
            );

            return insets;
        });
    }

    private void configurarPesquisa() {
        edtBuscarCacamba.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {
                carregarCacambas(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void carregarCacambas(String pesquisa) {
        AppExecutors.getDatabaseExecutor().execute(() -> {
            try {
                List<Cacamba> cacambas = appContainer.cacambaService.listarTodas();

                if (pesquisa != null && !pesquisa.trim().isEmpty()) {
                    String codigoPesquisado = pesquisa.trim();

                    List<Cacamba> filtradas = new ArrayList<>();

                    for (Cacamba cacamba : cacambas) {
                        String codigo = String.valueOf(cacamba.getCodigo());

                        if (codigo.contains(codigoPesquisado)) {
                            filtradas.add(cacamba);
                        }
                    }

                    cacambas = filtradas;
                }

                List<Cacamba> resultado = cacambas;
                runOnUiThread(() -> mostrarCacambas(resultado));

            } catch (Exception e) {
                runOnUiThread(() -> {
                    txtNenhumaCacamba.setText(
                            "Não foi possível carregar as caçambas."
                    );
                    txtNenhumaCacamba.setVisibility(View.VISIBLE);
                    containerCacambas.removeAllViews();
                });
            }
        });
    }

    private void mostrarCacambas(List<Cacamba> cacambas) {
        containerCacambas.removeAllViews();

        if (cacambas == null || cacambas.isEmpty()) {
            txtNenhumaCacamba.setText("Nenhuma caçamba encontrada.");
            txtNenhumaCacamba.setVisibility(View.VISIBLE);
            txtResultadoBusca.setText("");
            return;
        }

        txtNenhumaCacamba.setVisibility(View.GONE);

        if (edtBuscarCacamba.getText().toString().trim().isEmpty()) {
            txtResultadoBusca.setText("Caçambas cadastradas");
        } else {
            txtResultadoBusca.setText("Resultados da pesquisa");
        }

        for (Cacamba cacamba : cacambas) {
            View item = getLayoutInflater().inflate(
                    R.layout.item_cacamba_busca,
                    containerCacambas,
                    false
            );

            TextView txtCodigo = item.findViewById(R.id.txtCodigoCacamba);
            TextView txtStatus = item.findViewById(R.id.txtStatusCacamba);

            item.setOnClickListener(v -> {
                Intent intent = new Intent(
                        BuscarCacambaActivity.this,
                        DetalhesCacambaActivity.class
                );

                intent.putExtra("idCacamba", cacamba.getIdCacamba());

                startActivity(intent);
            });

            String codigoFormatado = String.format(
                    Locale.getDefault(),
                    "%03d",
                    cacamba.getCodigo()
            );

            txtCodigo.setText("Caçamba " + codigoFormatado);
            txtStatus.setText("Situação: " + formatarStatus(cacamba));

            containerCacambas.addView(item);
        }
    }

    private String formatarStatus(Cacamba cacamba) {
        switch (cacamba.getStatus()) {
            case DISPONIVEL:
                return "Disponível";

            case EM_USO:
                return "Em uso";

            case INATIVA:
                return "Inativa";

            case MANUTENCAO:
                return "Em manutenção";

            default:
                return "Não informado";
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (appContainer != null && edtBuscarCacamba != null) {
            carregarCacambas(edtBuscarCacamba.getText().toString());
        }
    }
}