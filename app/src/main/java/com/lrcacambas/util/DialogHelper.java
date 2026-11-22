package com.lrcacambas.util;

import android.app.Activity;
import android.app.Dialog;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.lrcacambas.R;

public class DialogHelper {

    public static void mostrarConfirmacao(
            Activity activity,
            String titulo,
            String mensagem,
            Runnable aoConfirmar) {

        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return;
        }

        Dialog dialog = criarDialog(activity, titulo, mensagem);

        TextView btnCancelar = dialog.findViewById(R.id.btnCancelarDialog);
        TextView btnConfirmar = dialog.findViewById(R.id.btnConfirmarDialog);

        btnCancelar.setVisibility(android.view.View.VISIBLE);
        btnCancelar.setText("Cancelar");
        btnConfirmar.setText("Confirmar");

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnConfirmar.setOnClickListener(v -> {
            dialog.dismiss();

            if (aoConfirmar != null) {
                aoConfirmar.run();
            }
        });

        dialog.show();
        ajustarTamanho(dialog, activity);
    }

    public static void mostrarSucesso(
            Activity activity,
            String titulo,
            String mensagem,
            Runnable aoFechar) {

        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return;
        }

        Dialog dialog = criarDialog(activity, titulo, mensagem);

        TextView btnCancelar = dialog.findViewById(R.id.btnCancelarDialog);
        TextView btnConfirmar = dialog.findViewById(R.id.btnConfirmarDialog);

        btnCancelar.setVisibility(android.view.View.GONE);
        btnConfirmar.setText("OK");

        btnConfirmar.setOnClickListener(v -> {
            dialog.dismiss();

            if (aoFechar != null) {
                aoFechar.run();
            }
        });

        dialog.show();
        ajustarTamanho(dialog, activity);
    }

    private static Dialog criarDialog(
            Activity activity,
            String titulo,
            String mensagem) {

        Dialog dialog = new Dialog(activity);

        dialog.setContentView(R.layout.dialog_padrao);
        dialog.setCancelable(true);

        TextView txtTitulo = dialog.findViewById(R.id.txtTituloDialog);
        TextView txtMensagem = dialog.findViewById(R.id.txtMensagemDialog);

        txtTitulo.setText(titulo);
        txtMensagem.setText(mensagem);

        Window window = dialog.getWindow();

        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        return dialog;
    }

    private static void ajustarTamanho(Dialog dialog, Activity activity) {

        Window window = dialog.getWindow();

        if (window != null) {
            int largura = (int) (activity.getResources()
                    .getDisplayMetrics().widthPixels * 0.90);

            window.setLayout(
                    largura,
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
        }
    }
}