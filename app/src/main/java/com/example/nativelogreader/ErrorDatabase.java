package com.example.nativelogreader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ErrorDatabase {
    // Lista global dos erros que bateram no log (usada pela ErrorDetailActivity)
    public static final List<ErrorPattern> errosEncontrados = new ArrayList<>();

    public static List<ErrorPattern> getKnownPatterns() {
        List<ErrorPattern> patterns = new ArrayList<>();

        patterns.add(new ErrorPattern(
            "NullPointerException",
            "O app tentou usar algo que não existia",
            "CRITICO",
            "Imagine que o app pediu um copo, mas a mão dele estava vazia. Em termos técnicos, ele tentou acessar uma informação que nunca foi criada ou já foi apagada da memória.",
            "Geralmente é bug do desenvolvedor do app, não do seu celular.",
            Arrays.asList(
                "Bug no aplicativo (variável não inicializada)",
                "Dados corrompidos no cache do app",
                "Versão incompatível com o Android"
            ),
            Arrays.asList(
                "Pesquisar: NullPointerException Android causas",
                "StackOverflow: NullPointerException",
                "XDA Developers: app crash NullPointer"
            )
        ));

        patterns.add(new ErrorPattern(
            "NoSuchMethodException",
            "O app chamou uma função que não existe mais",
            "ALTO",
            "É como procurar uma ferramenta na caixa e ela ter sumido. O app foi feito esperando uma função antiga, mas o sistema Android atualizou e removeu essa função.",
            "Costuma acontecer depois de atualizar o Android ou o app.",
            Arrays.asList(
                "App desatualizado para a versão do Android",
                "Biblioteca interna do app ficou incompatível",
                "Instalação corrompida"
            ),
            Arrays.asList(
                "Pesquisar: NoSuchMethodException Android atualização",
                "StackOverflow: NoSuchMethodException Android",
                "XDA: app quebra após atualizar Android"
            )
        ));

        patterns.add(new ErrorPattern(
            "FATAL EXCEPTION",
            "O app travou completamente (crash fatal)",
            "CRITICO",
            "Essa é a linha vermelha que aparece quando o app fecha do nada. Significa que o erro foi tão grave que o Android preferiu fechar o app para não travar o celular inteiro.",
            "Procure a linha logo abaixo — é lá que está o erro real.",
            Arrays.asList(
                "Qualquer erro grave (NullPointer, memória, etc)",
                "Memória RAM insuficiente no momento",
                "Conflito com outro app aberto"
            ),
            Arrays.asList(
                "Pesquisar: como ler FATAL EXCEPTION Android",
                "StackOverflow: entender logcat FATAL EXCEPTION",
                "Guia XDA: como interpretar crash log"
            )
        ));

        return patterns;
    }
}
