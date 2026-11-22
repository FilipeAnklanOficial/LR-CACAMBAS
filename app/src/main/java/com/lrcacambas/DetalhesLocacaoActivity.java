package com.lrcacambas;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.text.InputType;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.app.AlertDialog;

import com.lrcacambas.data.local.entity.Cacamba;
import com.lrcacambas.data.local.entity.Cliente;
import com.lrcacambas.data.local.entity.Locacao;
import com.lrcacambas.data.local.entity.MovimentacaoCacamba;
import com.lrcacambas.data.local.entity.StatusCacamba;
import com.lrcacambas.domain.service.CacambaService;
import com.lrcacambas.domain.service.ClienteService;
import com.lrcacambas.domain.service.LocacaoService;
import com.lrcacambas.domain.service.RecebimentoService;
import com.lrcacambas.util.AppContainer;
import com.lrcacambas.util.AppExecutors;
import com.lrcacambas.data.local.entity.Recebimento;
import com.lrcacambas.data.local.entity.MotivoTroca;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.math.BigDecimal;

public class DetalhesLocacaoActivity extends AppCompatActivity {

    private Long idLocacao;

    private TextView txtNomeCliente;
    private TextView txtEnderecoLocacao;
    private TextView txtDataInicio;
    private TextView txtStatusLocacao;

    private TextView txtValorAtual;
    private TextView txtSaldoPendente;
    private TextView txtValorRecebido;
    private TextView txtTotalPagamentos;

    private Button btnTrocarCacamba;
    private Button btnFinalizarLocacao;

    private LinearLayout containerHistoricoCacambas;
    private LinearLayout containerHistoricoRecebimentos;

    private ClienteService clienteService;
    private LocacaoService locacaoService;
    private RecebimentoService recebimentoService;
    private CacambaService cacambaService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_locacao);

        WindowCompat.enableEdgeToEdge(getWindow());
        configurarInsets();

        receberDados();
        inicializarComponentes();
        inicializarServicos();
        configurarBotaoTrocarCacamba();
        configurarBotaoFinalizarLocacao();

        if (idLocacao != null) {
            carregarDadosLocacao();
            carregarHistoricoCacambas();
            carregarHistoricoRecebimentos();
        }
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

            v.setPadding(
                    paddingLeft + systemBars.left,
                    paddingTop + systemBars.top,
                    paddingRight + systemBars.right,
                    paddingBottom + Math.max(systemBars.bottom, teclado.bottom)
            );

            return insets;
        });
    }

    private void receberDados() {
        long idRecebido = getIntent().getLongExtra("ID_LOCACAO", -1L);

        if (idRecebido == -1L) {
            Toast.makeText(
                    this,
                    "Não foi possível identificar a locação.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        idLocacao = idRecebido;
    }

    private void inicializarComponentes() {
        txtNomeCliente = findViewById(R.id.txtNomeCliente);
        txtEnderecoLocacao = findViewById(R.id.txtEnderecoLocacao);
        txtDataInicio = findViewById(R.id.txtDataInicio);
        txtStatusLocacao = findViewById(R.id.txtStatusLocacao);

        txtValorAtual = findViewById(R.id.txtValorAtual);
        txtSaldoPendente = findViewById(R.id.txtSaldoPendente);
        txtValorRecebido = findViewById(R.id.txtValorRecebido);
        txtTotalPagamentos = findViewById(R.id.txtTotalPagamentos);

        containerHistoricoCacambas = findViewById(R.id.containerHistoricoCacambas);
        containerHistoricoRecebimentos = findViewById(R.id.containerHistoricoRecebimentos);

        btnTrocarCacamba = findViewById(R.id.btnTrocarCacamba);
        btnFinalizarLocacao = findViewById(R.id.btnFinalizarLocacao);
    }

    private void inicializarServicos() {
        AppContainer appContainer = new AppContainer(this);

        clienteService = appContainer.clienteService;
        locacaoService = appContainer.locacaoService;
        recebimentoService = appContainer.recebimentoService;
        cacambaService = appContainer.cacambaService;
    }

    private void carregarDadosLocacao() {
        AppExecutors.getDatabaseExecutor().execute(() -> {
            try {
                Locacao locacao = locacaoService.buscarPorId(idLocacao);

                if (locacao == null) {
                    runOnUiThread(() -> {
                        Toast.makeText(
                                this,
                                "Locação não encontrada.",
                                Toast.LENGTH_SHORT
                        ).show();

                        finish();
                    });
                    return;
                }

                Cliente cliente = clienteService.buscarPorId(
                        locacao.getIdCliente()
                );

                Long valorAtual;

                if (locacao.getStatus().name().equals("FINALIZADA")
                        && locacao.getValorTotalFinal() != null) {

                    valorAtual = locacao.getValorTotalFinal();

                } else {
                    valorAtual = locacaoService.calcularValorTotal(idLocacao);
                }

                Long valorRecebido =
                        recebimentoService.calcularTotalRecebido(idLocacao);

                Long saldoPendente =
                        recebimentoService.calcularSaldoPendente(idLocacao);

                List<?> recebimentos =
                        recebimentoService.listarPorLocacao(idLocacao);

                runOnUiThread(() -> {
                    preencherInformacoes(locacao, cliente);

                    preencherResumoFinanceiro(
                            valorAtual,
                            saldoPendente,
                            valorRecebido,
                            recebimentos.size()
                    );
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(
                                this,
                                "Erro ao carregar os dados da locação: "
                                        + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
            }
        });
    }

    private void preencherInformacoes(Locacao locacao, Cliente cliente) {
        txtNomeCliente.setText(
                cliente != null
                        ? cliente.getNome()
                        : "Cliente não encontrado"
        );

        txtEnderecoLocacao.setText(formatarEndereco(locacao));

        if (locacao.getDataInicio() != null) {
            DateTimeFormatter formato =
                    DateTimeFormatter.ofPattern("dd/MM/yyyy");

            txtDataInicio.setText(
                    "Início: " + locacao.getDataInicio().format(formato)
            );
        }

        String status = locacao.getStatus().name();

        if (status.equals("ATIVA")) {
            txtStatusLocacao.setText("Status: Ativa");
            txtStatusLocacao.setTextColor(
                    getColor(android.R.color.holo_green_dark)
            );
        } else {
            txtStatusLocacao.setText("Status: Finalizada");
            txtStatusLocacao.setTextColor(
                    getColor(android.R.color.darker_gray)
            );
        }

        btnTrocarCacamba.setVisibility(
                status.equals("ATIVA") ? View.VISIBLE : View.GONE
        );

        btnFinalizarLocacao.setVisibility(
                status.equals("ATIVA") ? View.VISIBLE : View.GONE
        );
    }

    private void preencherResumoFinanceiro(
            Long valorAtual,
            Long saldoPendente,
            Long valorRecebido,
            int totalPagamentos
    ) {
        NumberFormat formato =
                NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        txtValorAtual.setText(formatarMoeda(valorAtual, formato));
        txtSaldoPendente.setText(formatarMoeda(saldoPendente, formato));
        txtValorRecebido.setText(formatarMoeda(valorRecebido, formato));
        txtTotalPagamentos.setText(String.valueOf(totalPagamentos));
    }

    private String formatarMoeda(Long valor, NumberFormat formato) {
        return formato.format(
                (valor == null ? 0L : valor) / 100.0
        );
    }

    private String formatarEndereco(Locacao locacao) {
        StringBuilder endereco = new StringBuilder();

        if (locacao.getLogradouro() != null) {
            endereco.append(locacao.getLogradouro());
        }

        if (locacao.getNumero() != null
                && !locacao.getNumero().trim().isEmpty()) {
            endereco.append(", ").append(locacao.getNumero());
        }

        if (locacao.getBairro() != null
                && !locacao.getBairro().trim().isEmpty()) {
            endereco.append(" - ").append(locacao.getBairro());
        }

        if (locacao.getCidade() != null
                && !locacao.getCidade().trim().isEmpty()) {
            endereco.append(" - ").append(locacao.getCidade());
        }

        return endereco.length() > 0
                ? endereco.toString()
                : "Endereço não informado";
    }

    private void configurarBotaoTrocarCacamba() {
        btnTrocarCacamba.setOnClickListener(v ->
                mostrarDialogoTrocaCacamba()
        );
    }

    private void configurarBotaoFinalizarLocacao() {
        btnFinalizarLocacao.setOnClickListener(v ->
                mostrarDialogoFinalizarLocacao()
        );
    }

    // HISTÓRICO DE CAÇAMBAS

    private void carregarHistoricoCacambas() {
        AppExecutors.getDatabaseExecutor().execute(() -> {
            try {
                Locacao locacao = locacaoService.buscarPorId(idLocacao);

                if (locacao == null) {
                    runOnUiThread(() ->
                            adicionarMensagemHistorico(
                                    "Locação não encontrada."
                            )
                    );
                    return;
                }

                Long valorInicial = locacao.getValorInicial();

                List<MovimentacaoCacamba> movimentacoes =
                        locacaoService.listarMovimentacoesPorLocacao(idLocacao);

                List<Cacamba> cacambas = new ArrayList<>();

                for (MovimentacaoCacamba movimentacao : movimentacoes) {
                    cacambas.add(
                            cacambaService.buscarPorId(
                                    movimentacao.getIdCacamba()
                            )
                    );
                }

                runOnUiThread(() -> {
                    containerHistoricoCacambas.removeAllViews();

                    if (movimentacoes.isEmpty()) {
                        adicionarMensagemHistorico(
                                "Nenhuma movimentação de caçamba encontrada."
                        );
                        return;
                    }

                    for (int i = 0; i < movimentacoes.size(); i++) {
                        MovimentacaoCacamba movimentacao = movimentacoes.get(i);
                        Cacamba cacamba = cacambas.get(i);

                        containerHistoricoCacambas.addView(
                                criarCardHistoricoCacamba(
                                        movimentacao,
                                        cacamba,
                                        i == 0,
                                        valorInicial,
                                        i > 0 ? movimentacoes.get(i - 1) : null
                                )
                        );
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        adicionarMensagemHistorico(
                                "Não foi possível carregar o histórico."
                        )
                );
            }
        });
    }

    private View criarCardHistoricoCacamba(
            MovimentacaoCacamba movimentacao,
            Cacamba cacamba,
            boolean primeiraMovimentacao,
            Long valorInicial,
            MovimentacaoCacamba trocaAnterior
    ) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(16), dp(18), dp(12));

        GradientDrawable fundo = new GradientDrawable();
        fundo.setColor(Color.WHITE);
        fundo.setCornerRadius(dp(16));
        card.setBackground(fundo);

        LinearLayout.LayoutParams parametros =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        parametros.setMargins(0, 0, 0, dp(14));
        card.setLayoutParams(parametros);

        String codigo = cacamba != null
                ? String.format(
                Locale.getDefault(),
                "%03d",
                cacamba.getCodigo()
        )
                : "---";

        card.addView(criarTexto(
                "Caçamba #" + codigo,
                19,
                Color.rgb(8, 43, 80),
                true
        ));

        boolean emUso = movimentacao.getDataRetirada() == null;

        card.addView(criarTexto(
                emUso ? "Em uso" : "Retirada",
                14,
                emUso ? Color.rgb(30, 120, 65) : Color.rgb(250, 0, 0),
                true
        ));

        DateTimeFormatter formato =
                DateTimeFormatter.ofPattern("dd/MM/yyyy");

        if (movimentacao.getDataEntrega() != null) {
            card.addView(criarTexto(
                    "Entrega: "
                            + movimentacao.getDataEntrega().format(formato),
                    14,
                    Color.DKGRAY,
                    false
            ));
        }

        if (!emUso && movimentacao.getDataRetirada() != null) {
            card.addView(criarTexto(
                    "Retirada: "
                            + movimentacao.getDataRetirada().format(formato),
                    14,
                    Color.DKGRAY,
                    false
            ));
        }

        long dias = calcularDiasAlugada(
                movimentacao.getDataEntrega(),
                emUso ? LocalDate.now() : movimentacao.getDataRetirada()
        );

        card.addView(criarTexto(
                "Alugada por " + dias + " dias",
                17,
                Color.rgb(8, 43, 80),
                true
        ));

        NumberFormat formatoMoeda =
                NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        // VALOR DA CAÇAMBA
        if (primeiraMovimentacao) {

            card.addView(criarTexto(
                    "Cobrança inicial: "
                            + formatarMoeda(valorInicial, formatoMoeda),
                    14,
                    Color.DKGRAY,
                    false
            ));

        } else if (trocaAnterior != null) {

            if (trocaAnterior.isGeraCobranca()) {

                card.addView(criarTexto(
                        "Cobrança adicional: "
                                + formatarMoeda(
                                trocaAnterior.getValorAdicional(),
                                formatoMoeda
                        ),
                        14,
                        Color.DKGRAY,
                        false
                ));

            } else {

                card.addView(criarTexto(
                        "Sem cobrança adicional",
                        14,
                        Color.DKGRAY,
                        false
                ));
            }
        }

        // INFORMAÇÕES DA TROCA QUE RETIROU ESTA CAÇAMBA
        if (!emUso && movimentacao.getMotivoTroca() != null) {

            String motivo;

            switch (movimentacao.getMotivoTroca()) {
                case CHEIA:
                    motivo = "Caçamba cheia";
                    break;

                case PROBLEMA_OPERACIONAL:
                    motivo = "Problema operacional";
                    break;

                default:
                    motivo = "Não informado";
                    break;
            }

            card.addView(criarTexto(
                    "Motivo da troca: " + motivo,
                    14,
                    Color.DKGRAY,
                    false
            ));

            if (movimentacao.getObservacao() != null
                    && !movimentacao.getObservacao().trim().isEmpty()) {

                card.addView(criarTexto(
                        "Observação: " + movimentacao.getObservacao(),
                        14,
                        Color.DKGRAY,
                        false
                ));
            }
        }

        return card;
    }

    private TextView criarTexto(
            String texto,
            int tamanho,
            int cor,
            boolean negrito
    ) {
        TextView view = new TextView(this);
        view.setText(texto);
        view.setTextSize(tamanho);
        view.setTextColor(cor);

        if (negrito) {
            view.setTypeface(null, Typeface.BOLD);
        }

        view.setPadding(0, 0, 0, dp(7));

        return view;
    }

    private long calcularDiasAlugada(
            LocalDate dataEntrega,
            LocalDate dataRetirada
    ) {
        if (dataEntrega == null || dataRetirada == null) {
            return 0;
        }

        return ChronoUnit.DAYS.between(dataEntrega, dataRetirada);
    }

    private void adicionarMensagemHistorico(String mensagem) {
        containerHistoricoCacambas.removeAllViews();

        TextView texto = criarTexto(
                mensagem,
                15,
                Color.DKGRAY,
                false
        );

        containerHistoricoCacambas.addView(texto);
    }

    private void exibirDialogoTroca(
            Locacao locacao,
            Cacamba cacambaAtual,
            List<Cacamba> cacambasDisponiveis
    ) {
        View dialogView = getLayoutInflater().inflate(
                R.layout.dialog_trocar_cacamba,
                null
        );

        TextView txtCacambaAtualTroca =
                dialogView.findViewById(R.id.txtCacambaAtualTroca);

        Spinner spinnerNovaCacamba =
                dialogView.findViewById(R.id.spinnerNovaCacamba);

        Spinner spinnerMotivoTroca =
                dialogView.findViewById(R.id.spinnerMotivoTroca);

        EditText edtValorAdicionalTroca =
                dialogView.findViewById(R.id.edtValorAdicionalTroca);

        EditText edtObservacaoTroca =
                dialogView.findViewById(R.id.edtObservacaoTroca);

        Button btnConfirmarTroca =
                dialogView.findViewById(R.id.btnConfirmarTroca);

        Button btnCancelarTroca =
                dialogView.findViewById(R.id.btnCancelarTroca);

        // Exibe a caçamba atualmente vinculada à locação
        String codigoAtual = String.format(
                Locale.getDefault(),
                "%03d",
                cacambaAtual.getCodigo()
        );

        txtCacambaAtualTroca.setText(
                "Caçamba atual: #" + codigoAtual
        );

        // Preenche a lista de caçambas disponíveis
        List<String> opcoesCacambas = new ArrayList<>();

        for (Cacamba cacamba : cacambasDisponiveis) {
            opcoesCacambas.add(
                    String.format(
                            Locale.getDefault(),
                            "Caçamba #%03d",
                            cacamba.getCodigo()
                    )
            );
        }

        ArrayAdapter<String> adapterCacambas =
                new ArrayAdapter<>(
                        this,
                        R.layout.item_spinner,
                        opcoesCacambas
                );

        adapterCacambas.setDropDownViewResource(
                R.layout.item_spinner_dropdown
        );

        spinnerNovaCacamba.setAdapter(adapterCacambas);

        // Preenche os motivos da troca
        String[] motivos = {
                "Caçamba cheia",
                "Problema operacional"
        };

        ArrayAdapter<String> adapterMotivos =
                new ArrayAdapter<>(
                        this,
                        R.layout.item_spinner,
                        motivos
                );

        adapterMotivos.setDropDownViewResource(
                R.layout.item_spinner_dropdown
        );

        spinnerMotivoTroca.setAdapter(adapterMotivos);

        // Sugere o valor conforme o motivo selecionado
        spinnerMotivoTroca.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            View view,
                            int position,
                            long id
                    ) {
                        if (position == 0) {
                            edtValorAdicionalTroca.setText(
                                    formatarValorParaEdicao(
                                            locacao.getValorInicial()
                                    )
                            );
                        } else {
                            edtValorAdicionalTroca.setText("0,00");
                        }
                    }

                    @Override
                    public void onNothingSelected(
                            android.widget.AdapterView<?> parent
                    ) {
                    }
                }
        );

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(
                    android.R.color.transparent
            );
        }

        btnCancelarTroca.setOnClickListener(v -> dialog.dismiss());

        btnConfirmarTroca.setOnClickListener(v -> {
            int posicaoCacamba = spinnerNovaCacamba.getSelectedItemPosition();
            int posicaoMotivo = spinnerMotivoTroca.getSelectedItemPosition();

            if (posicaoCacamba < 0 ||
                    posicaoCacamba >= cacambasDisponiveis.size()) {

                Toast.makeText(
                        this,
                        "Selecione uma caçamba.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            if (posicaoMotivo < 0) {
                Toast.makeText(
                        this,
                        "Selecione o motivo da troca.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            Cacamba novaCacamba =
                    cacambasDisponiveis.get(posicaoCacamba);

            MotivoTroca motivo = posicaoMotivo == 0
                    ? MotivoTroca.CHEIA
                    : MotivoTroca.PROBLEMA_OPERACIONAL;

            Long valorAdicional;

            try {
                valorAdicional = converterMoedaParaCentavos(
                        edtValorAdicionalTroca.getText().toString()
                );
            } catch (Exception e) {
                Toast.makeText(
                        this,
                        e.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            String observacao = edtObservacaoTroca
                    .getText()
                    .toString()
                    .trim();

            btnConfirmarTroca.setEnabled(false);

            AppExecutors.getDatabaseExecutor().execute(() -> {
                try {
                    locacaoService.trocarCacamba(
                            idLocacao,
                            novaCacamba.getCodigo(),
                            motivo,
                            valorAdicional,
                            observacao
                    );

                    runOnUiThread(() -> {
                        dialog.dismiss();

                        Toast.makeText(
                                this,
                                "Caçamba trocada com sucesso!",
                                Toast.LENGTH_SHORT
                        ).show();

                        carregarDadosLocacao();
                        carregarHistoricoCacambas();
                    });

                } catch (Exception e) {
                    runOnUiThread(() -> {
                        btnConfirmarTroca.setEnabled(true);

                        Toast.makeText(
                                this,
                                e.getMessage() != null
                                        ? e.getMessage()
                                        : "Não foi possível realizar a troca.",
                                Toast.LENGTH_LONG
                        ).show();
                    });
                }
            });
        });
    }

    private void mostrarDialogoTrocaCacamba() {

        AppExecutors.getDatabaseExecutor().execute(() -> {
            try {
                Locacao locacao = locacaoService.buscarPorId(idLocacao);

                if (locacao == null) {
                    throw new IllegalArgumentException("Locação não encontrada.");
                }

                List<MovimentacaoCacamba> movimentacoes =
                        locacaoService.listarMovimentacoesPorLocacao(idLocacao);

                MovimentacaoCacamba movimentacaoAtual = null;

                for (MovimentacaoCacamba movimentacao : movimentacoes) {
                    if (movimentacao.getDataRetirada() == null) {
                        movimentacaoAtual = movimentacao;
                        break;
                    }
                }

                if (movimentacaoAtual == null) {
                    throw new IllegalArgumentException(
                            "Não foi encontrada uma caçamba em uso."
                    );
                }

                Cacamba cacambaAtual = cacambaService.buscarPorId(
                        movimentacaoAtual.getIdCacamba()
                );

                if (cacambaAtual == null) {
                    throw new IllegalArgumentException(
                            "Não foi possível identificar a caçamba atual."
                    );
                }

                List<Cacamba> cacambasDisponiveis =
                        cacambaService.listarPorStatus(StatusCacamba.DISPONIVEL);

                if (cacambasDisponiveis == null || cacambasDisponiveis.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Não existem caçambas disponíveis para troca."
                    );
                }

                runOnUiThread(() ->
                        exibirDialogoTroca(
                                locacao,
                                cacambaAtual,
                                cacambasDisponiveis
                        )
                );

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(
                                this,
                                e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
            }
        });
    }

    private String formatarValorParaEdicao(Long valorCentavos) {
        if (valorCentavos == null) {
            valorCentavos = 0L;
        }

        return String.format(
                new Locale("pt", "BR"),
                "R$ %.2f",
                valorCentavos / 100.0
        ).replace(".", ",");
    }

    private void configurarMascaraMoeda(EditText campo) {

        campo.addTextChangedListener(new TextWatcher() {

            private boolean alterando = false;

            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after
            ) {}

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count
            ) {}

            @Override
            public void afterTextChanged(Editable s) {

                if (alterando) {
                    return;
                }

                alterando = true;

                String numeros = s.toString().replaceAll("[^0-9]", "");

                if (numeros.isEmpty()) {
                    numeros = "0";
                }

                BigDecimal valor = new BigDecimal(numeros)
                        .movePointLeft(2);

                NumberFormat formato =
                        NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

                String valorFormatado = formato.format(valor);

                campo.setText(valorFormatado);
                campo.setSelection(valorFormatado.length());

                alterando = false;
            }

        });
    }

    private Long converterMoedaParaCentavos(String valor) {

        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("Valor não informado.");
        }

        String valorLimpo = valor
                .replace("R$", "")
                .replace("\u00A0", "")
                .replace("\u202F", "")
                .replace(" ", "")
                .trim();

        if (valorLimpo.contains(",")) {
            valorLimpo = valorLimpo
                    .replace(".", "")
                    .replace(",", ".");
        }

        BigDecimal decimal = new BigDecimal(valorLimpo);

        if (decimal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "O valor não pode ser negativo."
            );
        }

        return decimal
                .multiply(new BigDecimal("100"))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
    }

    private void carregarHistoricoRecebimentos() {
        AppExecutors.getDatabaseExecutor().execute(() -> {
            try {
                List<Recebimento> recebimentos =
                        recebimentoService.listarPorLocacao(idLocacao);

                runOnUiThread(() -> {
                    containerHistoricoRecebimentos.removeAllViews();

                    if (recebimentos == null || recebimentos.isEmpty()) {
                        TextView texto = criarTexto(
                                "Nenhum recebimento registrado.",
                                15,
                                Color.DKGRAY,
                                false
                        );

                        containerHistoricoRecebimentos.addView(texto);
                        return;
                    }

                    for (Recebimento recebimento : recebimentos) {
                        containerHistoricoRecebimentos.addView(
                                criarCardHistoricoRecebimento(recebimento)
                        );
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    containerHistoricoRecebimentos.removeAllViews();

                    TextView texto = criarTexto(
                            "Não foi possível carregar os recebimentos.",
                            15,
                            Color.DKGRAY,
                            false
                    );

                    containerHistoricoRecebimentos.addView(texto);
                });
            }
        });
    }

    private View criarCardHistoricoRecebimento(Recebimento recebimento) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(16), dp(18), dp(12));

        GradientDrawable fundo = new GradientDrawable();
        fundo.setColor(Color.WHITE);
        fundo.setCornerRadius(dp(16));
        card.setBackground(fundo);

        LinearLayout.LayoutParams parametros =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        parametros.setMargins(0, 0, 0, dp(14));
        card.setLayoutParams(parametros);

        NumberFormat formato =
                NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        String formaPagamento;

        switch (recebimento.getFormaPagamento()) {
            case PIX:
                formaPagamento = "Pix";
                break;

            case DINHEIRO:
                formaPagamento = "Dinheiro";
                break;

            case CARTAO:
                formaPagamento = "Cartão";
                break;

            default:
                formaPagamento = "Não informado";
                break;
        }

        card.addView(criarTexto(
                "Recebimento",
                19,
                Color.rgb(8, 43, 80),
                true
        ));

        card.addView(criarTexto(
                "Forma de pagamento: " + formaPagamento,
                14,
                Color.DKGRAY,
                true
        ));

        if (recebimento.getDataRecebimento() != null) {
            DateTimeFormatter formatoData =
                    DateTimeFormatter.ofPattern("dd/MM/yyyy");

            card.addView(criarTexto(
                    "Data: " + recebimento.getDataRecebimento().format(formatoData),
                    14,
                    Color.DKGRAY,
                    false
            ));
        }

        card.addView(criarTexto(
                "Valor bruto: "
                        + formatarMoeda(recebimento.getValorBruto(), formato),
                14,
                Color.DKGRAY,
                false
        ));

        card.addView(criarTexto(
                "Desconto: "
                        + formatarMoeda(recebimento.getDescontoValor(), formato),
                14,
                Color.DKGRAY,
                false
        ));

        card.addView(criarTexto(
                "Valor recebido: "
                        + formatarMoeda(recebimento.getValorRecebido(), formato),
                17,
                Color.rgb(30, 120, 65),
                true
        ));

        return card;
    }

    private int dp(int valor) {
        return (int) (valor * getResources()
                .getDisplayMetrics().density);
    }

    private void mostrarDialogoFinalizarLocacao() {

        AppExecutors.getDatabaseExecutor().execute(() -> {
            try {
                Locacao locacao = locacaoService.buscarPorId(idLocacao);

                if (locacao == null ||
                        !locacao.getStatus().name().equals("ATIVA")) {
                    throw new IllegalArgumentException(
                            "Esta locação não está mais ativa."
                    );
                }

                Long totalCalculado =
                        locacaoService.calcularValorTotal(idLocacao);

                runOnUiThread(() ->
                        exibirDialogoFinalizacao(totalCalculado)
                );

            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(
                        this,
                        e.getMessage() != null
                                ? e.getMessage()
                                : "Não foi possível preparar a finalização.",
                        Toast.LENGTH_LONG
                ).show());
            }
        });
    }

    private void exibirDialogoFinalizacao(Long totalCalculado) {

        View dialogView = getLayoutInflater().inflate(
                R.layout.dialog_finalizar_locacao,
                null
        );

        TextView txtTotalCalculado =
                dialogView.findViewById(R.id.txtTotalCalculado);

        EditText edtValorFinal =
                dialogView.findViewById(R.id.edtValorFinal);

        EditText edtJustificativa =
                dialogView.findViewById(R.id.edtJustificativa);

        TextView btnCancelar =
                dialogView.findViewById(R.id.btnCancelarFinalizacao);

        TextView btnConfirmar =
                dialogView.findViewById(R.id.btnConfirmarFinalizacao);

        txtTotalCalculado.setText(
                formatarMoeda(
                        totalCalculado,
                        NumberFormat.getCurrencyInstance(new Locale("pt", "BR"))
                )
        );

        edtValorFinal.setText(
                formatarValorParaEdicao(totalCalculado)
        );

        configurarMascaraMoeda(edtValorFinal);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(
                    android.R.color.transparent
            );
        }

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnConfirmar.setOnClickListener(v -> {

            Long valorFinal;

            try {
                valorFinal = converterMoedaParaCentavos(
                        edtValorFinal.getText().toString()
                );
            } catch (Exception e) {
                Toast.makeText(
                        this,
                        e.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            String justificativa = edtJustificativa
                    .getText()
                    .toString()
                    .trim();

            boolean houveAjuste = !valorFinal.equals(totalCalculado);

            if (houveAjuste && justificativa.isEmpty()) {
                edtJustificativa.setError(
                        "Informe a justificativa do ajuste."
                );
                edtJustificativa.requestFocus();
                return;
            }

            if (!houveAjuste) {
                justificativa = null;
            }

            btnConfirmar.setEnabled(false);

            String justificativaFinal = justificativa;

            AppExecutors.getDatabaseExecutor().execute(() -> {
                try {
                    locacaoService.finalizarLocacao(
                            idLocacao,
                            valorFinal,
                            justificativaFinal
                    );

                    runOnUiThread(() -> {
                        dialog.dismiss();

                        Toast.makeText(
                                this,
                                "Locação finalizada com sucesso!",
                                Toast.LENGTH_SHORT
                        ).show();

                        carregarDadosLocacao();
                        carregarHistoricoCacambas();
                        carregarHistoricoRecebimentos();
                    });

                } catch (Exception e) {
                    runOnUiThread(() -> {
                        btnConfirmar.setEnabled(true);

                        Toast.makeText(
                                this,
                                e.getMessage() != null
                                        ? e.getMessage()
                                        : "Não foi possível finalizar a locação.",
                                Toast.LENGTH_LONG
                        ).show();
                    });
                }
            });
        });
    }
}