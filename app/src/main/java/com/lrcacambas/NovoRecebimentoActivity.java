package com.lrcacambas;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.lrcacambas.data.local.database.AppDatabase;
import com.lrcacambas.data.local.database.DatabaseProvider;
import com.lrcacambas.data.local.entity.Cliente;
import com.lrcacambas.data.local.entity.FormaPagamento;
import com.lrcacambas.data.local.entity.Locacao;
import com.lrcacambas.data.local.entity.Recebimento;
import com.lrcacambas.data.repository.ClienteRepository;
import com.lrcacambas.data.repository.CacambaRepository;
import com.lrcacambas.data.repository.EnderecoClienteRepository;
import com.lrcacambas.data.repository.LocacaoRepository;
import com.lrcacambas.data.repository.MovimentacaoCacambaRepository;
import com.lrcacambas.data.repository.RecebimentoRepository;
import com.lrcacambas.domain.service.LocacaoService;
import com.lrcacambas.domain.service.RecebimentoService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NovoRecebimentoActivity extends AppCompatActivity {

    public static final String EXTRA_ID_LOCACAO = "idLocacao";

    private TextView tvNomeCliente;
    private TextView tvEndereco;
    private TextView tvSaldoPendente;
    private TextView tvPercentualDesconto;
    private TextView tvValorLiquido;

    private EditText etValorBruto;
    private EditText etDesconto;

    private RadioGroup rgFormaPagamento;
    private Button btnConfirmarRecebimento;

    private LocacaoRepository locacaoRepository;
    private ClienteRepository clienteRepository;
    private RecebimentoService recebimentoService;

    private boolean atualizandoValor = false;

    private Locacao locacao;
    private long saldoPendente;
    private Long idLocacao;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_novo_recebimento);

        View root = findViewById(R.id.main);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
            );

            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
            );

            return insets;
        });

        idLocacao = getIntent().getLongExtra(EXTRA_ID_LOCACAO, -1L);

        if (idLocacao == -1L) {
            finish();
            return;
        }

        inicializarComponentes();
        inicializarServicos();
        configurarCalculos();

        btnConfirmarRecebimento.setOnClickListener(v ->
                confirmarRecebimento()
        );

        findViewById(R.id.btnCancelar).setOnClickListener(v -> finish());

        carregarDadosLocacao();
    }

    private void inicializarComponentes() {
        tvNomeCliente = findViewById(R.id.tvNomeCliente);
        tvEndereco = findViewById(R.id.tvEndereco);
        tvSaldoPendente = findViewById(R.id.tvSaldoPendente);
        tvPercentualDesconto = findViewById(R.id.tvPercentualDesconto);
        tvValorLiquido = findViewById(R.id.tvValorLiquido);

        etValorBruto = findViewById(R.id.etValorBruto);
        etDesconto = findViewById(R.id.etDesconto);

        rgFormaPagamento = findViewById(R.id.rgFormaPagamento);
        btnConfirmarRecebimento =
                findViewById(R.id.btnConfirmarRecebimento);
    }

    private void inicializarServicos() {
        AppDatabase database = DatabaseProvider.getDatabase(this);

        locacaoRepository = new LocacaoRepository(
                database.locacaoDao()
        );

        clienteRepository = new ClienteRepository(
                database.clienteDao()
        );

        EnderecoClienteRepository enderecoRepository =
                new EnderecoClienteRepository(
                        database.enderecoClienteDao()
                );

        CacambaRepository cacambaRepository =
                new CacambaRepository(
                        database.cacambaDao()
                );

        MovimentacaoCacambaRepository movimentacaoRepository =
                new MovimentacaoCacambaRepository(
                        database.movimentacaoCacambaDao()
                );

        RecebimentoRepository recebimentoRepository =
                new RecebimentoRepository(
                        database.recebimentoDao()
                );

        LocacaoService locacaoService = new LocacaoService(
                database,
                locacaoRepository,
                clienteRepository,
                enderecoRepository,
                cacambaRepository,
                movimentacaoRepository
        );

        recebimentoService = new RecebimentoService(
                database,
                recebimentoRepository,
                locacaoRepository,
                locacaoService
        );
    }

    private void carregarDadosLocacao() {
        executor.execute(() -> {
            Locacao locacaoEncontrada =
                    locacaoRepository.buscarPorId(idLocacao);

            if (locacaoEncontrada == null) {
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

            Cliente cliente = clienteRepository.buscarPorId(
                    locacaoEncontrada.getIdCliente()
            );

            long saldo = recebimentoService.calcularSaldoPendente(
                    idLocacao
            );

            runOnUiThread(() -> {
                locacao = locacaoEncontrada;
                saldoPendente = saldo;

                tvNomeCliente.setText(
                        cliente != null
                                ? cliente.getNome()
                                : "Cliente não encontrado"
                );

                tvEndereco.setText(formatarEndereco(locacao));

                tvSaldoPendente.setText(
                        "Saldo pendente: " + formatarMoeda(saldoPendente)
                );

                if (saldoPendente <= 0) {
                    btnConfirmarRecebimento.setEnabled(false);
                    Toast.makeText(
                            this,
                            "Esta locação não possui saldo pendente.",
                            Toast.LENGTH_LONG
                    ).show();
                }
            });
        });
    }

    private void configurarCalculos() {
        configurarMascaraValor(etValorBruto);
        configurarMascaraValor(etDesconto);
    }

    private void configurarMascaraValor(EditText campo) {

        campo.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after
            ) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count
            ) {
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (atualizandoValor) {
                    return;
                }

                atualizandoValor = true;

                try {
                    String numeros = s.toString().replaceAll("\\D", "");

                    if (numeros.isEmpty()) {
                        campo.setText("");
                    } else {
                        long centavos = Long.parseLong(numeros);

                        NumberFormat formato = NumberFormat.getCurrencyInstance(
                                new Locale("pt", "BR")
                        );

                        String valorFormatado = formato.format(
                                BigDecimal.valueOf(centavos, 2)
                        );

                        campo.setText(valorFormatado);
                        campo.setSelection(campo.getText().length());
                    }

                } catch (NumberFormatException e) {
                    campo.setError("Valor muito alto.");
                } finally {
                    atualizandoValor = false;
                }

                atualizarCalculos();
            }
        });
    }

    private void atualizarCalculos() {
        try {
            long bruto = converterParaCentavos(
                    etValorBruto.getText().toString()
            );

            long desconto = converterParaCentavos(
                    etDesconto.getText().toString()
            );

            long totalOperacao = bruto + desconto;

            if (bruto > 0 && desconto >= 0) {
                BigDecimal percentual = BigDecimal.valueOf(desconto)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(
                                BigDecimal.valueOf(totalOperacao),
                                2,
                                RoundingMode.HALF_UP
                        );

                tvPercentualDesconto.setText(
                        "Desconto: " + percentual.toPlainString() + "%"
                );
            } else {
                tvPercentualDesconto.setText("Desconto: 0%");
            }

            tvValorLiquido.setText(formatarMoeda(bruto));

        } catch (NumberFormatException e) {
            tvPercentualDesconto.setText("Desconto: 0%");
            tvValorLiquido.setText(formatarMoeda(0));
        }
    }

    private void confirmarRecebimento() {
        String textoBruto = etValorBruto.getText().toString().trim();
        String textoDesconto = etDesconto.getText().toString().trim();

        if (textoBruto.isEmpty()) {
            etValorBruto.setError("Informe o valor bruto.");
            return;
        }

        if (rgFormaPagamento.getCheckedRadioButtonId() == -1) {
            Toast.makeText(
                    this,
                    "Selecione a forma de pagamento.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        long bruto;
        long desconto;

        try {
            bruto = converterParaCentavos(textoBruto);
            desconto = textoDesconto.isEmpty()
                    ? 0
                    : converterParaCentavos(textoDesconto);
        } catch (NumberFormatException e) {
            Toast.makeText(
                    this,
                    "Informe valores monetários válidos.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (bruto <= 0) {
            etValorBruto.setError("O valor deve ser maior que zero.");
            return;
        }

        if (bruto + desconto > saldoPendente) {
            Toast.makeText(
                    this,
                    "O recebimento somado ao desconto ultrapassa o saldo pendente.",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        FormaPagamento formaPagamento =
                obterFormaPagamentoSelecionada();

        Recebimento recebimento = new Recebimento();
        recebimento.setIdLocacao(idLocacao);
        recebimento.setValorBruto(bruto);
        recebimento.setDescontoValor(desconto);
        recebimento.setValorRecebido(bruto);
        recebimento.setDataRecebimento(LocalDate.now());
        recebimento.setFormaPagamento(formaPagamento);

        btnConfirmarRecebimento.setEnabled(false);

        executor.execute(() -> {
            try {
                recebimentoService.registrar(recebimento);

                runOnUiThread(() -> {
                    Toast.makeText(
                            this,
                            "Recebimento registrado com sucesso!",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnConfirmarRecebimento.setEnabled(true);

                    Toast.makeText(
                            this,
                            e.getMessage() != null
                                    ? e.getMessage()
                                    : "Não foi possível registrar o recebimento.",
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        });
    }

    private FormaPagamento obterFormaPagamentoSelecionada() {
        int selecionado = rgFormaPagamento.getCheckedRadioButtonId();

        if (selecionado == R.id.rbPix) {
            return FormaPagamento.PIX;
        }

        if (selecionado == R.id.rbDinheiro) {
            return FormaPagamento.DINHEIRO;
        }

        return FormaPagamento.CARTAO;
    }

    private long converterParaCentavos(String valor) {

        String numeros = valor.replaceAll("\\D", "");

        if (numeros.isEmpty()) {
            return 0;
        }

        return Long.parseLong(numeros);
    }

    private String formatarMoeda(long valorEmCentavos) {
        NumberFormat formato = NumberFormat.getCurrencyInstance(
                new Locale("pt", "BR")
        );

        return formato.format(
                BigDecimal.valueOf(valorEmCentavos, 2)
        );
    }

    private String formatarEndereco(Locacao locacao) {
        StringBuilder endereco = new StringBuilder();

        adicionarEndereco(endereco, locacao.getLogradouro());
        adicionarEndereco(endereco, locacao.getNumero());
        adicionarEndereco(endereco, locacao.getBairro());
        adicionarEndereco(endereco, locacao.getCidade());
        adicionarEndereco(endereco, locacao.getComplemento());

        return endereco.length() > 0
                ? endereco.toString()
                : "Endereço não informado";
    }

    private void adicionarEndereco(StringBuilder endereco, String valor) {
        if (valor != null && !valor.trim().isEmpty()) {
            if (endereco.length() > 0) {
                endereco.append(", ");
            }

            endereco.append(valor);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}