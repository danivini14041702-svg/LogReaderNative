package com.example.nativelogreader;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;

public class ErrorDetailActivity extends Activity {
    private int erroAtualIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error_detail);

        erroAtualIndex = getIntent().getIntExtra("index", 0);
        
        Button btnErrorSelector = findViewById(R.id.btnErrorSelector);
        Button btnBack = findViewById(R.id.btnBack);
        Button btnShare = findViewById(R.id.btnShare);
        Button btnMenu = findViewById(R.id.btnMenu); // Menu da direita

        atualizarTela();

        // 1. Ação do Seletor (Bottom Sheet / Popup)
        btnErrorSelector.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, btnErrorSelector);
            for (int i = 0; i < ErrorDatabase.errosEncontrados.size(); i++) {
                ErrorPattern err = ErrorDatabase.errosEncontrados.get(i);
                // Adiciona no menu, mostrando o título colorido pela gravidade
                popup.getMenu().add(0, i, i, (err.severity.equals("CRITICO") ? "🔴 " : "🟡 ") + err.title);
            }
            
            popup.setOnMenuItemClickListener(item -> {
                // Troca para o erro selecionado
                erroAtualIndex = item.getItemId();
                atualizarTela();
                return true;
            });
            popup.show();
        });

        // 2. Ação do Menu (Configurações)
        btnMenu.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(this, btnMenu);
            menu.getMenu().add("Tema Escuro (Padrão)");
            menu.getMenu().add("Ajuda");
            menu.getMenu().add("Sobre o App");
            menu.show();
        });

        // 3. Compartilhar
        btnShare.setOnClickListener(v -> {
            ErrorPattern err = ErrorDatabase.errosEncontrados.get(erroAtualIndex);
            String texto = "🚨 " + err.title + "\n\n" + err.meaning + "\n\nCausas:\n- " + String.join("\n- ", err.causes);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, texto);
            startActivity(Intent.createChooser(shareIntent, "Compartilhar erro..."));
        });

        // 4. Voltar
        btnBack.setOnClickListener(v -> finish());
    }

    private void atualizarTela() {
        ErrorPattern err = ErrorDatabase.errosEncontrados.get(erroAtualIndex);
        
        Button btnErrorSelector = findViewById(R.id.btnErrorSelector);
        TextView tvContent = findViewById(R.id.tvDetailContent);

        // Muda o texto e a cor do botão do seletor dependendo da gravidade
        btnErrorSelector.setText(err.title + " 🔽");
        if (err.severity.equals("CRITICO")) {
            btnErrorSelector.setTextColor(Color.parseColor("#FF6B6B"));
        } else {
            btnErrorSelector.setTextColor(Color.parseColor("#FBBF24"));
        }

        // Monta o texto da explicação
        StringBuilder texto = new StringBuilder();
        texto.append("👉 TRADUZINDO:\n").append(err.meaning).append("\n\n");
        texto.append("🔍 CAUSAS PROVÁVEIS:\n");
        for (String causa : err.causes) texto.append("   • ").append(causa).append("\n");
        texto.append("\n🛠️ FONTES PARA PESQUISA:\n");
        for (String fonte : err.sources) texto.append("   • ").append(fonte).append("\n");

        tvContent.setText(texto.toString());
    }
}
