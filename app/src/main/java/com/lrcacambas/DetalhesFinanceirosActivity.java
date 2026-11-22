package com.lrcacambas;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.lrcacambas.data.local.database.AppDatabase;
import com.lrcacambas.data.local.database.DatabaseProvider;
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
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DetalhesFinanceirosActivity extends AppCompatActivity {

    private TextView tvTotalAReceber;
    private TextView tvLocacoesAtivas;

    private RecebimentoService recebimentoService;
    private LocacaoRepository locacaoRepository;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.enableEdgeToEdge(getWindow());

        setContentView(R.layout.activity_detalhes_financeiros);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
            );

            return insets;
        });

        tvTotalAReceber = findViewById(R.id.tvTotalAReceber);
        tvLocacoesAtivas = findViewById(R.id.tvLocacoesAtivas);

        AppDatabase database = DatabaseProvider.getDatabase(this);

        ClienteRepository clienteRepository =
                new ClienteRepository(database.clienteDao());

        EnderecoClienteRepository enderecoRepository =
                new EnderecoClienteRepository(database.enderecoClienteDao());

        CacambaRepository cacambaRepository =
                new CacambaRepository(database.cacambaDao());

        locacaoRepository =
                new LocacaoRepository(database.locacaoDao());

        MovimentacaoCacambaRepository movimentacaoRepository =
                new MovimentacaoCacambaRepository(database.movimentacaoCacambaDao());

        LocacaoService locacaoService = new LocacaoService(
                database,
                locacaoRepository,
                clienteRepository,
                enderecoRepository,
                cacambaRepository,
                movimentacaoRepository
        );

        RecebimentoRepository recebimentoRepository =
                new RecebimentoRepository(database.recebimentoDao());

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
        atualizarResumo();
    }

    private void atualizarResumo() {

        executor.execute(() -> {

            long totalAReceber =
                    recebimentoService.calcularTotalAReceber();

            int locacoesAtivas =
                    locacaoRepository.contarPorStatus(StatusLocacao.ATIVA);

            runOnUiThread(() -> {
                tvTotalAReceber.setText(formatarMoeda(totalAReceber));
                tvLocacoesAtivas.setText(String.valueOf(locacoesAtivas));
            });
        });
    }

    private String formatarMoeda(long valorEmCentavos) {

        NumberFormat formato =
                NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        return formato.format(BigDecimal.valueOf(valorEmCentavos, 2));
    }

    @Override
    protected void onDestroy() {
        executor.shutdown();
        super.onDestroy();
    }
}