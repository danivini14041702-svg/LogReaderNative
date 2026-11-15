package com.example.nativelogreader;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

public class MainActivity extends Activity {
    private static final int PICK_LOG_FILE = 1;
    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvResult = findViewById(R.id.tvResult);
        Button btnSelectFile = findViewById(R.id.btnSelectFile);

        btnSelectFile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(intent, PICK_LOG_FILE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_LOG_FILE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) processLogFile(uri);
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

            String logContent = content.toString();
            tvResult.setText(analyzeLog(logContent));
        } catch (Exception e) {
            Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String analyzeLog(String content) {
        String[] lines = content.split("\n");
        int errorCount = 0, warnCount = 0, infoCount = 0, debugCount = 0;
        StringBuilder keyIssues = new StringBuilder();

        Pattern errorP = Pattern.compile("\\bE/|ERROR|FATAL|Exception|SEVERE|CRITICAL|fail|failed", Pattern.CASE_INSENSITIVE);
        Pattern warnP = Pattern.compile("\\bW/|WARN|WARNING", Pattern.CASE_INSENSITIVE);
        Pattern infoP = Pattern.compile("\\bI/|INFO", Pattern.CASE_INSENSITIVE);
        Pattern debugP = Pattern.compile("\\bD/|DEBUG|VERBOSE|\\bV/", Pattern.CASE_INSENSITIVE);

        for (String line : lines) {
            if (errorP.matcher(line).find()) {
                errorCount++;
                if (keyIssues.length() < 300 && (line.contains("Exception") || line.contains("FATAL")))
                    keyIssues.append("• ").append(line.trim()).append("\n");
            } else if (warnP.matcher(line).find()) warnCount++;
            else if (infoP.matcher(line).find()) infoCount++;
            else if (debugP.matcher(line).find()) debugCount++;
        }

        StringBuilder result = new StringBuilder();
        result.append("=== RESULTADO DA ANÁLISE ===\n\n");
        result.append("Linhas totais: ").append(lines.length).append("\n");
        result.append("Erros: ").append(errorCount).append("\n");
        result.append("Avisos: ").append(warnCount).append("\n");
        result.append("Infos: ").append(infoCount).append("\n");
        result.append("Debugs: ").append(debugCount).append("\n\n");
        if (keyIssues.length() > 0) result.append("--- Principais Problemas ---\n").append(keyIssues.toString());
        
        return result.toString();
    }
}
