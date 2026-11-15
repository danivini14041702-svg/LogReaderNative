package com.example.nativelogreader;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import java.util.List;

public class ErrorDetailActivity extends AppCompatActivity {
    private int currentIndex = 0;
    private List<ErrorPattern> lista;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error_detail);

        drawerLayout = findViewById(R.id.drawerLayoutDetalhe);
        lista = ErrorDatabase.errosEncontrados;
        currentIndex = getIntent().getIntExtra("index", 0);
        if (currentIndex < 0 || currentIndex >= lista.size()) currentIndex = 0;

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());
        findViewById(R.id.btnMenuLateralDetalhe).setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        SharedPreferences prefs = getSharedPreferences("config", MODE_PRIVATE);
        Switch switchTema = findViewById(R.id.switchTemaDetalhe);
        switchTema.setChecked(prefs.getBoolean("dark_mode", true));
        switchTema.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("dark_mode", isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            recreate();
        });

        EditText edtGemini = findViewById(R.id.edtGeminiDetalhe);
        findViewById(R.id.btnPerguntarGeminiDetalhe).setOnClickListener(v -> {
            String pergunta = edtGemini.getText().toString().trim();
            if (!pergunta.isEmpty()) {
                // URL corrigida para abrir no Chrome com a IA do Google
                String url = "https://google.com/search?udm=50&q=" + Uri.encode(pergunta);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            } else {
                Toast.makeText(this, "Digite uma pergunta!", Toast.LENGTH_SHORT).show();
            }
        });

        Button btnDropdown = findViewById(R.id.btnDropdownErro);
        btnDropdown.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, v);
            for (int i = 0; i < lista.size(); i++) {
                popup.getMenu().add(0, i, i, lista.get(i).title);
            }
            popup.setOnMenuItemClickListener(item -> {
                currentIndex = item.getItemId();
                preencherTela();
                return true;
            });
            popup.show();
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

        ((Button) findViewById(R.id.btnDropdownErro)).setText("🚨 " + err.title + "  ▼");

        TextView tvDesc = findViewById(R.id.tvDescricao);
        tvDesc.setText(err.description + "\n\n💡 " + err.tip);

        // Verifica se o modo escuro está ativo para escolher a cor do texto corretamente
        boolean isDarkMode = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        int corTexto = isDarkMode ? Color.WHITE : Color.BLACK;

        LinearLayout containerCausas = findViewById(R.id.containerCausas);
        containerCausas.removeAllViews();
        for (String causa : err.causes) {
            TextView t = new TextView(this);
            t.setText("• " + causa);
            t.setTextColor(corTexto); // Cor corrigida aqui
            t.setTextSize(15f);
            t.setTextIsSelectable(true); // Permite copiar
            t.setPadding(0, 8, 0, 8);
            containerCausas.addView(t);
        }

        LinearLayout containerFontes = findViewById(R.id.containerFontes);
        containerFontes.removeAllViews();
        for (String fonte : err.sources) {
            TextView t = new TextView(this);
            t.setText("🔎 " + fonte);
            t.setTextColor(0xFF1976D2); // Azul
            t.setTextSize(15f);
            t.setTextIsSelectable(true); // Permite copiar
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
