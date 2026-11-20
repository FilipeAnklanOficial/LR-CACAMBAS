package com.lrcacambas;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.lrcacambas.data.local.entity.Cliente;
import com.lrcacambas.data.local.entity.Locacao;
import com.lrcacambas.domain.service.ClienteService;
import com.lrcacambas.domain.service.LocacaoService;
import com.lrcacambas.domain.service.RecebimentoService;
import com.lrcacambas.util.AppContainer;
import com.lrcacambas.util.AppExecutors;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LocacoesAtivasActivity extends AppCompatActivity {

    private EditText edtBuscarLocacao;
    private LinearLayout containerLocacoes;
    private TextView txtResultadoBusca;
    private TextView txtNenhumaLocacao;

    private ClienteService clienteService;
    private LocacaoService locacaoService;
    private RecebimentoService recebimentoService;

    private final List<ItemLocacao> listaLocacoes = new ArrayList<>();

    private final NumberFormat formatoMoeda =
            NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locacoes_ativas);

        inicializarComponentes();
        inicializarServicos();
        configurarBusca();
        carregarLocacoes();
    }

    private void inicializarComponentes() {
        edtBuscarLocacao = findViewById(R.id.edtBuscarLocacao);
        containerLocacoes = findViewById(R.id.containerLocacoes);
        txtResultadoBusca = findViewById(R.id.txtResultadoBusca);
        txtNenhumaLocacao = findViewById(R.id.txtNenhumaLocacao);
    }

    private void inicializarServicos() {
        AppContainer appContainer = new AppContainer(this);

        clienteService = appContainer.clienteService;
        locacaoService = appContainer.locacaoService;
        recebimentoService = appContainer.recebimentoService;
    }

    private void configurarBusca() {
        edtBuscarLocacao.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                exibirLocacoes(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void carregarLocacoes() {
        AppExecutors.getDatabaseExecutor().execute(() -> {
            try {
                List<Locacao> locacoes = locacaoService.listarAtivas();
                List<ItemLocacao> itens = new ArrayList<>();

                for (Locacao locacao : locacoes) {
                    Cliente cliente = clienteService.buscarPorId(locacao.getIdCliente());

                    if (cliente == null) {
                        continue;
                    }

                    Long saldo = recebimentoService.calcularSaldoPendente(
                            locacao.getIdLocacao()
                    );

                    itens.add(new ItemLocacao(locacao, cliente, saldo));
                }

                runOnUiThread(() -> {
                    listaLocacoes.clear();
                    listaLocacoes.addAll(itens);
                    exibirLocacoes(edtBuscarLocacao.getText().toString());
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        txtNenhumaLocacao.setText(
                                "Não foi possível carregar as locações."
                        )
                );
            }
        });
    }

    private void exibirLocacoes(String busca) {
        containerLocacoes.removeAllViews();

        String termo = busca.trim().toLowerCase(Locale.ROOT);
        int quantidade = 0;

        for (ItemLocacao item : listaLocacoes) {
            String nome = item.cliente.getNome() == null
                    ? ""
                    : item.cliente.getNome().toLowerCase(Locale.ROOT);

            String apelido = item.cliente.getApelido() == null
                    ? ""
                    : item.cliente.getApelido().toLowerCase(Locale.ROOT);

            if (!nome.contains(termo) && !apelido.contains(termo)) {
                continue;
            }

            containerLocacoes.addView(criarCardLocacao(item));
            quantidade++;
        }

        txtResultadoBusca.setText(
                quantidade == 1
                        ? "1 locação encontrada"
                        : quantidade + " locações encontradas"
        );

        txtNenhumaLocacao.setVisibility(
                quantidade == 0 ? View.VISIBLE : View.GONE
        );
    }

    private View criarCardLocacao(ItemLocacao item) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(16), dp(18), dp(16));

        GradientDrawable fundo = new GradientDrawable();
        fundo.setColor(Color.WHITE);
        fundo.setCornerRadius(dp(16));
        card.setBackground(fundo);

        LinearLayout.LayoutParams parametros = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        parametros.setMargins(0, 0, 0, dp(14));
        card.setLayoutParams(parametros);

        TextView nome = criarTexto(
                item.cliente.getNome(),
                19,
                Color.rgb(8, 43, 80),
                true
        );
        card.addView(nome);

        if (item.cliente.getApelido() != null
                && !item.cliente.getApelido().trim().isEmpty()) {
            card.addView(criarTexto(
                    "Apelido: " + item.cliente.getApelido(),
                    14,
                    Color.DKGRAY,
                    false
            ));
        }

        card.addView(criarTexto(
                formatarEndereco(item.locacao),
                14,
                Color.DKGRAY,
                false
        ));

        card.addView(criarTexto(
                "Status: Ativa",
                14,
                Color.rgb(30, 120, 65),
                true
        ));

        card.addView(criarTexto(
                "Saldo pendente: " + formatoMoeda.format(
                        (item.saldo == null ? 0L : item.saldo) / 100.0
                ),
                16,
                Color.rgb(8, 43, 80),
                true
        ));

        card.setOnClickListener(v -> {
            Intent intent = new Intent(
                    LocacoesAtivasActivity.this,
                    DetalhesLocacaoActivity.class
            );

            intent.putExtra(
                    "ID_LOCACAO",
                    item.locacao.getIdLocacao()
            );

            startActivity(intent);
        });

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

        if (endereco.length() == 0) {
            return "Endereço não informado";
        }

        return endereco.toString();
    }

    private int dp(int valor) {
        return (int) (valor * getResources().getDisplayMetrics().density);
    }

    private static class ItemLocacao {
        final Locacao locacao;
        final Cliente cliente;
        final Long saldo;

        ItemLocacao(Locacao locacao, Cliente cliente, Long saldo) {
            this.locacao = locacao;
            this.cliente = cliente;
            this.saldo = saldo;
        }
    }
}