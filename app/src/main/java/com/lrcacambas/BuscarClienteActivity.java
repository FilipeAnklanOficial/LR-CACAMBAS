package com.lrcacambas;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.lrcacambas.data.local.entity.Cliente;
import com.lrcacambas.util.AppContainer;
import com.lrcacambas.util.AppExecutors;

import java.util.List;

public class BuscarClienteActivity extends AppCompatActivity {

    private AppContainer appContainer;
    private EditText edtBuscarCliente;
    private LinearLayout containerClientes;
    private TextView txtNenhumCliente;
    private TextView txtResultadoBusca;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_buscar_cliente);

        appContainer = new AppContainer(getApplicationContext());

        edtBuscarCliente = findViewById(R.id.edtBuscarCliente);
        containerClientes = findViewById(R.id.containerClientes);
        txtNenhumCliente = findViewById(R.id.txtNenhumCliente);
        txtResultadoBusca = findViewById(R.id.txtResultadoBusca);

        configurarInsets();
        configurarPesquisa();

        carregarClientes("");
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
        edtBuscarCliente.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {
                carregarClientes(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void carregarClientes(String pesquisa) {
        AppExecutors.getDatabaseExecutor().execute(() -> {
            try {
                List<Cliente> clientes;

                if (pesquisa == null || pesquisa.trim().isEmpty()) {
                    clientes = appContainer.clienteService.listarAtivos();
                } else {
                    clientes = appContainer.clienteService.buscarPorNome(
                            pesquisa.trim()
                    );
                }

                runOnUiThread(() -> mostrarClientes(clientes));

            } catch (Exception e) {
                runOnUiThread(() -> {
                    txtNenhumCliente.setText(
                            "Não foi possível carregar os clientes."
                    );
                    txtNenhumCliente.setVisibility(View.VISIBLE);
                    containerClientes.removeAllViews();
                });
            }
        });
    }

    private void mostrarClientes(List<Cliente> clientes) {
        containerClientes.removeAllViews();

        if (clientes == null || clientes.isEmpty()) {
            txtNenhumCliente.setText("Nenhum cliente encontrado.");
            txtNenhumCliente.setVisibility(View.VISIBLE);
            return;
        }

        txtNenhumCliente.setVisibility(View.GONE);

        if (edtBuscarCliente.getText().toString().trim().isEmpty()) {
            txtResultadoBusca.setText("Clientes cadastrados");
        } else {
            txtResultadoBusca.setText("Resultados da pesquisa");
        }

        for (Cliente cliente : clientes) {
            View item = getLayoutInflater().inflate(
                    R.layout.item_cliente_busca,
                    containerClientes,
                    false
            );

            TextView txtNome = item.findViewById(R.id.txtNomeCliente);
            TextView txtApelido = item.findViewById(R.id.txtApelidoCliente);
            TextView txtTelefone = item.findViewById(R.id.txtTelefoneCliente);

            txtNome.setText(cliente.getNome());
            txtTelefone.setText(formatarTelefone(cliente.getTelefone()));

            if (cliente.getApelido() != null && !cliente.getApelido().trim().isEmpty()) {
                txtApelido.setText("Apelido: " + cliente.getApelido());
                txtApelido.setVisibility(View.VISIBLE);
            } else {
                txtApelido.setVisibility(View.GONE);
            }

            item.setOnClickListener(v -> {
                Intent intent = new Intent(
                        BuscarClienteActivity.this,
                        DetalhesClienteActivity.class
                );

                intent.putExtra("idCliente", cliente.getIdCliente());

                startActivity(intent);
            });

            containerClientes.addView(item);
        }
    }

    private String formatarTelefone(String telefone) {
        if (telefone == null) {
            return "";
        }

        String digitos = telefone.replaceAll("\\D", "");

        if (digitos.length() == 11) {
            return "(" + digitos.substring(0, 2) + ") "
                    + digitos.substring(2, 7) + "-"
                    + digitos.substring(7);
        }

        if (digitos.length() == 10) {
            return "(" + digitos.substring(0, 2) + ") "
                    + digitos.substring(2, 6) + "-"
                    + digitos.substring(6);
        }

        return telefone;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (appContainer != null && edtBuscarCliente != null) {
            carregarClientes(edtBuscarCliente.getText().toString());
        }
    }
}