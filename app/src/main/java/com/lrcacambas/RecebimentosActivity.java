package com.lrcacambas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.lrcacambas.data.local.database.AppDatabase;
import com.lrcacambas.data.local.database.DatabaseProvider;
import com.lrcacambas.data.repository.CacambaRepository;
import com.lrcacambas.data.repository.ClienteRepository;
import com.lrcacambas.data.repository.EnderecoClienteRepository;
import com.lrcacambas.data.repository.LocacaoRepository;
import com.lrcacambas.data.repository.MovimentacaoCacambaRepository;
import com.lrcacambas.data.repository.RecebimentoRepository;
import com.lrcacambas.domain.service.LocacaoService;
import com.lrcacambas.domain.service.RecebimentoService;

public class RecebimentosActivity extends AppCompatActivity {

    private Button btnDetalhesFinanceiros;
    private Button btnRegistrarRecebimento;
    private Button btnLocacoesInadimplentes;
    private Button btnHistoricoRecebimentos;

    private RecebimentoService recebimentoService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.enableEdgeToEdge(getWindow());

        setContentView(R.layout.activity_recebimentos);

        // Instância do banco de dados
        AppDatabase database = DatabaseProvider.getDatabase(this);

// Repositories
        ClienteRepository clienteRepository =
                new ClienteRepository(database.clienteDao());

        EnderecoClienteRepository enderecoRepository =
                new EnderecoClienteRepository(database.enderecoClienteDao());

        CacambaRepository cacambaRepository =
                new CacambaRepository(database.cacambaDao());

        LocacaoRepository locacaoRepository =
                new LocacaoRepository(database.locacaoDao());

        MovimentacaoCacambaRepository movimentacaoRepository =
                new MovimentacaoCacambaRepository(database.movimentacaoCacambaDao());

        RecebimentoRepository recebimentoRepository =
                new RecebimentoRepository(database.recebimentoDao());

// LocacaoService
        LocacaoService locacaoService = new LocacaoService(
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

        View root = findViewById(R.id.main);

        int paddingLeft = root.getPaddingLeft();
        int paddingTop = root.getPaddingTop();
        int paddingRight = root.getPaddingRight();
        int paddingBottom = root.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {

            Insets systemBars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
            );

            v.setPadding(
                    paddingLeft + systemBars.left,
                    paddingTop + systemBars.top,
                    paddingRight + systemBars.right,
                    paddingBottom + systemBars.bottom
            );

            return insets;
        });

        btnDetalhesFinanceiros = findViewById(R.id.btnDetalhesFinanceiros);
        btnRegistrarRecebimento = findViewById(R.id.btnRegistrarRecebimento);
        btnLocacoesInadimplentes = findViewById(R.id.btnLocacoesInadimplentes);
        btnHistoricoRecebimentos = findViewById(R.id.btnHistoricoRecebimentos);
        btnDetalhesFinanceiros.setOnClickListener(v -> {
            Intent intent = new Intent(
                    RecebimentosActivity.this,
                    DetalhesFinanceirosActivity.class
            );
            startActivity(intent);
        });

        btnRegistrarRecebimento.setOnClickListener(v -> {
            Intent intent = new Intent(
                    RecebimentosActivity.this,
                    RegistrarRecebimentoActivity.class
            );
            startActivity(intent);
        });

        btnLocacoesInadimplentes.setOnClickListener(v -> {
            Intent intent = new Intent(
                    RecebimentosActivity.this,
                    LocacoesInadimplentesActivity.class
            );
            startActivity(intent);
        });

        btnHistoricoRecebimentos.setOnClickListener(v -> {
            Intent intent = new Intent(
                    RecebimentosActivity.this,
                    HistoricoRecebimentosActivity.class
            );
            startActivity(intent);
        });
    }
}