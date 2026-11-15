package com.example.nativelogreader;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class ErrorDetailActivity extends AppCompatActivity {
    private int currentIndex = 0;
    private List<ErrorPattern> lista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error_detail);

        lista = ErrorDatabase.errosEncontrados;
        currentIndex = getIntent().getIntExtra("index", 0);
        if (currentIndex < 0 || currentIndex >= lista.size()) currentIndex = 0;

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        Button btnDropdown = findViewById(R.id.btnDropdownErro);
        btnDropdown.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, v);
            // Adiciona todos os erros encontrados na lista do menu suspenso
            for (int i = 0; i < lista.size(); i++) {
                popup.getMenu().add(0, i, i, lista.get(i).title);
            }
            
            popup.setOnMenuItemClickListener(item -> {
                currentIndex = item.getItemId();
                preencherTela(); // Muda o texto na mesma tela
                return true;
            });
            popup.show();
        });
    }

    private void preencherTela() {
        if (lista.isEmpty()) return;
        ErrorPattern err = lista.get(currentIndex);

        // Atualiza o texto do botão no topo
        ((Button) findViewById(R.id.btnDropdownErro)).setText(
            "🚨 " + err.title + "  ▼");

        // Atualiza o texto explicativo
        TextView tvDesc = findViewById(R.id.tvDescricao);
        tvDesc.setText(err.description + "\n\n💡 " + err.tip);

        // Atualiza as causas
        LinearLayout containerCausas = findViewById(R.id.containerCausas);
        containerCausas.removeAllViews();
        for (String causa : err.causes) {
            TextView t = new TextView(this);
            t.setText("• " + causa);
            t.setTextColor(getResources().getColor(android.R.color.darker_gray));
            t.setTextSize(15f);
            t.setPadding(0, 8, 0, 8);
            containerCausas.addView(t);
        }

        // Atualiza as fontes (Links Clicáveis)
        LinearLayout containerFontes = findViewById(R.id.containerFontes);
        containerFontes.removeAllViews();
        for (String fonte : err.sources) {
            TextView t = new TextView(this);
            t.setText("🔎 " + fonte);
            t.setTextColor(0xFF1976D2); // Azul Material Design
            t.setTextSize(15f);
            t.setPadding(0, 12, 0, 12);
            
            t.setOnClickListener(v -> {
                try {
                    String busca = Uri.encode(fonte.replace("Pesquisar: ", "").trim());
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + busca));
                    startActivity(browserIntent);
                } catch (Exception ignored) {}
            });
            containerFontes.addView(t);
        }
    }
}
