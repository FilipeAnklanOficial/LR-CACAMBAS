package com.lrcacambas;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.lrcacambas.data.local.entity.Cliente;
import com.lrcacambas.util.AppContainer;
import com.lrcacambas.util.AppExecutors;
import com.lrcacambas.util.DialogHelper;
import com.lrcacambas.data.local.entity.EnderecoCliente;

public class CadastroClienteActivity extends AppCompatActivity {

    private AppContainer appContainer;

    private EditText edtLogradouro;
    private EditText edtNumeroEndereco;
    private EditText edtBairro;
    private EditText edtCidade;
    private EditText edtComplemento;
    private EditText edtReferencia;

    private long idCliente = -1;
    private boolean modoEdicao = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.enableEdgeToEdge(getWindow());

        idCliente = getIntent().getLongExtra("idCliente", -1);
        modoEdicao = idCliente > 0;

        setContentView(modoEdicao
                ? R.layout.activity_editar_cliente
                : R.layout.activity_cadastro_cliente);

        appContainer = new AppContainer(getApplicationContext());

        configurarMascaraTelefone();
        configurarMascaraDocumento();

        MaterialButton btnSalvar = findViewById(R.id.btnSalvarCliente);

        if (modoEdicao) {
            btnSalvar.setText("Salvar alterações");
            carregarCliente();
        }

        btnSalvar.setOnClickListener(v -> confirmarCadastro());

        findViewById(R.id.btnCancelarCliente)
                .setOnClickListener(v -> finish());

        View root = findViewById(R.id.main);

        int paddingLeft = root.getPaddingLeft();
        int paddingTop = root.getPaddingTop();
        int paddingRight = root.getPaddingRight();
        int paddingBottomOriginal = root.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
            );

            Insets teclado = insets.getInsets(
                    WindowInsetsCompat.Type.ime()
            );

            int paddingBottom = Math.max(
                    systemBars.bottom,
                    teclado.bottom
            );

            v.setPadding(
                    paddingLeft + systemBars.left,
                    paddingTop + systemBars.top,
                    paddingRight + systemBars.right,
                    paddingBottomOriginal + paddingBottom
            );

            return insets;
        });
    }

    private void inicializarCamposEndereco() {
        edtLogradouro = findViewById(R.id.edtLogradouroCliente);
        edtNumeroEndereco = findViewById(R.id.edtNumeroEnderecoCliente);
        edtBairro = findViewById(R.id.edtBairroCliente);
        edtCidade = findViewById(R.id.edtCidadeCliente);
        edtComplemento = findViewById(R.id.edtComplementoCliente);
        edtReferencia = findViewById(R.id.edtReferenciaCliente);
    }

    private void configurarMascaraDocumento() {

        EditText edtDocumento = findViewById(R.id.edtDocumentoCliente);

        edtDocumento.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {

                String documento = editable.toString()
                        .replaceAll("\\D", "");

                if (documento.length() > 14) {
                    documento = documento.substring(0, 14);
                }

                String formatado = formatarDocumento(documento);

                if (!formatado.equals(editable.toString())) {

                    edtDocumento.removeTextChangedListener(this);

                    edtDocumento.setText(formatado);
                    edtDocumento.setSelection(formatado.length());

                    edtDocumento.addTextChangedListener(this);
                }
            }
        });
    }

    private void configurarMascaraTelefone() {

        EditText edtTelefone = findViewById(R.id.edtTelefoneCliente);

        edtTelefone.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {

                String telefone = editable.toString()
                        .replaceAll("\\D", "");

                if (telefone.length() > 11) {
                    telefone = telefone.substring(0, 11);
                }

                String formatado = formatarTelefone(telefone);

                if (!formatado.equals(editable.toString())) {

                    edtTelefone.removeTextChangedListener(this);

                    edtTelefone.setText(formatado);
                    edtTelefone.setSelection(formatado.length());

                    edtTelefone.addTextChangedListener(this);
                }
            }
        });
    }

    private String formatarTelefone(String telefone) {

        if (telefone.isEmpty()) {
            return "";
        }

        StringBuilder resultado = new StringBuilder();

        resultado.append("(");
        resultado.append(
                telefone.substring(0, Math.min(2, telefone.length()))
        );

        if (telefone.length() >= 3) {

            resultado.append(") ");

            boolean celular = telefone.length() > 10;

            int digitosAntesTraco = celular ? 7 : 6;
            int fim = Math.min(telefone.length(), digitosAntesTraco);

            resultado.append(telefone, 2, fim);

            if (telefone.length() > digitosAntesTraco) {
                resultado.append("-");
                resultado.append(telefone.substring(digitosAntesTraco));
            }
        }

        return resultado.toString();
    }

    private String formatarDocumento(String documento) {

        StringBuilder resultado = new StringBuilder();

        boolean cnpj = documento.length() > 11;

        for (int i = 0; i < documento.length(); i++) {

            resultado.append(documento.charAt(i));

            if (i == documento.length() - 1) {
                continue;
            }

            if (cnpj) {

                if (i == 1 || i == 4) {
                    resultado.append(".");
                } else if (i == 7) {
                    resultado.append("/");
                } else if (i == 11) {
                    resultado.append("-");
                }

            } else {

                if (i == 2 || i == 5) {
                    resultado.append(".");
                } else if (i == 8) {
                    resultado.append("-");
                }
            }
        }

        return resultado.toString();
    }

    private void confirmarCadastro() {
        String titulo = modoEdicao
                ? "Confirmar alterações"
                : "Confirmar cadastro";

        String mensagem = modoEdicao
                ? "Deseja realmente salvar as alterações deste cliente?"
                : "Deseja realmente cadastrar este cliente?";

        DialogHelper.mostrarConfirmacao(
                this,
                titulo,
                mensagem,
                this::salvarCliente
        );
    }

    private void salvarCliente() {

        EditText edtNome = findViewById(R.id.edtNomeCliente);
        EditText edtTelefone = findViewById(R.id.edtTelefoneCliente);
        EditText edtDocumento = findViewById(R.id.edtDocumentoCliente);
        EditText edtApelido = findViewById(R.id.edtApelidoCliente);

        Cliente cliente = new Cliente();

        cliente.setNome(edtNome.getText().toString().trim());
        cliente.setTelefone(edtTelefone.getText().toString().trim());
        cliente.setDocumento(edtDocumento.getText().toString().trim());
        cliente.setApelido(edtApelido.getText().toString().trim());

        if (modoEdicao) {
            cliente.setIdCliente(idCliente);
        }

        AppExecutors.getDatabaseExecutor().execute(() -> {

            try {

                if (modoEdicao) {

                    appContainer.clienteService.atualizar(cliente);

                } else {

                    EditText edtLogradouro = findViewById(R.id.edtLogradouroCliente);
                    EditText edtNumero = findViewById(R.id.edtNumeroEnderecoCliente);
                    EditText edtBairro = findViewById(R.id.edtBairroCliente);
                    EditText edtCidade = findViewById(R.id.edtCidadeCliente);
                    EditText edtComplemento = findViewById(R.id.edtComplementoCliente);
                    EditText edtReferencia = findViewById(R.id.edtReferenciaCliente);

                    String logradouro = edtLogradouro.getText().toString().trim();
                    String numero = edtNumero.getText().toString().trim();
                    String bairro = edtBairro.getText().toString().trim();
                    String cidade = edtCidade.getText().toString().trim();
                    String complemento = edtComplemento.getText().toString().trim();
                    String referencia = edtReferencia.getText().toString().trim();

                    if (logradouro.isEmpty() || bairro.isEmpty() || cidade.isEmpty()) {
                        runOnUiThread(() -> Toast.makeText(
                                this,
                                "Preencha o logradouro, bairro e cidade.",
                                Toast.LENGTH_LONG
                        ).show());
                        return;
                    }

                    EnderecoCliente endereco = new EnderecoCliente();

                    endereco.setDescricao("Principal");
                    endereco.setLogradouro(logradouro);
                    endereco.setNumero(numero.isEmpty() ? null : numero);
                    endereco.setBairro(bairro);
                    endereco.setCidade(cidade);
                    endereco.setComplemento(complemento.isEmpty() ? null : complemento);
                    endereco.setReferencia(referencia.isEmpty() ? null : referencia);

                    appContainer.database.runInTransaction(() -> {

                        long novoIdCliente =
                                appContainer.clienteService.cadastrar(cliente);

                        endereco.setIdCliente(novoIdCliente);

                        appContainer.enderecoClienteService.cadastrar(endereco);
                    });
                }

                runOnUiThread(() -> {

                    DialogHelper.mostrarSucesso(
                            CadastroClienteActivity.this,
                            modoEdicao ? "Alterações realizadas!" : "Cadastro realizado!",
                            modoEdicao
                                    ? "Os dados do cliente foram atualizados com sucesso."
                                    : "O cliente e seu endereço foram cadastrados com sucesso.",
                            this::finish
                    );
                });

            } catch (Exception e) {

                runOnUiThread(() -> Toast.makeText(
                        CadastroClienteActivity.this,
                        e.getMessage() != null
                                ? e.getMessage()
                                : modoEdicao
                                ? "Erro ao atualizar cliente."
                                : "Erro ao cadastrar cliente.",
                        Toast.LENGTH_LONG
                ).show());
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

                    EditText edtNome = findViewById(R.id.edtNomeCliente);
                    EditText edtTelefone = findViewById(R.id.edtTelefoneCliente);
                    EditText edtDocumento = findViewById(R.id.edtDocumentoCliente);
                    EditText edtApelido = findViewById(R.id.edtApelidoCliente);

                    edtNome.setText(cliente.getNome());
                    edtTelefone.setText(cliente.getTelefone());
                    edtDocumento.setText(cliente.getDocumento());
                    edtApelido.setText(cliente.getApelido());
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
}