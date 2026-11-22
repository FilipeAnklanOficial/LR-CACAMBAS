package com.lrcacambas;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.lrcacambas.data.local.database.AppDatabase;
import com.lrcacambas.data.local.database.DatabaseProvider;
import com.lrcacambas.data.local.entity.Cliente;
import com.lrcacambas.data.local.entity.Locacao;
import com.lrcacambas.data.local.entity.Recebimento;
import com.lrcacambas.data.local.entity.StatusLocacao;
import com.lrcacambas.data.repository.LocacaoRepository;
import com.lrcacambas.domain.service.ClienteService;
import com.lrcacambas.domain.service.LocacaoService;
import com.lrcacambas.domain.service.RecebimentoService;
import com.lrcacambas.util.AppContainer;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocacoesInadimplentesActivity extends AppCompatActivity {

    private LinearLayout containerLocacoesInadimplentes;

    private LocacaoRepository locacaoRepository;
    private LocacaoService locacaoService;
    private RecebimentoService recebimentoService;
    private ClienteService clienteService;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.enableEdgeToEdge(getWindow());

        setContentView(R.layout.activity_locacoes_inadimplentes);

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

        containerLocacoesInadimplentes =
                findViewById(R.id.containerLocacoesInadimplentes);

        inicializarServicos();
    }

    private void inicializarServicos() {

        AppContainer appContainer = new AppContainer(this);

        locacaoService = appContainer.locacaoService;
        recebimentoService = appContainer.recebimentoService;
        clienteService = appContainer.clienteService;

        AppDatabase database = DatabaseProvider.getDatabase(this);

        locacaoRepository = new LocacaoRepository(
                database.locacaoDao()
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarLocacoesInadimplentes();
    }

    private void carregarLocacoesInadimplentes() {

        executor.execute(() -> {

            try {
                List<Locacao> locacoes =
                        locacaoRepository.listarPorStatus(
                                StatusLocacao.INADIMPLENTE
                        );

                List<DadosLocacaoInadimplente> dados = new ArrayList<>();

                for (Locacao locacao : locacoes) {

                    Cliente cliente = clienteService.buscarPorId(
                            locacao.getIdCliente()
                    );

                    long totalRecebido =
                            recebimentoService.calcularTotalRecebido(
                                    locacao.getIdLocacao()
                            );

                    long saldoPendente =
                            recebimentoService.calcularSaldoPendente(
                                    locacao.getIdLocacao()
                            );

                    List<Recebimento> recebimentos =
                            recebimentoService.listarPorLocacao(
                                    locacao.getIdLocacao()
                            );

                    long totalDescontos = 0;

                    for (Recebimento recebimento : recebimentos) {
                        if (recebimento.getDescontoValor() != null) {
                            totalDescontos += recebimento.getDescontoValor();
                        }
                    }

                    dados.add(new DadosLocacaoInadimplente(
                            locacao,
                            cliente,
                            totalRecebido,
                            totalDescontos,
                            saldoPendente
                    ));
                }

                runOnUiThread(() -> exibirLocacoes(dados));

            } catch (Exception e) {
                runOnUiThread(() -> exibirMensagem(
                        "Não foi possível carregar as locações inadimplentes."
                ));
            }
        });
    }

    private void exibirLocacoes(
            List<DadosLocacaoInadimplente> dados
    ) {

        containerLocacoesInadimplentes.removeAllViews();

        if (dados.isEmpty()) {
            exibirMensagem("Nenhuma locação inadimplente encontrada.");
            return;
        }

        for (DadosLocacaoInadimplente item : dados) {
            criarCartaoLocacao(item);
        }
    }

    private void criarCartaoLocacao(
            DadosLocacaoInadimplente item
    ) {

        Locacao locacao = item.locacao;
        Cliente cliente = item.cliente;

        LinearLayout cartao = new LinearLayout(this);
        cartao.setOrientation(LinearLayout.VERTICAL);
        cartao.setPadding(dp(16), dp(16), dp(16), dp(16));

        GradientDrawable fundo = new GradientDrawable();
        fundo.setColor(Color.WHITE);
        fundo.setCornerRadius(dp(16));

        cartao.setBackground(fundo);
        cartao.setClipToPadding(false);
        cartao.setClipChildren(false);
        cartao.setElevation(dp(3));

        LinearLayout.LayoutParams parametros =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        parametros.bottomMargin = dp(16);

        TextView nome = criarTexto(
                cliente != null
                        ? cliente.getNome()
                        : "Cliente não encontrado",
                18,
                Color.BLACK,
                true
        );

        TextView endereco = criarTexto(
                formatarEndereco(locacao),
                14,
                Color.DKGRAY,
                false
        );

        TextView status = criarTexto(
                "Status: Inadimplente",
                14,
                Color.rgb(190, 35, 35),
                true
        );

        cartao.addView(nome);
        cartao.addView(endereco);
        cartao.addView(status);

        if (locacao.getDataFinalizacao() != null) {

            DateTimeFormatter formato =
                    DateTimeFormatter.ofPattern("dd/MM/yyyy");

            cartao.addView(criarTexto(
                    "Finalizada em: "
                            + locacao.getDataFinalizacao().format(formato),
                    14,
                    Color.DKGRAY,
                    false
            ));
        }

        NumberFormat formatoMoeda =
                NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        cartao.addView(criarTexto(
                "Valor total: "
                        + formatarMoeda(
                        locacao.getValorTotalFinal(),
                        formatoMoeda
                ),
                15,
                Color.DKGRAY,
                false
        ));

        cartao.addView(criarTexto(
                "Total recebido: "
                        + formatarMoeda(item.totalRecebido, formatoMoeda),
                15,
                Color.rgb(30, 120, 65),
                false
        ));

        cartao.addView(criarTexto(
                "Descontos concedidos: "
                        + formatarMoeda(item.totalDescontos, formatoMoeda),
                15,
                Color.DKGRAY,
                false
        ));

        cartao.addView(criarTexto(
                "Saldo devedor: "
                        + formatarMoeda(item.saldoPendente, formatoMoeda),
                18,
                Color.rgb(190, 35, 35),
                true
        ));

        LinearLayout linhaBotoes = new LinearLayout(this);
        linhaBotoes.setOrientation(LinearLayout.HORIZONTAL);
        linhaBotoes.setPadding(0, dp(12), 0, 0);

        Button btnRecebimento = criarBotao(
                "Registrar recebimento",
                Color.rgb(35, 65, 110)
        );

        Button btnFinalizar = criarBotao(
                "Finalizar mesmo devendo",
                Color.rgb(160, 45, 45)
        );

        LinearLayout.LayoutParams parametrosBotao =
                new LinearLayout.LayoutParams(
                        0,
                        dp(48),
                        1
                );

        parametrosBotao.setMargins(0, 0, dp(6), 0);
        linhaBotoes.addView(btnRecebimento, parametrosBotao);

        LinearLayout.LayoutParams parametrosBotaoFinalizar =
                new LinearLayout.LayoutParams(
                        0,
                        dp(48),
                        1
                );

        parametrosBotaoFinalizar.setMargins(dp(6), 0, 0, 0);
        linhaBotoes.addView(btnFinalizar, parametrosBotaoFinalizar);

        cartao.addView(linhaBotoes);

        btnRecebimento.setOnClickListener(v -> registrarRecebimento(locacao));
        btnFinalizar.setOnClickListener(v -> confirmarFinalizacaoComDivida(locacao));

        cartao.setOnClickListener(v -> {
            Intent intent = new Intent(
                    LocacoesInadimplentesActivity.this,
                    DetalhesLocacaoActivity.class
            );

            intent.putExtra("ID_LOCACAO", locacao.getIdLocacao());
            startActivity(intent);
        });

        containerLocacoesInadimplentes.addView(cartao, parametros);
    }

    private Button criarBotao(String texto, int cor) {

        Button botao = new Button(this);
        botao.setText(texto);
        botao.setTextColor(Color.WHITE);
        botao.setTextSize(12);
        botao.setAllCaps(false);
        botao.setPadding(dp(4), 0, dp(4), 0);

        GradientDrawable fundo = new GradientDrawable();
        fundo.setColor(cor);
        fundo.setCornerRadius(dp(10));

        botao.setBackground(fundo);

        return botao;
    }

    private void registrarRecebimento(Locacao locacao) {

        Intent intent = new Intent(
                LocacoesInadimplentesActivity.this,
                NovoRecebimentoActivity.class
        );

        intent.putExtra(
                NovoRecebimentoActivity.EXTRA_ID_LOCACAO,
                locacao.getIdLocacao()
        );

        startActivity(intent);
    }

    private void confirmarFinalizacaoComDivida(Locacao locacao) {

        View view = getLayoutInflater().inflate(
                R.layout.dialog_confirmar_inadimplencia,
                null
        );

        TextView titulo = view.findViewById(R.id.txtTituloDialogo);
        TextView mensagem = view.findViewById(R.id.txtMensagemDialogo);
        TextView btnCancelar = view.findViewById(R.id.btnCancelarDialogo);
        TextView btnConfirmar = view.findViewById(R.id.btnConfirmarDialogo);

        titulo.setText("Finalizar locação?");
        mensagem.setText(
                "Essa locação será encerrada e deixará de aparecer " +
                        "nas listas de pendentes e inadimplentes.\n\n" +
                        "O saldo devedor e todo o histórico financeiro " +
                        "serão preservados.\n\n" +
                        "Deseja continuar?"
        );

        btnCancelar.setText("Cancelar");
        btnConfirmar.setText("Finalizar");

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .create();

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(
                    android.R.color.transparent
            );
        }

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnConfirmar.setOnClickListener(v -> {

            dialog.dismiss();

            executor.execute(() -> {

                try {
                    locacaoService.finalizarComDivida(
                            locacao.getIdLocacao()
                    );

                    runOnUiThread(this::carregarLocacoesInadimplentes);

                } catch (Exception e) {

                    runOnUiThread(() -> new AlertDialog.Builder(this)
                            .setTitle("Erro")
                            .setMessage(
                                    "Não foi possível finalizar a locação."
                            )
                            .setPositiveButton("OK", null)
                            .show());
                }
            });
        });
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
        view.setPadding(0, 0, 0, dp(6));

        if (negrito) {
            view.setTypeface(null, Typeface.BOLD);
        }

        return view;
    }

    private void exibirMensagem(String mensagem) {

        containerLocacoesInadimplentes.removeAllViews();

        TextView texto = criarTexto(
                mensagem,
                16,
                Color.BLACK,
                false
        );

        texto.setPadding(dp(8), dp(16), dp(8), dp(16));

        containerLocacoesInadimplentes.addView(texto);
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

    private void adicionarEndereco(
            StringBuilder endereco,
            String parte
    ) {

        if (parte != null && !parte.trim().isEmpty()) {

            if (endereco.length() > 0) {
                endereco.append(", ");
            }

            endereco.append(parte);
        }
    }

    private String formatarMoeda(
            Long valor,
            NumberFormat formato
    ) {

        return formato.format(
                (valor == null ? 0L : valor) / 100.0
        );
    }

    private int dp(int valor) {
        return (int) (valor * getResources()
                .getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        executor.shutdown();
        super.onDestroy();
    }

    private static class DadosLocacaoInadimplente {

        final Locacao locacao;
        final Cliente cliente;
        final long totalRecebido;
        final long totalDescontos;
        final long saldoPendente;

        DadosLocacaoInadimplente(
                Locacao locacao,
                Cliente cliente,
                long totalRecebido,
                long totalDescontos,
                long saldoPendente
        ) {
            this.locacao = locacao;
            this.cliente = cliente;
            this.totalRecebido = totalRecebido;
            this.totalDescontos = totalDescontos;
            this.saldoPendente = saldoPendente;
        }
    }
}