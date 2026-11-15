package com.example.nativelogreader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ErrorDatabase {
    // Lista que vai guardar os erros encontrados no log atual
    public static List<ErrorPattern> errosEncontrados = new ArrayList<>();

    public static List<ErrorPattern> getKnownPatterns() {
        List<ErrorPattern> patterns = new ArrayList<>();

        patterns.add(new ErrorPattern(
                "err_001", "NoSuchMethodException", "Erro de Função Inexistente", "CRITICO",
                "O aplicativo tentou usar um recurso do sistema que o seu celular não possui.",
                Arrays.asList("O app foi feito para uma versão mais nova do Android.", "Bug na fabricação do celular (Realme, Xiaomi)."),
                Arrays.asList("XDA: Erro NoSuchMethodException no Android", "StackOverflow: O que é NoSuchMethod?")
        ));

        patterns.add(new ErrorPattern(
                "err_002", "NullPointerException", "Variável Vazia (NullPointer)", "ALTO",
                "O aplicativo tentou puxar uma informação que estava vazia ou não existia.",
                Arrays.asList("Algum campo não foi preenchido.", "O app perdeu a conexão com os dados."),
                Arrays.asList("Google: NullPointerException Android", "YouTube: Como resolver NullPointer")
        ));

        patterns.add(new ErrorPattern(
                "err_003", "FATAL EXCEPTION", "Travamento Fatal", "CRITICO",
                "O aplicativo travou de vez e foi fechado à força pelo sistema.",
                Arrays.asList("Erro grave de programação.", "Arquivos do celular corrompidos."),
                Arrays.asList("Google: FATAL EXCEPTION Android", "Como limpar cache do Android")
        ));

        return patterns;
    }
}
