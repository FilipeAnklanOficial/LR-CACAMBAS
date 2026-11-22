package com.lrcacambas;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

public class LocacoesActivity extends AppCompatActivity {

    private Button btnAbrirLocacao;
    private Button btnLocacoesAtivas;
    private Button btnLocacoesFinalizadas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.enableEdgeToEdge(getWindow());

        setContentView(R.layout.activity_locacoes);

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

        btnAbrirLocacao = findViewById(R.id.btnAbrirLocacao);
        btnLocacoesAtivas = findViewById(R.id.btnLocacoesAtivas);
        btnLocacoesFinalizadas = findViewById(R.id.btnLocacoesFinalizadas);

        btnAbrirLocacao.setOnClickListener(v -> {
            Intent intent = new Intent(
                    LocacoesActivity.this,
                    AbrirLocacaoActivity.class
            );
            startActivity(intent);
        });

        btnLocacoesAtivas.setOnClickListener(v -> {
            Intent intent = new Intent(this, LocacoesAtivasActivity.class);
            startActivity(intent);
        });

        btnLocacoesFinalizadas.setOnClickListener(v -> {
            Intent intent = new Intent(this, LocacoesFinalizadasActivity.class);
            startActivity(intent);
        });
    }
}