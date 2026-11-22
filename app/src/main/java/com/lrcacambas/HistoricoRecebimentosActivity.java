package com.lrcacambas;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.lrcacambas.data.local.entity.Cliente;
import com.lrcacambas.data.local.entity.Locacao;
import com.lrcacambas.data.local.entity.Recebimento;
import com.lrcacambas.domain.service.ClienteService;
import com.lrcacambas.domain.service.LocacaoService;
import com.lrcacambas.domain.service.RecebimentoService;
import com.lrcacambas.util.AppContainer;
import com.lrcacambas.util.AppExecutors;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class HistoricoRecebimentosActivity extends AppCompatActivity {

    private LinearLayout containerHistoricoRecebimentos;

    private ClienteService clienteService;
    private LocacaoService locacaoService;
    private RecebimentoService recebimentoService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico_recebimentos);

        containerHistoricoRecebimentos = findViewById(R.id.containerHistoricoRecebimentos);

        AppContainer appContainer = new AppContainer(this);
        clienteService = appContainer.clienteService;
        locacaoService = appContainer.locacaoService;
        recebimentoService = appContainer.recebimentoService;
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarHistorico();
    }

    private void carregarHistorico() {
        AppExecutors.getDatabaseExecutor().execute(() -> {

            List<Recebimento> recebimentos = recebimentoService.listarTodos();

            runOnUiThread(() -> {
                containerHistoricoRecebimentos.removeAllViews();

                if (recebimentos == null || recebimentos.isEmpty()) {
                    TextView tvVazio = criarTexto(
                            "Nenhum recebimento registrado.",
                            16,
                            Color.DKGRAY,
                            false
                    );

                    tvVazio.setPadding(dp(12), dp(20), dp(12), dp(20));
                    containerHistoricoRecebimentos.addView(tvVazio);
                    return;
                }

                for (Recebimento recebimento : recebimentos) {
                    carregarDadosCard(recebimento);
                }
            });
        });
    }

    private void carregarDadosCard(Recebimento recebimento) {
        AppExecutors.getDatabaseExecutor().execute(() -> {

            Locacao locacao = locacaoService.buscarPorId(recebimento.getIdLocacao());

            if (locacao == null) return;

            Cliente cliente = clienteService.buscarPorId(locacao.getIdCliente());

            runOnUiThread(() -> {
                containerHistoricoRecebimentos.addView(
                        criarCard(recebimento, locacao, cliente)
                );
            });
        });
    }

    private LinearLayout criarCard(
            Recebimento recebimento,
            Locacao locacao,
            Cliente cliente
    ) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(16), dp(18), dp(16));

        GradientDrawable fundo = new GradientDrawable();
        fundo.setColor(Color.WHITE);
        fundo.setCornerRadius(dp(16));
        card.setBackground(fundo);

        LinearLayout.LayoutParams parametros = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        parametros.bottomMargin = dp(14);
        card.setLayoutParams(parametros);

        NumberFormat formato = NumberFormat.getCurrencyInstance(
                new Locale("pt", "BR")
        );

        String nomeCliente = cliente != null
                ? cliente.getNome()
                : "Cliente não encontrado";

        String formaPagamento = formatarFormaPagamento(
                recebimento.getFormaPagamento()
        );

        String data = recebimento.getDataRecebimento() != null
                ? recebimento.getDataRecebimento().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy")
        )
                : "Data não informada";

        card.addView(criarTexto(nomeCliente, 19, Color.rgb(8, 43, 80), true));

        card.addView(criarTexto(
                formatarEndereco(locacao),
                14,
                Color.DKGRAY,
                false
        ));

        card.addView(criarTexto(
                "Forma de pagamento: " + formaPagamento,
                14,
                Color.DKGRAY,
                true
        ));

        card.addView(criarTexto(
                "Data: " + data,
                14,
                Color.DKGRAY,
                false
        ));

        card.addView(criarTexto(
                "Valor recebido: " + formatarMoeda(recebimento.getValorRecebido(), formato),
                17,
                Color.rgb(30, 130, 70),
                true
        ));

        card.addView(criarTexto(
                "Desconto concedido: " + formatarMoeda(recebimento.getDescontoValor(), formato),
                14,
                Color.DKGRAY,
                false
        ));

        return card;
    }

    private TextView criarTexto(String texto, int tamanho, int cor, boolean negrito) {
        TextView tv = new TextView(this);
        tv.setText(texto);
        tv.setTextSize(tamanho);
        tv.setTextColor(cor);

        if (negrito) {
            tv.setTypeface(null, Typeface.BOLD);
        }

        tv.setPadding(0, dp(4), 0, dp(4));
        return tv;
    }

    private String formatarEndereco(Locacao locacao) {
        StringBuilder endereco = new StringBuilder();

        if (locacao.getLogradouro() != null && !locacao.getLogradouro().isEmpty()) {
            endereco.append(locacao.getLogradouro());
        }

        if (locacao.getNumero() != null && !locacao.getNumero().isEmpty()) {
            endereco.append(", ").append(locacao.getNumero());
        }

        if (locacao.getBairro() != null && !locacao.getBairro().isEmpty()) {
            endereco.append(" - ").append(locacao.getBairro());
        }

        if (locacao.getCidade() != null && !locacao.getCidade().isEmpty()) {
            endereco.append(" - ").append(locacao.getCidade());
        }

        return endereco.length() > 0
                ? endereco.toString()
                : "Endereço não informado";
    }

    private String formatarFormaPagamento(
            com.lrcacambas.data.local.entity.FormaPagamento forma
    ) {
        if (forma == null) return "Não informada";

        switch (forma) {
            case PIX:
                return "Pix";
            case DINHEIRO:
                return "Dinheiro";
            case CARTAO:
                return "Cartão";
            default:
                return forma.name();
        }
    }

    private String formatarMoeda(Long valor, NumberFormat formato) {
        return formato.format((valor == null ? 0L : valor) / 100.0);
    }

    private int dp(int valor) {
        return (int) (valor * getResources().getDisplayMetrics().density);
    }
}