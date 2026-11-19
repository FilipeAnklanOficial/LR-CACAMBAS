package com.lrcacambas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.app.Dialog;
import android.graphics.Typeface;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.lrcacambas.data.local.entity.Cliente;
import com.lrcacambas.util.AppContainer;
import com.lrcacambas.util.AppExecutors;
import com.lrcacambas.util.DialogHelper;
import com.lrcacambas.data.local.entity.EnderecoCliente;

import java.util.List;

public class DetalhesClienteActivity extends AppCompatActivity {

    private AppContainer appContainer;

    private TextView txtNomeCliente;
    private TextView txtApelidoCliente;
    private TextView txtTelefoneCliente;
    private TextView txtDocumentoCliente;
    private TextView txtSemEnderecos;
    private LinearLayout containerEnderecos;
    private Button btnEditarEndereco;

    private long idCliente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_detalhes_cliente);

        appContainer = new AppContainer(getApplicationContext());

        txtNomeCliente = findViewById(R.id.txtNomeCliente);
        txtApelidoCliente = findViewById(R.id.txtApelidoCliente);
        txtTelefoneCliente = findViewById(R.id.txtTelefoneCliente);
        txtDocumentoCliente = findViewById(R.id.txtDocumentoCliente);
        txtSemEnderecos = findViewById(R.id.txtSemEnderecos);
        containerEnderecos = findViewById(R.id.containerEnderecos);
        btnEditarEndereco = findViewById(R.id.btnEditarEndereco);

        configurarInsets();

        findViewById(R.id.btnEditarCliente)
                .setOnClickListener(v -> abrirEdicaoCliente());

        findViewById(R.id.btnInativarCliente)
                .setOnClickListener(v -> confirmarInativacao());

        findViewById(R.id.btnAdicionarEndereco)
                .setOnClickListener(v -> abrirCadastroEndereco());

        btnEditarEndereco.setOnClickListener(v -> selecionarEnderecoParaEditar());

        idCliente = getIntent().getLongExtra("idCliente", -1);

        if (idCliente <= 0) {
            Toast.makeText(this, "Cliente inválido.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        carregarCliente();
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

            v.setPadding(
                    paddingLeft + systemBars.left,
                    paddingTop + systemBars.top,
                    paddingRight + systemBars.right,
                    paddingBottom + systemBars.bottom
            );

            return insets;
        });
    }

    private void confirmarInativacao() {
        DialogHelper.mostrarConfirmacao(
                this,
                "Inativar cliente",
                "Deseja realmente inativar este cliente? Ele deixará de aparecer na lista de clientes ativos.",
                this::inativarCliente
        );
    }

    private void inativarCliente() {
        AppExecutors.getDatabaseExecutor().execute(() -> {
            try {
                appContainer.clienteService.inativar(idCliente);

                runOnUiThread(() -> {
                    DialogHelper.mostrarSucesso(
                            DetalhesClienteActivity.this,
                            "Cliente inativado!",
                            "O cliente foi inativado com sucesso.",
                            this::finish
                    );
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(
                            DetalhesClienteActivity.this,
                            e.getMessage() != null
                                    ? e.getMessage()
                                    : "Não foi possível inativar o cliente.",
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        });
    }


    private void carregarCliente() {
        AppExecutors.getDatabaseExecutor().execute(() -> {
            try {
                Cliente cliente = appContainer.clienteService.buscarPorId(idCliente);

                runOnUiThread(() -> {
                    if (cliente == null) {
                        Toast.makeText(
                                this,
                                "Cliente não encontrado.",
                                Toast.LENGTH_SHORT
                        ).show();

                        finish();
                        return;
                    }

                    mostrarDadosCliente(cliente);
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(
                            this,
                            "Não foi possível carregar os dados do cliente.",
                            Toast.LENGTH_LONG
                    ).show();

                    finish();
                });
            }
        });
    }

    private void mostrarDadosCliente(Cliente cliente) {
        txtNomeCliente.setText(cliente.getNome());

        if (cliente.getApelido() != null
                && !cliente.getApelido().trim().isEmpty()) {
            txtApelidoCliente.setText(cliente.getApelido());
        } else {
            txtApelidoCliente.setText("Não informado");
        }

        txtTelefoneCliente.setText(formatarTelefone(cliente.getTelefone()));

        if (cliente.getDocumento() != null
                && !cliente.getDocumento().trim().isEmpty()) {
            txtDocumentoCliente.setText(formatarDocumento(cliente.getDocumento()));
        } else {
            txtDocumentoCliente.setText("Não informado");
        }
    }

    private String formatarTelefone(String telefone) {
        if (telefone == null) {
            return "Não informado";
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

    private String formatarDocumento(String documento) {
        String digitos = documento.replaceAll("\\D", "");

        if (digitos.length() == 11) {
            return digitos.substring(0, 3) + "."
                    + digitos.substring(3, 6) + "."
                    + digitos.substring(6, 9) + "-"
                    + digitos.substring(9);
        }

        if (digitos.length() == 14) {
            return digitos.substring(0, 2) + "."
                    + digitos.substring(2, 5) + "."
                    + digitos.substring(5, 8) + "/"
                    + digitos.substring(8, 12) + "-"
                    + digitos.substring(12);
        }

        return documento;
    }

    private void abrirEdicaoCliente() {
        Intent intent = new Intent(
                DetalhesClienteActivity.this,
                CadastroClienteActivity.class
        );

        intent.putExtra("idCliente", idCliente);

        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (idCliente > 0) {
            carregarCliente();
            carregarEnderecos();
        }
    }

    private void carregarEnderecos() {
        AppExecutors.getDatabaseExecutor().execute(() -> {
            try {
                List<EnderecoCliente> enderecos =
                        appContainer.enderecoClienteService.listarPorCliente(idCliente);

                runOnUiThread(() -> mostrarEnderecos(enderecos));

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(
                            DetalhesClienteActivity.this,
                            "Não foi possível carregar os endereços.",
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        });
    }

    private void mostrarEnderecos(List<EnderecoCliente> enderecos) {
        containerEnderecos.removeAllViews();

        if (enderecos == null || enderecos.isEmpty()) {
            txtSemEnderecos.setVisibility(View.VISIBLE);
            return;
        }

        txtSemEnderecos.setVisibility(View.GONE);

        for (int i = 0; i < enderecos.size(); i++) {
            EnderecoCliente endereco = enderecos.get(i);

            String descricao = endereco.getDescricao();

            if (descricao == null || descricao.trim().isEmpty()) {
                descricao = "Endereço " + (i + 1);
            }

            // Card principal
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(
                    dpParaPx(16),
                    dpParaPx(14),
                    dpParaPx(16),
                    dpParaPx(14)
            );
            card.setBackgroundResource(R.drawable.bg_cards);

            // Título do endereço
            TextView txtDescricao = new TextView(this);
            txtDescricao.setText(descricao);
            txtDescricao.setTextColor(getColor(R.color.azul_marinho));
            txtDescricao.setTextSize(18);
            txtDescricao.setTypeface(null, android.graphics.Typeface.BOLD);

            card.addView(txtDescricao);

            // Linha divisória
            View divisoria = new View(this);
            divisoria.setBackgroundColor(getColor(R.color.amarelo_principal));

            LinearLayout.LayoutParams paramsDivisoria =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            dpParaPx(2)
                    );

            paramsDivisoria.topMargin = dpParaPx(10);
            paramsDivisoria.bottomMargin = dpParaPx(10);

            card.addView(divisoria, paramsDivisoria);

            // Informações do endereço
            StringBuilder texto = new StringBuilder();

            texto.append(endereco.getLogradouro());

            if (endereco.getNumero() != null
                    && !endereco.getNumero().trim().isEmpty()) {
                texto.append(", ").append(endereco.getNumero());
            }

            texto.append("\n")
                    .append(endereco.getBairro())
                    .append(" - ")
                    .append(endereco.getCidade());

            if (endereco.getComplemento() != null
                    && !endereco.getComplemento().trim().isEmpty()) {
                texto.append("\n").append(endereco.getComplemento());
            }

            if (endereco.getReferencia() != null
                    && !endereco.getReferencia().trim().isEmpty()) {
                texto.append("\nReferência: ")
                        .append(endereco.getReferencia());
            }

            TextView txtDados = new TextView(this);
            txtDados.setText(texto.toString());
            txtDados.setTextColor(getColor(R.color.preto));
            txtDados.setTextSize(16);

            card.addView(txtDados);

            LinearLayout.LayoutParams paramsCard =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            paramsCard.topMargin = dpParaPx(12);

            containerEnderecos.addView(card, paramsCard);
        }

        int quantidadeEnderecos = enderecos.size();

        if (quantidadeEnderecos >= 2) {
            findViewById(R.id.btnAdicionarEndereco).setVisibility(View.GONE);
            btnEditarEndereco.setVisibility(View.VISIBLE);

        } else {
            findViewById(R.id.btnAdicionarEndereco).setVisibility(View.VISIBLE);
            btnEditarEndereco.setVisibility(
                    quantidadeEnderecos == 1 ? View.VISIBLE : View.GONE
            );
        }
    }

    private int dpParaPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void abrirCadastroEndereco() {
        Intent intent = new Intent(
                DetalhesClienteActivity.this,
                CadastroEnderecoActivity.class
        );

        intent.putExtra("idCliente", idCliente);

        startActivity(intent);
    }

    private void selecionarEnderecoParaEditar() {
        AppExecutors.getDatabaseExecutor().execute(() -> {
            try {
                List<EnderecoCliente> enderecos =
                        appContainer.enderecoClienteService.listarPorCliente(idCliente);

                runOnUiThread(() -> {
                    if (enderecos == null || enderecos.isEmpty()) {
                        Toast.makeText(
                                this,
                                "Nenhum endereço cadastrado.",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    mostrarSeletorEnderecos(enderecos);
                });

            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(
                        this,
                        "Não foi possível consultar os endereços.",
                        Toast.LENGTH_LONG
                ).show());
            }
        });
    }

    private void mostrarSeletorEnderecos(List<EnderecoCliente> enderecos) {
        Dialog dialog = new Dialog(this);

        dialog.setContentView(R.layout.dialog_selecionar_endereco);
        dialog.setCancelable(true);

        LinearLayout container = dialog.findViewById(
                R.id.containerOpcoesEndereco
        );

        Button btnCancelar = dialog.findViewById(
                R.id.btnCancelarSelecao
        );

        for (EnderecoCliente endereco : enderecos) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(
                    dpParaPx(14),
                    dpParaPx(12),
                    dpParaPx(14),
                    dpParaPx(12)
            );
            card.setBackgroundResource(R.drawable.bg_cards);

            String descricao = endereco.getDescricao();

            if (descricao == null || descricao.trim().isEmpty()) {
                descricao = "Endereço";
            }

            TextView txtDescricao = new TextView(this);
            txtDescricao.setText(descricao);
            txtDescricao.setTextColor(getColor(R.color.azul_marinho));
            txtDescricao.setTextSize(18);
            txtDescricao.setTypeface(null, Typeface.BOLD);

            TextView txtInformacoes = new TextView(this);

            StringBuilder informacoes = new StringBuilder();
            informacoes.append(endereco.getLogradouro());

            if (endereco.getNumero() != null
                    && !endereco.getNumero().trim().isEmpty()) {
                informacoes.append(", ").append(endereco.getNumero());
            }

            informacoes.append("\n").append(endereco.getBairro());

            txtInformacoes.setText(informacoes.toString());
            txtInformacoes.setTextColor(getColor(R.color.preto));
            txtInformacoes.setTextSize(15);
            txtInformacoes.setPadding(0, dpParaPx(6), 0, 0);

            card.addView(txtDescricao);
            card.addView(txtInformacoes);

            LinearLayout.LayoutParams parametros =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            parametros.bottomMargin = dpParaPx(12);

            container.addView(card, parametros);

            card.setOnClickListener(v -> {
                dialog.dismiss();
                abrirEdicaoEndereco(endereco.getIdEndereco());
            });
        }

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        Window janela = dialog.getWindow();

        if (janela != null) {
            janela.setBackgroundDrawableResource(
                    android.R.color.transparent
            );
        }

        dialog.show();

        if (janela != null) {
            janela.setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.88),
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private void abrirEdicaoEndereco(long idEndereco) {
        Intent intent = new Intent(
                DetalhesClienteActivity.this,
                CadastroEnderecoActivity.class
        );

        intent.putExtra("idCliente", idCliente);
        intent.putExtra("idEndereco", idEndereco);

        startActivity(intent);
    }

}