package com.example.nativelogreader;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ErrorDatabase {
    public static final List<ErrorPattern> errosEncontrados = new ArrayList<>();
    private static List<ErrorPattern> knownPatterns = getLocalFallbackPatterns();

    private static final String JSON_URL = "https://raw.githubusercontent.com/danivini14041702-svg/LogReaderNative/main/errors.json";

    public static List<ErrorPattern> getKnownPatterns() {
        return knownPatterns;
    }

    public static void sincronizarNuvem(Context context) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    URL url = new URL(JSON_URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    reader.close();
                    return result.toString();
                } catch (Exception e) {
                    Log.e("ERRO_SYNC", "Falha ao baixar JSON: " + e.getMessage());
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String jsonString) {
                if (jsonString != null) {
                    try {
                        JSONArray array = new JSONArray(jsonString);
                        List<ErrorPattern> novaLista = new ArrayList<>();
                        
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            JSONArray causasArray = obj.getJSONArray("causes");
                            List<String> causas = new ArrayList<>();
                            for (int c = 0; c < causasArray.length(); c++) causas.add(causasArray.getString(c));
                            
                            JSONArray sourcesArray = obj.getJSONArray("sources");
                            List<String> sources = new ArrayList<>();
                            for (int s = 0; s < sourcesArray.length(); s++) sources.add(sourcesArray.getString(s));
                            
                            novaLista.add(new ErrorPattern(
                                obj.getString("pattern"), obj.getString("title"), obj.getString("severity"),
                                obj.getString("description"), obj.getString("tip"), causas, sources
                            ));
                        }
                        
                        knownPatterns = novaLista; 
                        Toast.makeText(context, "Dicionário atualizado! " + novaLista.size() + " erros carregados.", Toast.LENGTH_LONG).show();
                        
                    } catch (Exception e) {
                        Log.e("ERRO_SYNC", "Erro ao ler JSON: " + e.getMessage());
                        Toast.makeText(context, "Erro ao ler o dicionário.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Sem internet ou link errado. Usando 3 erros locais.", Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    private static List<ErrorPattern> getLocalFallbackPatterns() {
        List<ErrorPattern> patterns = new ArrayList<>();
        patterns.add(new ErrorPattern("NullPointerException", "O app tentou usar algo que não existia", "CRITICO", "Imagine que o app pediu um copo, mas a mão dele estava vazia.", "Geralmente é bug do desenvolvedor.", Arrays.asList("Bug no app", "Dados corrompidos"), Arrays.asList("Pesquisar: NullPointerException Android", "StackOverflow: NullPointerException")));
        patterns.add(new ErrorPattern("NoSuchMethodException", "O app chamou uma função que não existe mais", "ALTO", "O app foi feito esperando uma função antiga que o Android removeu.", "Costuma acontecer depois de atualizar o Android.", Arrays.asList("App desatualizado"), Arrays.asList("Pesquisar: NoSuchMethodException Android", "StackOverflow: NoSuchMethodException")));
        patterns.add(new ErrorPattern("FATAL EXCEPTION", "O app travou completamente (crash fatal)", "CRITICO", "O erro foi tão grave que o Android preferiu fechar o app.", "Procure a linha abaixo para ver o erro real.", Arrays.asList("Erro grave no app"), Arrays.asList("Pesquisar: FATAL EXCEPTION Android", "StackOverflow: FATAL EXCEPTION")));
        return patterns;
    }
}
