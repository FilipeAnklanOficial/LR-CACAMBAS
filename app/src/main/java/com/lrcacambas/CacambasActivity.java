package com.lrcacambas;

import android.os.Bundle;
import android.widget.Button;
import android.content.Intent;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CacambasActivity extends AppCompatActivity {

    private Button btnCadastrarCacamba;
    private Button btnBuscarCacamba;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cacambas);

        configurarInsets();

        btnCadastrarCacamba = findViewById(R.id.btnCadastrarCacamba);
        btnBuscarCacamba = findViewById(R.id.btnBuscarCacamba);

        btnCadastrarCacamba.setOnClickListener(v -> {
            Intent intent = new Intent(CacambasActivity.this, CadastroCacambaActivity.class);
            startActivity(intent);
        });

        btnBuscarCacamba.setOnClickListener(v -> {
            Intent intent = new Intent(
                    CacambasActivity.this,
                    BuscarCacambaActivity.class
            );
            startActivity(intent);
        });
    }

    private void configurarInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(
                    20 + systemBars.left,
                    systemBars.top,
                    20 + systemBars.right,
                    systemBars.bottom
            );

            return insets;
        });
    }
}