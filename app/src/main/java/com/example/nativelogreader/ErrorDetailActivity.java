package com.example.nativelogreader;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
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
                abrirGemini(pergunta);
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

        boolean isDarkMode = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        int corTexto = isDarkMode ? 0xFFE0E0E0 : 0xFF212121; 

        LinearLayout containerCausas = findViewById(R.id.containerCausas);
        containerCausas.removeAllViews();
        for (String causa : err.causes) {
            TextView t = new TextView(this);
            t.setText("• " + causa);
            t.setTextColor(corTexto);
            t.setTextSize(15f);
            t.setTextIsSelectable(true); 
            t.setPadding(0, 8, 0, 8);
            containerCausas.addView(t);
        }

        // --- NOVA LÓGICA DA SEÇÃO DE PESQUISA ---
        LinearLayout containerFontes = findViewById(R.id.containerFontes);
        containerFontes.removeAllViews();

        // 1. Título do Google
        TextView tvGoogleTitle = new TextView(this);
        tvGoogleTitle.setText("No Google pesquise por:");
        tvGoogleTitle.setTextColor(0xFF888888); // Cinza
        tvGoogleTitle.setTextSize(14f);
        tvGoogleTitle.setPadding(0, 0, 0, 8);
        containerFontes.addView(tvGoogleTitle);

        // 2. Termos de busca do Google
        for (String fonte : err.sources) {
            if (fonte.startsWith("Pesquisar:")) {
                String termo = fonte.replace("Pesquisar:", "").trim();
                TextView t = new TextView(this);
                t.setText("🔎 " + termo);
                t.setTextColor(0xFF1976D2); // Azul
                t.setTextSize(15f);
                t.setPadding(0, 8, 0, 16);
                t.setOnClickListener(v -> abrirLink("https://www.google.com/search?q=" + Uri.encode(termo)));
                containerFontes.addView(t);
            }
        }

        // 3. Título dos Sites
        TextView tvSitesTitle = new TextView(this);
        tvSitesTitle.setText("Sites recomendados:");
        tvSitesTitle.setTextColor(0xFF888888);
        tvSitesTitle.setTextSize(14f);
        tvSitesTitle.setPadding(0, 8, 0, 8);
        containerFontes.addView(tvSitesTitle);

        // 4. Links dos Sites (formatados a partir das strings do ErrorDatabase)
        for (String fonte : err.sources) {
            if (fonte.startsWith("StackOverflow:")) {
                String termo = fonte.replace("StackOverflow:", "").trim();
                TextView t = new TextView(this);
                t.setText("🌐 StackOverflow");
                t.setTextColor(0xFF1976D2);
                t.setTextSize(15f);
                t.setPadding(0, 8, 0, 8);
                t.setOnClickListener(v -> abrirLink("https://stackoverflow.com/search?q=" + Uri.encode(termo)));
                containerFontes.addView(t);
            } else if (fonte.startsWith("XDA")) {
                TextView t = new TextView(this);
                t.setText("🌐 XDA Developers");
                t.setTextColor(0xFF1976D2);
                t.setTextSize(15f);
                t.setPadding(0, 8, 0, 8);
                t.setOnClickListener(v -> abrirLink("https://www.google.com/search?q=site:xda-developers.com+" + Uri.encode(err.pattern)));
                containerFontes.addView(t);
            }
        }
    }

    private void abrirGemini(String pergunta) {
        // Forçando o www e a categoria BROWSABLE para tentar enganar o app do Google
        String url = "https://www.google.com/search?q=" + Uri.encode(pergunta) + "&udm=50";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setPackage("com.android.chrome");
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            intent.setPackage(null);
            startActivity(Intent.createChooser(intent, "Escolha o navegador"));
        }
    }

    private void abrirLink(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}
