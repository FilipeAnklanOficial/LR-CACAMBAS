package com.lrcacambas.data.local.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.lrcacambas.data.local.entity.Cacamba;
import com.lrcacambas.data.local.entity.Cliente;
import com.lrcacambas.data.local.entity.Despesa;
import com.lrcacambas.data.local.entity.EnderecoCliente;
import com.lrcacambas.data.local.entity.Locacao;
import com.lrcacambas.data.local.entity.MovimentacaoCacamba;
import com.lrcacambas.data.local.entity.Recebimento;
import com.lrcacambas.data.local.dao.ClienteDao;
import com.lrcacambas.data.local.dao.EnderecoClienteDao;
import com.lrcacambas.data.local.dao.CacambaDao;
import com.lrcacambas.data.local.dao.LocacaoDao;
import com.lrcacambas.data.local.dao.MovimentacaoCacambaDao;
import com.lrcacambas.data.local.dao.RecebimentoDao;
import com.lrcacambas.data.local.dao.DespesaDao;

@Database(
        entities = {
                Cliente.class,
                EnderecoCliente.class,
                Cacamba.class,
                Locacao.class,
                MovimentacaoCacamba.class,
                Recebimento.class,
                Despesa.class
        },
        version = 1,
        exportSchema = false
)


@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ClienteDao clienteDao();

    public abstract EnderecoClienteDao enderecoClienteDao();

    public abstract CacambaDao cacambaDao();

    public abstract LocacaoDao locacaoDao();

    public abstract MovimentacaoCacambaDao movimentacaoCacambaDao();

    public abstract RecebimentoDao recebimentoDao();

    public abstract DespesaDao despesaDao();
}