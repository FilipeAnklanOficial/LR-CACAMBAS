package com.lrcacambas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.enableEdgeToEdge(getWindow());

        setContentView(R.layout.activity_main);

        View root = findViewById(R.id.main);

        int paddingLeft = root.getPaddingLeft();
        int paddingTop = root.getPaddingTop();
        int paddingRight = root.getPaddingRight();
        int paddingBottom = root.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {

            Insets systemBars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
            );

            v.setPadding(
                    paddingLeft + systemBars.left,
                    paddingTop + systemBars.top,
                    paddingRight + systemBars.right,
                    paddingBottom + systemBars.bottom
            );

            return insets;
        });

        // Navegação para Clientes
        findViewById(R.id.btnClientes).setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    ClientesActivity.class
            );

            startActivity(intent);
        });

        // Navegação para Locações
        findViewById(R.id.btnLocacoes).setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    LocacoesActivity.class
            );

            startActivity(intent);
        });

        // Navegação para Caçambas
        findViewById(R.id.btnCacambas).setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    CacambasActivity.class
            );

            startActivity(intent);
        });

        // Navegação para Recebimentos
        findViewById(R.id.btnRecebimentos).setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    RecebimentosActivity.class
            );

            startActivity(intent);
        });

        // Navegação para Despesas
        findViewById(R.id.btnDespesas).setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    DespesasActivity.class
            );

            startActivity(intent);
        });
    }
}