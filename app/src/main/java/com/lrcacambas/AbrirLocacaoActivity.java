package com.lrcacambas;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.snackbar.Snackbar;
import com.lrcacambas.data.local.entity.Cacamba;
import com.lrcacambas.data.local.entity.Cliente;
import com.lrcacambas.data.local.entity.EnderecoCliente;
import com.lrcacambas.data.local.entity.StatusCacamba;
import com.lrcacambas.domain.service.CacambaService;
import com.lrcacambas.domain.service.ClienteService;
import com.lrcacambas.domain.service.EnderecoClienteService;
import com.lrcacambas.domain.service.LocacaoService;
import com.lrcacambas.util.AppContainer;
import com.lrcacambas.util.AppExecutors;
import com.lrcacambas.data.local.entity.Locacao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.text.NumberFormat;

public class AbrirLocacaoActivity extends AppCompatActivity {

    // Componentes da tela
    private TextView txtClienteSelecionado;
    private TextView txtEnderecoSelecionado;
    private Spinner spCacamba;
    private EditText edtValorInicial;
    private EditText edtObservacoes;
    private Button btnSalvarLocacao;
    private Button btnCancelar;
    private boolean atualizandoValor = false;

    // Serviços
    private ClienteService clienteService;
    private EnderecoClienteService enderecoClienteService;
    private CacambaService cacambaService;
    private LocacaoService locacaoService;

    // Dados selecionados
    private Cliente clienteSelecionado;
    private EnderecoCliente enderecoSelecionado;
    private boolean enderecoExcepcionalSelecionado = false;
    private long idClienteInicial = -1;

    // Dados do endereço excepcional
    private String logradouroEnderecoTemporario;
    private String numeroEnderecoTemporario;
    private String bairroEnderecoTemporario;
    private String cidadeEnderecoTemporario;
    private String complementoEnderecoTemporario;
    private String referenciaEnderecoTemporario;

    // Listas utilizadas na tela
    private final List<Cliente> clientesAtivos = new ArrayList<>();
    private final List<EnderecoCliente> enderecosCliente = new ArrayList<>();
    private final List<Cacamba> cacambasDisponiveis = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_abrir_locacao);

        configurarInsets();
        inicializarComponentes();
        configurarMascaraValor();
        inicializarServicos();

        idClienteInicial = getIntent().getLongExtra("idCliente", -1);

        carregarClientes();
        configurarSelecaoEndereco();
        carregarCacambasDisponiveis();
        configurarBotoes();
    }

    // Inicialização

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

            v.setPadding(
                    paddingLeft + systemBars.left,
                    paddingTop + systemBars.top,
                    paddingRight + systemBars.right,
                    paddingBottom + Math.max(systemBars.bottom, teclado.bottom)
            );

            return insets;
        });
    }

    private void inicializarComponentes() {
        txtClienteSelecionado = findViewById(R.id.txtClienteSelecionado);
        txtEnderecoSelecionado = findViewById(R.id.txtEnderecoSelecionado);
        spCacamba = findViewById(R.id.spCacamba);

        edtValorInicial = findViewById(R.id.edtValorInicial);
        edtObservacoes = findViewById(R.id.edtObservacoes);

        btnSalvarLocacao = findViewById(R.id.btnSalvarLocacao);
        btnCancelar = findViewById(R.id.btnCancelar);
    }

    private void inicializarServicos() {
        AppContainer appContainer = new AppContainer(this);

        clienteService = appContainer.clienteService;
        enderecoClienteService = appContainer.enderecoClienteService;
        cacambaService = appContainer.cacambaService;
        locacaoService = appContainer.locacaoService;
    }

    // Clientes

    private void carregarClientes() {
        AppExecutors.getDatabaseExecutor().execute(() -> {
            List<Cliente> lista = clienteService.listarAtivos();

            runOnUiThread(() -> {
                clientesAtivos.clear();
                clientesAtivos.addAll(lista);

                txtClienteSelecionado.setOnClickListener(v ->
                        mostrarDialogoClientes()
                );

                if (idClienteInicial > 0) {
                    for (Cliente cliente : clientesAtivos) {
                        if (cliente.getIdCliente() == idClienteInicial) {
                            clienteSelecionado = cliente;

                            txtClienteSelecionado.setText(cliente.getNome());
                            txtClienteSelecionado.setTextColor(
                                    getColor(R.color.preto)
                            );

                            carregarEnderecosCliente(idClienteInicial);
                            break;
                        }
                    }
                }
            });
        });
    }

    private void mostrarDialogoClientes() {
        if (clientesAtivos.isEmpty()) {
            Toast.makeText(
                    this,
                    "Nenhum cliente ativo cadastrado.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        View dialogView = getLayoutInflater().inflate(
                R.layout.dialog_selecionar_cliente,
                null
        );

        EditText edtBuscarCliente = dialogView.findViewById(
                R.id.edtBuscarCliente
        );

        ListView listaClientes = dialogView.findViewById(
                R.id.listaClientes
        );

        List<Cliente> clientesFiltrados = new ArrayList<>(clientesAtivos);
        List<String> nomesExibidos = new ArrayList<>();

        for (Cliente cliente : clientesFiltrados) {
            nomesExibidos.add(cliente.getNome());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.item_cliente_selecao,
                R.id.txtNomeCliente,
                nomesExibidos
        ) {
            @Override
            public View getView(
                    int position,
                    View convertView,
                    android.view.ViewGroup parent
            ) {
                View item = super.getView(
                        position,
                        convertView,
                        parent
                );

                TextView txtNome = item.findViewById(
                        R.id.txtNomeCliente
                );

                TextView txtTelefone = item.findViewById(
                        R.id.txtTelefoneCliente
                );

                Cliente cliente = clientesFiltrados.get(position);

                txtNome.setText(cliente.getNome());
                txtTelefone.setText(cliente.getTelefone());

                return item;
            }
        };

        listaClientes.setAdapter(adapter);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        listaClientes.setOnItemClickListener((parent, view, position, id) -> {
            clienteSelecionado = clientesFiltrados.get(position);

            txtClienteSelecionado.setText(
                    clienteSelecionado.getNome()
            );

            txtClienteSelecionado.setTextColor(
                    getColor(R.color.preto)
            );

            carregarEnderecosCliente(
                    clienteSelecionado.getIdCliente()
            );

            dialog.dismiss();
        });

        edtBuscarCliente.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after
            ) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count
            ) {
                String pesquisa = s.toString()
                        .trim()
                        .toLowerCase(Locale.ROOT);

                clientesFiltrados.clear();
                nomesExibidos.clear();

                for (Cliente cliente : clientesAtivos) {
                    String nome = cliente.getNome() == null
                            ? ""
                            : cliente.getNome().toLowerCase(Locale.ROOT);

                    String apelido = cliente.getApelido() == null
                            ? ""
                            : cliente.getApelido().toLowerCase(Locale.ROOT);

                    if (nome.contains(pesquisa)
                            || apelido.contains(pesquisa)) {
                        clientesFiltrados.add(cliente);
                        nomesExibidos.add(cliente.getNome());
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(
                android.R.color.transparent
        );
    }

    // Endereços

    private void configurarSelecaoEndereco() {
        txtEnderecoSelecionado.setOnClickListener(v -> {
            if (clienteSelecionado == null) {
                Toast.makeText(
                        this,
                        "Selecione um cliente primeiro.",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            mostrarDialogoEnderecos();
        });
    }

    private void carregarEnderecosCliente(Long idCliente) {
        AppExecutors.getDatabaseExecutor().execute(() -> {
            try {
                List<EnderecoCliente> lista =
                        enderecoClienteService.listarPorCliente(idCliente);

                runOnUiThread(() -> {
                    String mensagem = lista.isEmpty()
                            ? "Nenhum endereço cadastrado"
                            : "Selecione um endereço";

                    carregarEnderecos(lista, mensagem);
                });

            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(
                        this,
                        e.getMessage(),
                        Toast.LENGTH_SHORT
                ).show());
            }
        });
    }

    private void carregarEnderecos(
            List<EnderecoCliente> lista,
            String mensagemInicial
    ) {
        enderecosCliente.clear();
        enderecosCliente.addAll(lista);

        enderecoSelecionado = null;
        enderecoExcepcionalSelecionado = false;

        logradouroEnderecoTemporario = null;
        numeroEnderecoTemporario = null;
        bairroEnderecoTemporario = null;
        cidadeEnderecoTemporario = null;
        complementoEnderecoTemporario = null;
        referenciaEnderecoTemporario = null;

        txtEnderecoSelecionado.setText(mensagemInicial);
        txtEnderecoSelecionado.setTextColor(
                getColor(R.color.cinza_hint)
        );
    }

    private void mostrarDialogoEnderecos() {
        View dialogView = getLayoutInflater().inflate(
                R.layout.dialog_selecionar_endereco_locacao,
                null
        );

        LinearLayout layoutListaEnderecos = dialogView.findViewById(
                R.id.layoutListaEnderecos
        );

        LinearLayout layoutFormularioEndereco = dialogView.findViewById(
                R.id.layoutFormularioEndereco
        );

        ListView listaEnderecos = dialogView.findViewById(
                R.id.listaEnderecosLocacao
        );

        Button btnOutroEndereco = dialogView.findViewById(
                R.id.btnOutroEndereco
        );

        Button btnConfirmarEndereco = dialogView.findViewById(
                R.id.btnConfirmarEndereco
        );

        Button btnVoltarEnderecos = dialogView.findViewById(
                R.id.btnVoltarEnderecos
        );

        EditText edtLogradouro = dialogView.findViewById(
                R.id.edtNovoLogradouro
        );

        EditText edtNumero = dialogView.findViewById(
                R.id.edtNovoNumero
        );

        EditText edtBairro = dialogView.findViewById(
                R.id.edtNovoBairro
        );

        EditText edtCidade = dialogView.findViewById(
                R.id.edtNovoCidade
        );

        EditText edtComplemento = dialogView.findViewById(
                R.id.edtNovoComplemento
        );

        EditText edtReferencia = dialogView.findViewById(
                R.id.edtNovoReferencia
        );

        ArrayAdapter<EnderecoCliente> adapter =
                new ArrayAdapter<EnderecoCliente>(
                        this,
                        R.layout.item_endereco_locacao,
                        R.id.txtDescricaoEndereco,
                        enderecosCliente
                ) {
                    @Override
                    public View getView(
                            int position,
                            View convertView,
                            android.view.ViewGroup parent
                    ) {
                        View view = convertView;

                        if (view == null) {
                            view = getLayoutInflater().inflate(
                                    R.layout.item_endereco_locacao,
                                    parent,
                                    false
                            );
                        }

                        EnderecoCliente endereco = getItem(position);

                        TextView txtDescricao = view.findViewById(
                                R.id.txtDescricaoEndereco
                        );

                        TextView txtDetalhes = view.findViewById(
                                R.id.txtDetalhesEndereco
                        );

                        txtDescricao.setText(endereco.getDescricao());

                        String detalhes = endereco.getLogradouro();

                        if (endereco.getNumero() != null
                                && !endereco.getNumero().trim().isEmpty()) {
                            detalhes += ", " + endereco.getNumero();
                        }

                        detalhes += " - " + endereco.getBairro()
                                + ", " + endereco.getCidade();

                        txtDetalhes.setText(detalhes);

                        return view;
                    }
                };

        listaEnderecos.setAdapter(adapter);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        listaEnderecos.setOnItemClickListener(
                (parent, view, position, id) -> {
                    enderecoSelecionado = enderecosCliente.get(position);
                    enderecoExcepcionalSelecionado = false;

                    logradouroEnderecoTemporario = null;
                    numeroEnderecoTemporario = null;
                    bairroEnderecoTemporario = null;
                    cidadeEnderecoTemporario = null;
                    complementoEnderecoTemporario = null;
                    referenciaEnderecoTemporario = null;

                    String resumo = enderecoSelecionado.getLogradouro();

                    if (enderecoSelecionado.getNumero() != null
                            && !enderecoSelecionado.getNumero().trim().isEmpty()) {
                        resumo += ", " + enderecoSelecionado.getNumero();
                    }

                    resumo += " - " + enderecoSelecionado.getBairro()
                            + ", " + enderecoSelecionado.getCidade();

                    txtEnderecoSelecionado.setText(resumo);
                    txtEnderecoSelecionado.setTextColor(
                            getColor(R.color.preto)
                    );

                    dialog.dismiss();
                }
        );

        btnOutroEndereco.setOnClickListener(v -> {
            layoutListaEnderecos.setVisibility(View.GONE);
            layoutFormularioEndereco.setVisibility(View.VISIBLE);
        });

        btnVoltarEnderecos.setOnClickListener(v -> {
            layoutFormularioEndereco.setVisibility(View.GONE);
            layoutListaEnderecos.setVisibility(View.VISIBLE);
        });

        btnConfirmarEndereco.setOnClickListener(v -> {
            String logradouro = edtLogradouro.getText().toString().trim();
            String numero = edtNumero.getText().toString().trim();
            String bairro = edtBairro.getText().toString().trim();
            String cidade = edtCidade.getText().toString().trim();

            String complemento = edtComplemento.getText().toString().trim();
            String referencia = edtReferencia.getText().toString().trim();

            if (logradouro.isEmpty()) {
                edtLogradouro.setError("Informe o logradouro.");
                edtLogradouro.requestFocus();
                return;
            }

            if (numero.isEmpty()) {
                edtNumero.setError("Informe o número.");
                edtNumero.requestFocus();
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

            logradouroEnderecoTemporario = logradouro;
            numeroEnderecoTemporario = numero;
            bairroEnderecoTemporario = bairro;
            cidadeEnderecoTemporario = cidade;
            complementoEnderecoTemporario = complemento;
            referenciaEnderecoTemporario = referencia;

            enderecoSelecionado = null;
            enderecoExcepcionalSelecionado = true;

            String resumo = logradouro + ", " + numero
                    + " - " + bairro + ", " + cidade;

            txtEnderecoSelecionado.setText(resumo);
            txtEnderecoSelecionado.setTextColor(
                    getColor(R.color.preto)
            );

            dialog.dismiss();
        });

        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(
                android.R.color.transparent
        );
    }

    // Caçambas

    private void carregarCacambasDisponiveis() {
        AppExecutors.getDatabaseExecutor().execute(() -> {
            try {
                List<Cacamba> lista = cacambaService.listarPorStatus(
                        StatusCacamba.DISPONIVEL
                );

                runOnUiThread(() -> {
                    cacambasDisponiveis.clear();
                    cacambasDisponiveis.addAll(lista);

                    List<String> opcoes = new ArrayList<>();
                    opcoes.add("Selecione uma caçamba");

                    for (Cacamba cacamba : cacambasDisponiveis) {
                        opcoes.add(String.format(
                                Locale.getDefault(),
                                "Caçamba %03d",
                                cacamba.getCodigo()
                        ));
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                            this,
                            android.R.layout.simple_spinner_item,
                            opcoes
                    ) {
                        @Override
                        public View getView(
                                int position,
                                View convertView,
                                android.view.ViewGroup parent
                        ) {
                            TextView item = (TextView) super.getView(
                                    position, convertView, parent
                            );

                            item.setTextColor(getColor(R.color.cinza_hint));

                            return item;
                        }
                    };

                    adapter.setDropDownViewResource(
                            R.layout.item_spinner_cacamba_dropdown
                    );

                    spCacamba.setAdapter(adapter);
                    spCacamba.setSelection(0);
                    spCacamba.setEnabled(!cacambasDisponiveis.isEmpty());
                });

            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(
                        this,
                        e.getMessage(),
                        Toast.LENGTH_SHORT
                ).show());
            }
        });
    }

    private void configurarBotoes() {
        btnSalvarLocacao.setOnClickListener(v -> salvarLocacao());

        btnCancelar.setOnClickListener(v -> finish());
    }

    private void configurarMascaraValor() {
        edtValorInicial.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after
            ) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count
            ) {
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (atualizandoValor) {
                    return;
                }

                atualizandoValor = true;

                String numeros = s.toString().replaceAll("\\D", "");

                if (numeros.isEmpty()) {
                    edtValorInicial.setText("");
                    atualizandoValor = false;
                    return;
                }

                try {
                    long centavos = Long.parseLong(numeros);

                    NumberFormat formato = NumberFormat.getCurrencyInstance(
                            new Locale("pt", "BR")
                    );

                    String valorFormatado = formato.format(
                            BigDecimal.valueOf(centavos, 2)
                    );

                    edtValorInicial.setText(valorFormatado);
                    edtValorInicial.setSelection(
                            edtValorInicial.getText().length()
                    );

                } catch (NumberFormatException e) {
                    edtValorInicial.setError("Valor muito alto.");
                }

                atualizandoValor = false;
            }
        });
    }

    private void salvarLocacao() {

        // Verifica se um cliente foi selecionado
        if (clienteSelecionado == null) {
            Toast.makeText(
                    this,
                    "Selecione um cliente.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        // Verifica se um endereço foi selecionado
        if (enderecoSelecionado == null && !enderecoExcepcionalSelecionado) {
            Toast.makeText(
                    this,
                    "Selecione ou informe um endereço.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        // Verifica se uma caçamba foi selecionada
        int posicaoCacamba = spCacamba.getSelectedItemPosition();

        if (posicaoCacamba <= 0) {
            Toast.makeText(
                    this,
                    "Selecione uma caçamba.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        // Valida o valor inicial
        String valorTexto = edtValorInicial.getText().toString().trim();
        String somenteDigitos = valorTexto.replaceAll("\\D", "");

        if (somenteDigitos.isEmpty()) {
            edtValorInicial.setError("Informe o valor da locação.");
            edtValorInicial.requestFocus();
            return;
        }

        long valorEmCentavos;

        try {
            valorEmCentavos = Long.parseLong(somenteDigitos);

            if (valorEmCentavos <= 0) {
                edtValorInicial.setError("Informe um valor maior que zero.");
                edtValorInicial.requestFocus();
                return;
            }

        } catch (NumberFormatException e) {
            edtValorInicial.setError("Informe um valor válido.");
            edtValorInicial.requestFocus();
            return;
        }

        // Monta o objeto da locação
        Locacao locacao = new Locacao();

        locacao.setIdCliente(clienteSelecionado.getIdCliente());
        locacao.setValorInicial(valorEmCentavos);

        String observacoes = edtObservacoes.getText().toString().trim();
        locacao.setObservacoes(observacoes.isEmpty() ? null : observacoes);

        // Define o endereço da locação
        if (enderecoSelecionado != null) {

            locacao.setIdEndereco(enderecoSelecionado.getIdEndereco());

        } else {

            locacao.setIdEndereco(null);
            locacao.setLogradouro(logradouroEnderecoTemporario);
            locacao.setNumero(numeroEnderecoTemporario);
            locacao.setBairro(bairroEnderecoTemporario);
            locacao.setCidade(cidadeEnderecoTemporario);
            locacao.setComplemento(complementoEnderecoTemporario);
            locacao.setReferencia(referenciaEnderecoTemporario);
        }

        // Recupera a caçamba selecionada
        Cacamba cacambaSelecionada =
                cacambasDisponiveis.get(posicaoCacamba - 1);

        // Evita cliques repetidos durante o salvamento
        btnSalvarLocacao.setEnabled(false);

        AppExecutors.getDatabaseExecutor().execute(() -> {

            try {
                locacaoService.abrirLocacao(
                        locacao,
                        cacambaSelecionada.getCodigo()
                );

                runOnUiThread(() -> {
                    Toast.makeText(
                            this,
                            "Locação cadastrada com sucesso!",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();
                });

            } catch (Exception e) {

                runOnUiThread(() -> {
                    btnSalvarLocacao.setEnabled(true);

                    Toast.makeText(
                            this,
                            e.getMessage() != null
                                    ? e.getMessage()
                                    : "Erro ao cadastrar locação.",
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        });
    }
}