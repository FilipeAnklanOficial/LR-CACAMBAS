package com.lrcacambas;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;
import android.app.Dialog;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.lrcacambas.data.local.entity.Cacamba;
import com.lrcacambas.data.local.entity.StatusCacamba;
import com.lrcacambas.util.AppContainer;
import com.lrcacambas.util.AppExecutors;


import java.util.Locale;

public class DetalhesCacambaActivity extends AppCompatActivity {

    private AppContainer appContainer;

    private TextView txtCodigoCacamba;
    private TextView txtStatusCacamba;
    private TextView txtObservacaoStatus;

    private Button btnAlterarStatus;

    private Long idCacamba;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_detalhes_cacamba);
        appContainer = new AppContainer(getApplicationContext());

        idCacamba = getIntent().getLongExtra("idCacamba", -1L);

        txtCodigoCacamba = findViewById(R.id.txtCodigoCacamba);
        txtStatusCacamba = findViewById(R.id.txtStatusCacamba);
        txtObservacaoStatus = findViewById(R.id.txtObservacaoStatus);

        btnAlterarStatus = findViewById(R.id.btnAlterarStatus);

        carregarCacamba();
    }

    private void carregarCacamba() {
        if (idCacamba == null || idCacamba <= 0) {
            Toast.makeText(this, "Caçamba inválida.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        AppExecutors.getDatabaseExecutor().execute(() -> {
            try {
                Cacamba cacamba = appContainer.cacambaService.buscarPorId(idCacamba);

                runOnUiThread(() -> {
                    txtCodigoCacamba.setText(
                            String.format(Locale.getDefault(), "%03d", cacamba.getCodigo())
                    );

                    txtStatusCacamba.setText(formatarStatus(cacamba));

                    configurarBotaoAlterarStatus(cacamba);

                    if (cacamba.getObservacaoStatus() != null
                            && !cacamba.getObservacaoStatus().trim().isEmpty()) {
                        txtObservacaoStatus.setText(
                                "Observação: " + cacamba.getObservacaoStatus()
                        );
                        txtObservacaoStatus.setVisibility(View.VISIBLE);
                    } else {
                        txtObservacaoStatus.setVisibility(View.GONE);
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(
                            this,
                            "Erro ao carregar a caçamba: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                    finish();
                });
            }
        });
    }

    private String formatarStatus(Cacamba cacamba) {
        switch (cacamba.getStatus()) {
            case DISPONIVEL:
                return "Disponível";

            case EM_USO:
                return "Em uso";

            case INATIVA:
                return "Inativa";

            case MANUTENCAO:
                return "Em manutenção";

            default:
                return "Não informado";
        }
    }

    private void configurarBotaoAlterarStatus(Cacamba cacamba) {
        btnAlterarStatus.setOnClickListener(v -> {

            if (cacamba.getStatus() == StatusCacamba.EM_USO) {
                Toast.makeText(
                        this,
                        "Não é possível alterar a situação de uma caçamba em uso.",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            exibirDialogoStatus(cacamba);
        });
    }

    private void solicitarObservacao(StatusCacamba novoStatus) {

        if (novoStatus == StatusCacamba.DISPONIVEL) {
            alterarStatus(novoStatus, null);
            return;
        }

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_observacao_cacamba);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(
                    android.R.color.transparent
            );
        }

        TextView txtTitulo = dialog.findViewById(R.id.txtTituloObservacao);
        EditText edtObservacao = dialog.findViewById(R.id.edtObservacaoStatus);
        TextView btnCancelar = dialog.findViewById(R.id.btnCancelarObservacao);
        Button btnSalvar = dialog.findViewById(R.id.btnSalvarObservacao);

        if (novoStatus == StatusCacamba.MANUTENCAO) {
            txtTitulo.setText("Motivo da manutenção");
        } else {
            txtTitulo.setText("Motivo da inativação");
        }

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnSalvar.setOnClickListener(v -> {
            String observacao = edtObservacao.getText()
                    .toString()
                    .trim();

            if (observacao.isEmpty()) {
                edtObservacao.setError("Informe uma observação.");
                edtObservacao.requestFocus();
                return;
            }

            dialog.dismiss();
            alterarStatus(novoStatus, observacao);
        });

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.88),
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private void alterarStatus(StatusCacamba novoStatus, String observacao) {

        AppExecutors.getDatabaseExecutor().execute(() -> {
            try {
                appContainer.cacambaService.alterarStatus(
                        idCacamba,
                        novoStatus,
                        observacao
                );

                runOnUiThread(() -> {
                    Toast.makeText(
                            this,
                            "Situação alterada com sucesso!",
                            Toast.LENGTH_SHORT
                    ).show();

                    carregarCacamba();
                });

            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(
                        this,
                        e.getMessage(),
                        Toast.LENGTH_LONG
                ).show());
            }
        });
    }

    private void exibirDialogoStatus(Cacamba cacamba) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_alterar_status_cacamba);
        dialog.getWindow().setBackgroundDrawableResource(
                android.R.color.transparent
        );

        Button btnOpcao1 = dialog.findViewById(R.id.btnStatusOpcao1);
        Button btnOpcao2 = dialog.findViewById(R.id.btnStatusOpcao2);
        TextView btnCancelar = dialog.findViewById(R.id.btnCancelarStatus);

        switch (cacamba.getStatus()) {
            case DISPONIVEL:
                btnOpcao1.setText("Em manutenção");
                btnOpcao1.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(
                                getColor(R.color.amarelo_principal)
                        )
                );
                btnOpcao1.setTextColor(getColor(R.color.azul_marinho));

                btnOpcao2.setText("Inativar");
                btnOpcao2.setVisibility(View.VISIBLE);
                btnOpcao2.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(
                                android.graphics.Color.parseColor("#D32F2F")
                        )
                );
                btnOpcao2.setTextColor(android.graphics.Color.WHITE);
                break;

            case MANUTENCAO:
                btnOpcao1.setText("Disponível");
                btnOpcao1.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(
                                android.graphics.Color.parseColor("#2E7D32")
                        )
                );
                btnOpcao1.setTextColor(android.graphics.Color.WHITE);
                btnOpcao2.setText("Inativar");
                btnOpcao2.setVisibility(View.VISIBLE);
                btnOpcao2.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(
                                android.graphics.Color.parseColor("#D32F2F")
                        )
                );
                btnOpcao2.setTextColor(android.graphics.Color.WHITE);
                break;

            case INATIVA:
                btnOpcao1.setText("Reativar como disponível");
                btnOpcao1.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(
                                android.graphics.Color.parseColor("#2E7D32")
                        )
                );
                btnOpcao1.setTextColor(
                        android.graphics.Color.WHITE
                );

                btnOpcao2.setVisibility(View.GONE);
                break;

            default:
                dialog.dismiss();
                return;
        }

        btnOpcao1.setOnClickListener(v -> {
            StatusCacamba novoStatus;

            if (cacamba.getStatus() == StatusCacamba.DISPONIVEL) {
                novoStatus = StatusCacamba.MANUTENCAO;
            } else {
                novoStatus = StatusCacamba.DISPONIVEL;
            }

            dialog.dismiss();
            solicitarObservacao(novoStatus);
        });

        btnOpcao2.setOnClickListener(v -> {
            dialog.dismiss();
            solicitarObservacao(StatusCacamba.INATIVA);
        });

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.88),
                    android.view.WindowManager.LayoutParams.WRAP_CONTENT
            );
        }
    }
}