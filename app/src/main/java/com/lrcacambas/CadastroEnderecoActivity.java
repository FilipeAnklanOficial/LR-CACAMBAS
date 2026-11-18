package com.lrcacambas;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.lrcacambas.data.local.entity.EnderecoCliente;
import com.lrcacambas.util.AppContainer;
import com.lrcacambas.util.AppExecutors;

public class CadastroEnderecoActivity extends AppCompatActivity {

    private AppContainer appContainer;
    private long idCliente = -1;
    private Spinner spDescricaoEndereco;
    private EditText edtLogradouro;
    private EditText edtNumeroEndereco;
    private EditText edtBairro;
    private EditText edtCidade;
    private EditText edtComplemento;
    private EditText edtReferencia;
    private Button btnSalvarEndereco;
    private Button btnCancelarEndereco;
    private long idEndereco = -1;
    private boolean modoEdicao = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_cadastro_endereco);

        appContainer = new AppContainer(getApplicationContext());

        idCliente = getIntent().getLongExtra("idCliente", -1);
        modoEdicao = idEndereco > 0;

        if (idCliente <= 0) {
            Toast.makeText(this, "Cliente inválido.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        inicializarComponentes();
        configurarInsets();
        configurarBotoes();
    }

    private void inicializarComponentes() {
        if (modoEdicao) {
            carregarEndereco();
        }
        spDescricaoEndereco = findViewById(R.id.spDescricaoEndereco);
        String[] descricoes = {"Principal", "Secundário"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.item_spinner_endereco,
                descricoes
        );

        adapter.setDropDownViewResource(
                R.layout.item_spinner_endereco_dropdown
        );

        spDescricaoEndereco.setAdapter(adapter);
        edtLogradouro = findViewById(R.id.edtLogradouro);
        edtNumeroEndereco = findViewById(R.id.edtNumeroEndereco);
        edtBairro = findViewById(R.id.edtBairro);
        edtCidade = findViewById(R.id.edtCidade);
        edtComplemento = findViewById(R.id.edtComplemento);
        edtReferencia = findViewById(R.id.edtReferencia);

        btnSalvarEndereco = findViewById(R.id.btnSalvarEndereco);
        btnCancelarEndereco = findViewById(R.id.btnCancelarEndereco);

        TextView txtTitulo = findViewById(R.id.txtTituloCadastroEndereco);

        if (modoEdicao) {
            txtTitulo.setText("Editar endereço");
            btnSalvarEndereco.setText("Salvar alterações");
        }
    }

    private void configurarInsets() {
        if (modoEdicao) {
            carregarEndereco();
        }
        View raiz = findViewById(R.id.main);

        int paddingLeft = raiz.getPaddingLeft();
        int paddingTop = raiz.getPaddingTop();
        int paddingRight = raiz.getPaddingRight();
        int paddingBottom = raiz.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(raiz, (v, insets) -> {
            Insets barras = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
            );

            Insets teclado = insets.getInsets(
                    WindowInsetsCompat.Type.ime()
            );

            int bottom = Math.max(barras.bottom, teclado.bottom);

            v.setPadding(
                    paddingLeft + barras.left,
                    paddingTop + barras.top,
                    paddingRight + barras.right,
                    paddingBottom + bottom
            );

            return insets;
        });

        ViewCompat.requestApplyInsets(raiz);
    }

    private void configurarBotoes() {
        if (modoEdicao) {
            carregarEndereco();
        }
        btnSalvarEndereco.setOnClickListener(v -> salvarEndereco());

        btnCancelarEndereco.setOnClickListener(v -> finish());
    }

    private void salvarEndereco() {
        String logradouro = edtLogradouro.getText().toString().trim();
        String numero = edtNumeroEndereco.getText().toString().trim();
        String bairro = edtBairro.getText().toString().trim();
        String cidade = edtCidade.getText().toString().trim();
        String complemento = edtComplemento.getText().toString().trim();
        String referencia = edtReferencia.getText().toString().trim();

        if (logradouro.isEmpty()) {
            edtLogradouro.setError("Informe o logradouro.");
            edtLogradouro.requestFocus();
            return;
        }

        if (bairro.isEmpty()) {
            edtBairro.setError("Informe o bairro.");
            edtBairro.requestFocus();
            return;
        }

        if (cidade.isEmpty()) {
            edtCidade.setError("Informe a cidade.");
            edtCidade.requestFocus();
            return;
        }

        String descricao = spDescricaoEndereco.getSelectedItem().toString();

        EnderecoCliente endereco = new EnderecoCliente();
        endereco.setIdCliente(idCliente);
        endereco.setDescricao(descricao);
        endereco.setLogradouro(logradouro);
        endereco.setNumero(numero.isEmpty() ? null : numero);
        endereco.setBairro(bairro);
        endereco.setCidade(cidade);
        endereco.setComplemento(complemento.isEmpty() ? null : complemento);
        endereco.setReferencia(referencia.isEmpty() ? null : referencia);

        btnSalvarEndereco.setEnabled(false);

        AppExecutors.getDatabaseExecutor().execute(() -> {
            try {
                if (modoEdicao) {
                    endereco.setIdEndereco(idEndereco);
                    appContainer.enderecoClienteService.atualizar(endereco);
                } else {
                    appContainer.enderecoClienteService.cadastrar(endereco);
                }

                runOnUiThread(() -> {
                    Toast.makeText(
                            CadastroEnderecoActivity.this,
                            modoEdicao
                                    ? "Endereço atualizado com sucesso!"
                                    : "Endereço cadastrado com sucesso!",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnSalvarEndereco.setEnabled(true);

                    String mensagem = e.getMessage();

                    if (mensagem == null || mensagem.trim().isEmpty()) {
                        mensagem = "Não foi possível cadastrar o endereço.";
                    }

                    Toast.makeText(
                            CadastroEnderecoActivity.this,
                            mensagem,
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        });
    }

    private void carregarEndereco() {
        AppExecutors.getDatabaseExecutor().execute(() -> {
            try {
                EnderecoCliente endereco =
                        appContainer.enderecoClienteService.buscarPorId(idEndereco);

                if (endereco == null
                        || !Long.valueOf(idCliente).equals(endereco.getIdCliente())) {
                    runOnUiThread(() -> {
                        Toast.makeText(
                                this,
                                "Endereço não encontrado para este cliente.",
                                Toast.LENGTH_LONG
                        ).show();
                        finish();
                    });
                    return;
                }

                runOnUiThread(() -> {
                    edtLogradouro.setText(endereco.getLogradouro());
                    edtNumeroEndereco.setText(endereco.getNumero());
                    edtBairro.setText(endereco.getBairro());
                    edtCidade.setText(endereco.getCidade());
                    edtComplemento.setText(endereco.getComplemento());
                    edtReferencia.setText(endereco.getReferencia());

                    String descricao = endereco.getDescricao();

                    if ("Secundário".equalsIgnoreCase(descricao)) {
                        spDescricaoEndereco.setSelection(1);
                    } else {
                        spDescricaoEndereco.setSelection(0);
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(
                            this,
                            "Não foi possível carregar o endereço.",
                            Toast.LENGTH_LONG
                    ).show();
                    finish();
                });
            }
        });
    }
}