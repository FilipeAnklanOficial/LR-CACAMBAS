package com.lrcacambas.util;

import android.content.Context;

import com.lrcacambas.data.local.database.AppDatabase;
import com.lrcacambas.data.local.database.DatabaseProvider;
import com.lrcacambas.data.repository.ClienteRepository;
import com.lrcacambas.data.repository.EnderecoClienteRepository;
import com.lrcacambas.data.repository.CacambaRepository;
import com.lrcacambas.data.repository.LocacaoRepository;
import com.lrcacambas.data.repository.MovimentacaoCacambaRepository;
import com.lrcacambas.data.repository.RecebimentoRepository;
import com.lrcacambas.data.repository.DespesaRepository;

import com.lrcacambas.domain.service.ClienteService;
import com.lrcacambas.domain.service.EnderecoClienteService;
import com.lrcacambas.domain.service.CacambaService;
import com.lrcacambas.domain.service.LocacaoService;
import com.lrcacambas.domain.service.RecebimentoService;
import com.lrcacambas.domain.service.DespesaService;

public class AppContainer {

    public final AppDatabase database;

    public final ClienteService clienteService;
    public final EnderecoClienteService enderecoClienteService;
    public final CacambaService cacambaService;
    public final LocacaoService locacaoService;
    public final RecebimentoService recebimentoService;
    public final DespesaService despesaService;

    public AppContainer(Context context) {

        // Banco de dados
        database = DatabaseProvider.getDatabase(context);

        // Repositórios
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

        DespesaRepository despesaRepository =
                new DespesaRepository(database.despesaDao());

        // Serviços
        clienteService = new ClienteService(clienteRepository);

        enderecoClienteService =
                new EnderecoClienteService(enderecoRepository, clienteRepository);

        cacambaService = new CacambaService(
                cacambaRepository,
                movimentacaoRepository
        );

        locacaoService = new LocacaoService(
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

        despesaService = new DespesaService(despesaRepository);
    }
}