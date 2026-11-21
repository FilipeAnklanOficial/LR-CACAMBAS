package com.lrcacambas;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
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
import com.lrcacambas.data.local.entity.Locacao;
import com.lrcacambas.data.local.entity.StatusLocacao;
import com.lrcacambas.data.repository.CacambaRepository;
import com.lrcacambas.data.repository.ClienteRepository;
import com.lrcacambas.data.repository.EnderecoClienteRepository;
import com.lrcacambas.data.repository.LocacaoRepository;
import com.lrcacambas.data.repository.MovimentacaoCacambaRepository;
import com.lrcacambas.data.repository.RecebimentoRepository;
import com.lrcacambas.domain.service.LocacaoService;
import com.lrcacambas.domain.service.RecebimentoService;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegistrarRecebimentoActivity extends AppCompatActivity {

    private LinearLayout containerLocacoes;

    private RecebimentoService recebimentoService;
    private LocacaoRepository locacaoRepository;
    private LocacaoService locacaoService;
    private ClienteRepository clienteRepository;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.enableEdgeToEdge(getWindow());

        setContentView(R.layout.activity_registrar_recebimento);

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

        containerLocacoes = findViewById(R.id.containerLocacoes);

        // Banco de dados
        AppDatabase database = DatabaseProvider.getDatabase(this);

        // Repositories
        clienteRepository =
                new ClienteRepository(database.clienteDao());

        EnderecoClienteRepository enderecoRepository =
                new EnderecoClienteRepository(database.enderecoClienteDao());

        CacambaRepository cacambaRepository =
                new CacambaRepository(database.cacambaDao());

        locacaoRepository =
                new LocacaoRepository(database.locacaoDao());

        MovimentacaoCacambaRepository movimentacaoRepository =
                new MovimentacaoCacambaRepository(database.movimentacaoCacambaDao());

        RecebimentoRepository recebimentoRepository =
                new RecebimentoRepository(database.recebimentoDao());

        // LocacaoService
        locacaoService = new LocacaoService(
                database,
                locacaoRepository,
                clienteRepository,
                enderecoRepository,
                cacambaRepository,
                movimentacaoRepository
        );

        // RecebimentoService
        recebimentoService = new RecebimentoService(
                database,
                recebimentoRepository,
                locacaoRepository,
                locacaoService
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarLocacoes();
    }

    private void carregarLocacoes() {

        executor.execute(() -> {

            List<Locacao> locacoes = new ArrayList<>();
            List<Cliente> clientes = new ArrayList<>();
            List<Long> saldos = new ArrayList<>();

            // Busca locações ativas e finalizadas
            List<Locacao> ativas =
                    locacaoRepository.listarPorStatus(StatusLocacao.ATIVA);

            List<Locacao> finalizadas =
                    locacaoRepository.listarPorStatus(StatusLocacao.FINALIZADA);

            List<Locacao> todas = new ArrayList<>();
            todas.addAll(ativas);
            todas.addAll(finalizadas);

            // Verifica o saldo de cada locação
            for (Locacao locacao : todas) {

                long saldo = recebimentoService.calcularSaldoPendente(
                        locacao.getIdLocacao()
                );

                if (saldo > 0) {

                    Cliente cliente = clienteRepository.buscarPorId(
                            locacao.getIdCliente()
                    );

                    locacoes.add(locacao);
                    clientes.add(cliente);
                    saldos.add(saldo);
                }
            }

            runOnUiThread(() -> exibirLocacoes(locacoes, clientes, saldos));
        });
    }

    private void exibirLocacoes(
            List<Locacao> locacoes,
            List<Cliente> clientes,
            List<Long> saldos
    ) {

        containerLocacoes.removeAllViews();

        if (locacoes.isEmpty()) {

            TextView mensagem = new TextView(this);
            mensagem.setText("Nenhuma locação possui saldo pendente.");
            mensagem.setTextColor(Color.BLACK);
            mensagem.setTextSize(16);
            mensagem.setPadding(8, 16, 8, 16);

            containerLocacoes.addView(mensagem);

            return;
        }

        for (int i = 0; i < locacoes.size(); i++) {

            Locacao locacao = locacoes.get(i);
            Cliente cliente = clientes.get(i);
            long saldo = saldos.get(i);

            criarCartaoLocacao(locacao, cliente, saldo);
        }
    }

    private void criarCartaoLocacao(
            Locacao locacao,
            Cliente cliente,
            long saldo
    ) {

        LinearLayout cartao = new LinearLayout(this);
        cartao.setOrientation(LinearLayout.VERTICAL);
        cartao.setPadding(dp(16), dp(16), dp(16), dp(16));
        GradientDrawable fundo = new GradientDrawable();
        fundo.setColor(Color.WHITE);
        fundo.setCornerRadius(dp(16));

        cartao.setBackground(fundo);
        cartao.setElevation(dp(3));

        LinearLayout.LayoutParams parametros =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        parametros.bottomMargin = dp(16);

        TextView nome = new TextView(this);
        nome.setText(cliente != null
                ? cliente.getNome()
                : "Cliente não encontrado");
        nome.setTextColor(Color.BLACK);
        nome.setTextSize(18);
        nome.setTypeface(null, android.graphics.Typeface.BOLD);
        nome.setPadding(0, 0, 0, dp(6));

        TextView endereco = new TextView(this);
        endereco.setText(formatarEndereco(locacao));
        endereco.setTextColor(Color.DKGRAY);
        endereco.setTextSize(14);
        endereco.setPadding(0, 0, 0, dp(6));

        TextView status = new TextView(this);
        status.setText(locacao.getStatus() == StatusLocacao.ATIVA
                ? "Em andamento"
                : "Finalizada");
        status.setTextColor(Color.DKGRAY);
        status.setTextSize(14);
        status.setPadding(0, 0, 0, dp(6));

        TextView valor = new TextView(this);
        valor.setText("Saldo pendente: " + formatarMoeda(saldo));
        valor.setTextColor(Color.rgb(20, 70, 120));
        valor.setTextSize(17);
        valor.setTypeface(null, android.graphics.Typeface.BOLD);
        valor.setPadding(0, 0, 0, dp(6));

        cartao.addView(nome);
        cartao.addView(endereco);
        cartao.addView(status);
        cartao.addView(valor);

        if (locacao.getStatus() == StatusLocacao.FINALIZADA) {
            Button btnInadimplente = new Button(this);
            btnInadimplente.setText("Tornar inadimplente");
            btnInadimplente.setAllCaps(false);

            GradientDrawable fundoBotao = new GradientDrawable();
            fundoBotao.setColor(Color.rgb(8, 43, 80));
            fundoBotao.setCornerRadius(dp(12));

            btnInadimplente.setBackground(fundoBotao);
            btnInadimplente.setTextColor(Color.WHITE);

            btnInadimplente.setOnClickListener(v ->
                    confirmarInadimplencia(locacao, cliente, saldo)
            );

            LinearLayout.LayoutParams parametrosBotao =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            parametrosBotao.topMargin = dp(10);

            cartao.addView(btnInadimplente, parametrosBotao);
        }

        cartao.setOnClickListener(v -> {
            Intent intent = new Intent(
                    RegistrarRecebimentoActivity.this,
                    NovoRecebimentoActivity.class
            );

            intent.putExtra(
                    NovoRecebimentoActivity.EXTRA_ID_LOCACAO,
                    locacao.getIdLocacao()
            );

            startActivity(intent);
        });

        containerLocacoes.addView(cartao, parametros);
    }

    private void confirmarInadimplencia(Locacao locacao, Cliente cliente, long saldo) {

        String nome = cliente != null ? cliente.getNome() : "este cliente";

        View view = getLayoutInflater().inflate(
                R.layout.dialog_confirmar_inadimplencia,
                null
        );

        TextView txtMensagem = view.findViewById(R.id.txtMensagemDialogo);
        TextView btnCancelar = view.findViewById(R.id.btnCancelarDialogo);
        TextView btnConfirmar = view.findViewById(R.id.btnConfirmarDialogo);

        txtMensagem.setText(
                "Deseja encaminhar a dívida de " + nome +
                        " para a lista de inadimplentes?\n\n" +
                        "Saldo pendente: " + formatarMoeda(saldo)
        );

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .create();

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnConfirmar.setOnClickListener(v -> {

            btnConfirmar.setEnabled(false);

            executor.execute(() -> {
                try {
                    locacaoService.marcarComoInadimplente(
                            locacao.getIdLocacao()
                    );

                    runOnUiThread(() -> {
                        dialog.dismiss();

                        Toast.makeText(
                                this,
                                "Locação marcada como inadimplente.",
                                Toast.LENGTH_SHORT
                        ).show();

                        carregarLocacoes();
                    });

                } catch (Exception e) {
                    runOnUiThread(() -> {
                        btnConfirmar.setEnabled(true);

                        Toast.makeText(
                                this,
                                e.getMessage() != null
                                        ? e.getMessage()
                                        : "Não foi possível atualizar a locação.",
                                Toast.LENGTH_LONG
                        ).show();
                    });
                }
            });
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(
                    android.R.color.transparent
            );
        }

        dialog.show();
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

    private void adicionarEndereco(StringBuilder endereco, String parte) {

        if (parte != null && !parte.trim().isEmpty()) {

            if (endereco.length() > 0) {
                endereco.append(", ");
            }

            endereco.append(parte);
        }
    }

    private String formatarMoeda(long valorEmCentavos) {

        NumberFormat formato =
                NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        return formato.format(BigDecimal.valueOf(valorEmCentavos, 2));
    }

    private int dp(int valor) {
        return (int) (valor * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        executor.shutdown();
        super.onDestroy();
    }
}