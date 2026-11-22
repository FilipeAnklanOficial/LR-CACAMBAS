package com.lrcacambas;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.lrcacambas.util.AppContainer;
import com.lrcacambas.util.AppExecutors;

import java.util.Locale;

public class CadastroCacambaActivity extends AppCompatActivity {

    private TextView tvCodigoCacamba;
    private Button btnSalvar;
    private Button btnCancelar;
    private AppContainer appContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_cadastro_cacamba);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvCodigoCacamba = findViewById(R.id.tvCodigoCacamba);
        btnSalvar = findViewById(R.id.btnSalvar);
        btnCancelar = findViewById(R.id.btnCancelar);

        btnSalvar.setOnClickListener(v -> salvarCacamba());

        btnCancelar.setOnClickListener(v -> finish());

        appContainer = new AppContainer(getApplicationContext());

        carregarProximoCodigo();
    }

    private void carregarProximoCodigo() {
        AppExecutors.getDatabaseExecutor().execute(() -> {
            try {
                int codigo = appContainer.cacambaService.obterProximoCodigo();

                String codigoFormatado = String.format(
                        Locale.getDefault(), "%03d", codigo
                );

                runOnUiThread(() ->
                        tvCodigoCacamba.setText(codigoFormatado)
                );

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(
                                this,
                                "Erro ao carregar o código da caçamba.",
                                Toast.LENGTH_SHORT
                        ).show()
                );
            }
        });
    }

    private void salvarCacamba() {
        btnSalvar.setEnabled(false);

        AppExecutors.getDatabaseExecutor().execute(() -> {
            try {
                appContainer.cacambaService.cadastrar();

                runOnUiThread(() -> {
                    Toast.makeText(
                            this,
                            "Caçamba cadastrada com sucesso!",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnSalvar.setEnabled(true);

                    Toast.makeText(
                            this,
                            "Erro ao cadastrar caçamba: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        });
    }
}