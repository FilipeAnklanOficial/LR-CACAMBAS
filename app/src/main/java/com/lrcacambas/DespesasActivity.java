package com.lrcacambas;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class DespesasActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_despesas);

        findViewById(R.id.btnCombustivel)
                .setOnClickListener(v -> {
                    Intent intent = new Intent(
                            DespesasActivity.this,
                            CombustivelActivity.class);
                    startActivity(intent);
                });

        findViewById(R.id.btnManutencaoCaminhao)
                .setOnClickListener(v -> {
                    Intent intent = new Intent(
                            this,
                            ManutencaoCaminhaoActivity.class);
                    startActivity(intent);
                });

        findViewById(R.id.btnManutencaoCacambas)
                .setOnClickListener(v -> {
                    Intent intent = new Intent(this,
                            ManutencaoCacambasActivity.class);
                    startActivity(intent);
                });
    }
}