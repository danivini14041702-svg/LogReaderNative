package com.example.nativelogreader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
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

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        Button btnMenu = findViewById(R.id.btnMenuErro);
        btnMenu.setOnClickListener(v -> {
            if (lista.size() <= 1) return;
            currentIndex = (currentIndex + 1) % lista.size(); // pula pro próximo
            preencherTela();
        });

        findViewById(R.id.btnCompartilhar).setOnClickListener(v -> compartilhar());
    }

    @Override
    protected void onResume() {
        super.onResume();
        preencherTela();
    }

    private void preencherTela() {
        if (lista.isEmpty()) return;
        ErrorPattern err = lista.get(currentIndex);

        ((Button) findViewById(R.id.btnMenuErro)).setText(
            "🚨 " + err.title + "  ▼  (" + (currentIndex + 1) + "/" + lista.size() + ")");

        TextView tvDesc = findViewById(R.id.tvDescricao);
        tvDesc.setText(err.description + "\n\n💡 " + err.tip);

        LinearLayout containerCausas = findViewById(R.id.containerCausas);
        containerCausas.removeAllViews();
        for (String causa : err.causes) {
            TextView t = new TextView(this);
            t.setText("• " + causa);
            t.setTextSize(15f);
            t.setPadding(0, 8, 0, 8);
            containerCausas.addView(t);
        }

        LinearLayout containerFontes = findViewById(R.id.containerFontes);
        containerFontes.removeAllViews();
        for (String fonte : err.sources) {
            TextView t = new TextView(this);
            t.setText("🔎 " + fonte);
            t.setTextSize(14f);
            t.setPadding(0, 6, 0, 6);
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
        sb.append("\nFontes de pesquisa:\n");
        for (String s : err.sources) sb.append("• ").append(s).append("\n");

        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, sb.toString());
        startActivity(Intent.createChooser(i, "Compartilhar erro"));
    }
}
