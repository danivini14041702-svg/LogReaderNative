package com.example.nativelogreader;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import java.util.List;

public class ErrorDetailActivity extends Activity {
    private int currentIndex = 0;
    private List<ErrorPattern> lista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error_detail);

        lista = ErrorDatabase.errosEncontrados;
        currentIndex = getIntent().getIntExtra("index", 0);
        if (currentIndex < 0 || currentIndex >= lista.size()) currentIndex = 0;

        // 1. Configurando o Menu Sanduíche (Canto Superior Esquerdo)
        Button btnSanduiche = findViewById(R.id.btnMenuSanduiche);
        btnSanduiche.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, v);
            popup.getMenu().add("Compartilhar este erro");
            popup.getMenu().add("Voltar para a lista");
            
            popup.setOnMenuItemClickListener(item -> {
                String titulo = item.getTitle().toString();
                if (titulo.equals("Compartilhar este erro")) {
                    compartilhar();
                    return true;
                } else if (titulo.equals("Voltar para a lista")) {
                    finish(); // Fecha a tela atual
                    return true;
                }
                return false;
            });
            popup.show();
        });

        // 2. Configurando o botão de pular para o próximo erro
        Button btnMenu = findViewById(R.id.btnMenuErro);
        btnMenu.setOnClickListener(v -> {
            if (lista.size() <= 1) return;
            currentIndex = (currentIndex + 1) % lista.size(); // pula pro próximo
            preencherTela();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        preencherTela();
    }

    private void preencherTela() {
        if (lista.isEmpty()) return;
        ErrorPattern err = lista.get(currentIndex);

        // Atualiza o botão do topo
        ((Button) findViewById(R.id.btnMenuErro)).setText(
            "🚨 " + err.title + "  ▼  (" + (currentIndex + 1) + "/" + lista.size() + ")");

        // Atualiza o texto principal
        TextView tvDesc = findViewById(R.id.tvDescricao);
        tvDesc.setText(err.description + "\n\n💡 " + err.tip);

        // Atualiza as causas
        LinearLayout containerCausas = findViewById(R.id.containerCausas);
        containerCausas.removeAllViews();
        for (String causa : err.causes) {
            TextView t = new TextView(this);
            t.setText("• " + causa);
            t.setTextColor(0xFFBBBBBB); // Cinza claro
            t.setTextSize(15f);
            t.setPadding(0, 8, 0, 8);
            containerCausas.addView(t);
        }

        // Atualiza as fontes e as transforma em links clicáveis!
        LinearLayout containerFontes = findViewById(R.id.containerFontes);
        containerFontes.removeAllViews();
        for (String fonte : err.sources) {
            TextView t = new TextView(this);
            t.setText("🔎 " + fonte);
            t.setTextColor(0xFF64B5F6); // Azul claro estilo link
            t.setTextSize(15f);
            t.setPadding(0, 12, 0, 12);
            
            // Ação de clicar e pesquisar no Google
            t.setOnClickListener(v -> {
                try {
                    // Transforma o texto da fonte em uma pesquisa no Google
                    String busca = Uri.encode(fonte.replace("Pesquisar: ", "").trim());
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + busca));
                    startActivity(browserIntent);
                } catch (Exception e) {
                    // Ignora se não tiver navegador
                }
            });
            containerFontes.addView(t);
        }
    }

    private void compartilhar() {
        if (lista.isEmpty()) return;
        ErrorPattern err = lista.get(currentIndex);
        StringBuilder sb = new StringBuilder();
        sb.append("🚨 ").append(err.title).append("\n\n");
        sb.append(err.description).append("\n\n");
        sb.append("💡 ").append(err.tip).append("\n\n");
        sb.append("Causas prováveis:\n");
        for (String c : err.causes) sb.append("• ").append(c).append("\n");
        
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, sb.toString());
        startActivity(Intent.createChooser(i, "Compartilhar erro"));
    }
}
