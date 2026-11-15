package com.example.nativelogreader;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Pattern;

public class MainActivity extends Activity {
    private static final int PICK_LOG_FILE = 1;
    private TextView tvSummary;
    private LinearLayout containerErros;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvSummary = findViewById(R.id.tvSummary);
        containerErros = findViewById(R.id.containerErros);

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
        containerErros.removeAllViews(); // Limpa a tela
        ErrorDatabase.errosEncontrados.clear(); // Limpa a lista global
        List<ErrorPattern> knownErrors = ErrorDatabase.getKnownPatterns();
        int encontrados = 0;

        // Aqui está a correção do loop para pegar o índice
        for (int i = 0; i < knownErrors.size(); i++) {
            ErrorPattern err = knownErrors.get(i);
            if (content.toLowerCase().contains(err.pattern.toLowerCase())) {
                ErrorDatabase.errosEncontrados.add(err); // Salva na memória global
                criarCartaoDeErro(err, ErrorDatabase.errosEncontrados.size() - 1); // Passa o índice
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
        params.setMargins(0, 0, 0, 24);
        card.setLayoutParams(params);
        
        if (err.severity.equals("CRITICO")) {
            card.setBackgroundColor(Color.parseColor("#8B0000"));
        } else {
            card.setBackgroundColor(Color.parseColor("#B8860B"));
        }
        
        card.setTextColor(Color.WHITE);
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
