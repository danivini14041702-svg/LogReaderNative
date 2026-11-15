package com.example.nativelogreader;

import java.util.List;

public class ErrorPattern {
    public final String pattern;       // trecho técnico buscado no log (ex: "NullPointerException")
    public final String title;         // nome curto em humanês (ex: "App tentou usar algo que não existia")
    public final String severity;      // "CRITICO" ou "ALTO"
    public final String description;   // explicação sem jargão
    public final String tip;           // dica rápida (1 linha)
    public final List<String> causes;  // causas prováveis
    public final List<String> sources; // links de pesquisa

    public ErrorPattern(String pattern, String title, String severity,
                        String description, String tip,
                        List<String> causes, List<String> sources) {
        this.pattern = pattern;
        this.title = title;
        this.severity = severity;
        this.description = description;
        this.tip = tip;
        this.causes = causes;
        this.sources = sources;
    }
}
