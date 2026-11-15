package com.example.nativelogreader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ErrorDatabase {
    public static List<ErrorPattern> getKnownPatterns() {
        List<ErrorPattern> patterns = new ArrayList<>();

        patterns.add(new ErrorPattern(
                "NullPointerException",
                "NullPointerException",
                "O código tentou acessar um objeto que não existe (está 'nulo').",
                Arrays.asList("Variável não inicializada", "Objeto removido da memória", "Busca no banco retornou vazio"),
                Arrays.asList("Reinicie o aplicativo", "Verifique todos os campos", "Reporte se persistir")
        ));

        patterns.add(new ErrorPattern(
                "OutOfMemoryError",
                "OutOfMemoryError",
                "O aplicativo esgotou toda a memória RAM disponível.",
                Arrays.asList("Arquivos muito grandes", "Vários apps abertos", "Vazamento de memória (Memory Leak)"),
                Arrays.asList("Feche outros apps", "Reinicie o celular", "Use arquivos menores")
        ));

        patterns.add(new ErrorPattern(
                "FATAL EXCEPTION",
                "Fatal Exception",
                "O aplicativo travou completamente e foi fechado pelo sistema.",
                Arrays.asList("Bug grave no código", "Dados corrompidos", "Memória insuficiente"),
                Arrays.asList("Reinicie o aplicativo", "Atualize o app", "Reporte ao desenvolvedor")
        ));
        
        // NOVOS ERROS ADICIONADOS AQUI:
        patterns.add(new ErrorPattern(
                "NoSuchMethodException",
                "NoSuchMethodException",
                "O aplicativo tentou usar uma função ou método do sistema que não existe nesta versão do Android.",
                Arrays.asList("Versão do Android incompatível", "Biblioteca desatualizada", "Bug na customização do fabricante (ex: Realme/Oppo)"),
                Arrays.asList("Atualize o aplicativo", "Atualize o sistema do celular", "Reporte ao desenvolvedor")
        ));

        patterns.add(new ErrorPattern(
                "ActivityNotFoundException",
                "ActivityNotFoundException",
                "O app tentou abrir uma tela ou recurso que não está instalado no celular.",
                Arrays.asList("App necessário não instalado", "Link inválido", "Permissão negada"),
                Arrays.asList("Instale o aplicativo necessário", "Verifique as permissões")
        ));

        patterns.add(new ErrorPattern(
                "timeout",
                "Timeout (Tempo esgotado)",
                "O aplicativo tentou conectar na internet ou em um servidor, mas demorou muito e desistiu.",
                Arrays.asList("Internet lenta", "Servidor fora do ar", "Bloqueio de firewall"),
                Arrays.asList("Verifique sua internet", "Tente novamente mais tarde")
        ));

        return patterns;
    }
}
