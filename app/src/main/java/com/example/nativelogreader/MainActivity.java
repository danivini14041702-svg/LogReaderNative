package com.example.nativelogreader;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int PICK_LOG_FILE = 1;
    private TextView tvSummary;
    private LinearLayout containerErros;
    private DrawerLayout drawerLayout;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = getSharedPreferences("config", MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", true);
        AppCompatDelegate.setDefaultNightMode(isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawerLayout);
        tvSummary = findViewById(R.id.tvSummary);
        containerErros = findViewById(R.id.containerErros);
        Switch switchTema = findViewById(R.id.switchTema);
        
        switchTema.setChecked(isDarkMode);

        findViewById(R.id.btnMenuLateral).setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        switchTema.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("dark_mode", isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        EditText edtGemini = findViewById(R.id.edtGemini);
        findViewById(R.id.btnPerguntarGemini).setOnClickListener(v -> {
            String pergunta = edtGemini.getText().toString().trim();
            if (!pergunta.isEmpty()) {
                String url = "https://google.com/search?udm=50&q=" + Uri.encode(pergunta);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            } else {
                Toast.makeText(this, "Digite uma pergunta primeiro!", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btnCompartilharApp).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, "Conheça o Log Reader! O app que traduz erros de Android para o português. (Manual do Mundo para Idiotas)");
            startActivity(Intent.createChooser(intent, "Compartilhar App"));
        });

        findViewById(R.id.btnSelectFile).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(intent, PICK_LOG_FILE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_LOG_FILE && resultCode == RESULT_OK && data != null) {
            processLogFile(data.getData());
        }
    }

    private void processLogFile(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) content.append(line).append("\n");
            reader.close();
            analyzeLog(content.toString());
        } catch (Exception e) {
            Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void analyzeLog(String content) {
        containerErros.removeAllViews();
        ErrorDatabase.errosEncontrados.clear();
        List<ErrorPattern> knownErrors = ErrorDatabase.getKnownPatterns();
        int encontrados = 0;

        for (int i = 0; i < knownErrors.size(); i++) {
            ErrorPattern err = knownErrors.get(i);
            if (content.toLowerCase().contains(err.pattern.toLowerCase())) {
                ErrorDatabase.errosEncontrados.add(err);
                criarCartaoDeErro(err, ErrorDatabase.errosEncontrados.size() - 1);
                encontrados++;
            }
        }

        if (encontrados == 0) {
            tvSummary.setText("Nenhum erro conhecido encontrado neste log.");
        } else {
            tvSummary.setText("Encontramos " + encontrados + " erro(s) que podem ser a causa do problema:");
        }
    }

    private void criarCartaoDeErro(ErrorPattern err, int index) {
        Button card = new Button(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 16);
        card.setLayoutParams(params);

        if (err.severity.equals("CRITICO")) {
            card.setBackgroundColor(0xFF8B0000);
        } else {
            card.setBackgroundColor(0xFFFF8C00);
        }

        card.setTextColor(0xFFFFFFFF);
        card.setTextSize(16f);
        card.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        card.setPadding(30, 30, 30, 30);
        card.setText("🚨 " + err.title);

        card.setOnClickListener(v -> {
            Intent intent = new Intent(this, ErrorDetailActivity.class);
            intent.putExtra("index", index);
            startActivity(intent);
        });

        containerErros.addView(card);
    }
}
