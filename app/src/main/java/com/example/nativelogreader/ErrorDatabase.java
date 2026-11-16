package com.example.nativelogreader;

import android.os.AsyncTask;
import android.util.Log;
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
    // Lista global dos erros que bateram no log (usada pela ErrorDetailActivity)
    public static final List<ErrorPattern> errosEncontrados = new ArrayList<>();
    
    // A lista que o app usa para comparar (começa com a local)
    private static List<ErrorPattern> knownPatterns = getLocalFallbackPatterns();

    // Link direto para o arquivo no GitHub (A SUA URL AQUI)
    private static final String JSON_URL = "https://raw.githubusercontent.com/danivini14041702-svg/LogReaderNative/main/errors.json";

    public static List<ErrorPattern> getKnownPatterns() {
        return knownPatterns;
    }

    // Método para baixar o JSON em segundo plano
    public static void sincronizarNuvem() {
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
                    return null; // Retorna null se não tiver internet
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
                            
                            // Lendo as listas de causas e fontes
                            JSONArray causasArray = obj.getJSONArray("causes");
                            List<String> causas = new ArrayList<>();
                            for (int c = 0; c < causasArray.length(); c++) {
                                causas.add(causasArray.getString(c));
                            }
                            
                            JSONArray sourcesArray = obj.getJSONArray("sources");
                            List<String> sources = new ArrayList<>();
                            for (int s = 0; s < sourcesArray.length(); s++) {
                                sources.add(sourcesArray.getString(s));
                            }
                            
                            novaLista.add(new ErrorPattern(
                                obj.getString("pattern"),
                                obj.getString("title"),
                                obj.getString("severity"),
                                obj.getString("description"),
                                obj.getString("tip"),
                                causas,
                                sources
                            ));
                        }
                        
                        // Atualiza a lista em memória com a versão nova da nuvem!
                        knownPatterns = novaLista; 
                        Log.d("SYNC", "Dicionário atualizado! " + novaLista.size() + " erros carregados.");
                        
                    } catch (Exception e) {
                        Log.e("ERRO_SYNC", "Erro ao ler JSON: " + e.getMessage());
                    }
                }
            }
        }.execute();
    }

    // Fallback: se o celular estiver sem internet ou o JSON falhar, usa esses 3 erros básicos locais
    private static List<ErrorPattern> getLocalFallbackPatterns() {
        List<ErrorPattern> patterns = new ArrayList<>();
        patterns.add(new ErrorPattern(
            "NullPointerException", "O app tentou usar algo que não existia", "CRITICO",
            "Imagine que o app pediu um copo, mas a mão dele estava vazia.",
            "Geralmente é bug do desenvolvedor.",
            Arrays.asList("Bug no app", "Dados corrompidos"),
            Arrays.asList("Pesquisar: NullPointerException Android", "StackOverflow: NullPointerException")
        ));
        patterns.add(new ErrorPattern(
            "NoSuchMethodException", "O app chamou uma função que não existe mais", "ALTO",
            "O app foi feito esperando uma função antiga que o Android removeu.",
            "Costuma acontecer depois de atualizar o Android.",
            Arrays.asList("App desatualizado"),
            Arrays.asList("Pesquisar: NoSuchMethodException Android", "StackOverflow: NoSuchMethodException")
        ));
        patterns.add(new ErrorPattern(
            "FATAL EXCEPTION", "O app travou completamente (crash fatal)", "CRITICO",
            "O erro foi tão grave que o Android preferiu fechar o app.",
            "Procure a linha abaixo para ver o erro real.",
            Arrays.asList("Erro grave no app"),
            Arrays.asList("Pesquisar: FATAL EXCEPTION Android", "StackOverflow: FATAL EXCEPTION")
        ));
        return patterns;
    }
}
