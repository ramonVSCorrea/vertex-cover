import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.ToDoubleFunction;

/** Resumo textual reproduzível, calculado dos mesmos CSVs que alimentam as figuras. */
public final class ExperimentReport {
    private ExperimentReport() { }

    public static void generate(Path input, Path output) throws IOException {
        List<Map<String, String>> rows = CsvExporter.read(input);
        for (Map<String, String> row : rows) {
            ChartGenerator.validate(row);
        }
        long optimal = rows.stream().filter(ExperimentReport::proven).count();
        List<Map<String, String>> withRatio = rows.stream()
                .filter(row -> proven(row) && !missing(row.get("approximation_ratio"))).toList();
        long zero = rows.stream().filter(row -> proven(row) && value(row, "exact_optimal_size") == 0).count();
        StringBuilder text = new StringBuilder("""
                # Resultados experimentais medidos

                Documento gerado em Java diretamente do CSV real, sem números experimentais pré-preenchidos.
                Consulte `docs/experimental-methodology.md`, na raiz do projeto, para o protocolo completo.
                Os parâmetros e o ambiente desta execução estão no arquivo `*-environment.md` ao lado do CSV.

                ## Evidências

                """);
        text.append("- Fonte: `").append(input.toString().replace('\\', '/')).append("`\n");
        text.append("- SHA-256 do CSV: `").append(sha256(input)).append("`\n");
        text.append("- Instâncias no resumo: **").append(rows.size()).append("**.\n");
        text.append("- Execuções selecionadas: **").append(optimal).append(" OPTIMAL**, **")
                .append(rows.size() - optimal).append(" TIME_LIMIT**.\n");
        text.append("- Ótimos iguais a zero: **").append(zero).append("**; razão e gap são NA.\n");
        Path rawPath = BenchmarkRunner.sidecar(input, "-runs.csv");
        if (Files.exists(rawPath)) {
            List<Map<String, String>> raw = CsvExporter.read(rawPath);
            for (Map<String, String> row : raw) {
                ChartGenerator.validate(row);
            }
            long rawTimeouts = raw.stream().filter(row -> !proven(row)).count();
            text.append("- Medições individuais por algoritmo: **").append(raw.size())
                    .append("**; timeouts individuais do exato: **").append(rawTimeouts).append("**.\n");
            text.append("- CSV individual: `").append(rawPath.toString().replace('\\', '/'))
                    .append("`; SHA-256: `").append(sha256(rawPath)).append("`.\n");
        } else {
            text.append("- CSV individual não encontrado; contagens de repetições não inferidas.\n");
        }
        text.append("\nOs tempos abaixo são das execuções selecionadas pela mediana por instância. ")
                .append("Métricas de qualidade incluem apenas ótimo comprovado e positivo.\n");
        if (!withRatio.isEmpty()) {
            text.append("\n## Qualidade da aproximação\n\n");
            text.append("- Instâncias com razão definida: **").append(withRatio.size()).append("**.\n");
            text.append("- Razão mínima/média/máxima: **")
                    .append(decimal(min(withRatio, row -> value(row, "approximation_ratio")))).append(" / ")
                    .append(decimal(mean(withRatio, row -> value(row, "approximation_ratio")))).append(" / ")
                    .append(decimal(max(withRatio, row -> value(row, "approximation_ratio")))).append("**.\n");
            text.append("- Gap percentual médio/máximo: **")
                    .append(decimal(mean(withRatio, row -> value(row, "gap_percent")))).append("% / ")
                    .append(decimal(max(withRatio, row -> value(row, "gap_percent")))).append("%**.\n");
            text.append("- Razões acima de 2: **").append(withRatio.stream()
                    .filter(row -> value(row, "approximation_ratio") > 2 + 1e-9).count()).append("**.\n");
            text.append("- Coincidência de cardinalidade com o ótimo positivo: **").append(withRatio.stream()
                    .filter(row -> value(row, "approx_solution_size") == value(row, "exact_optimal_size")).count())
                    .append("** instâncias. Isso não transforma o algoritmo aproximado em algoritmo exato.\n");
        }
        text.append("""

                ## G(n,p), por configuração

                Tempos em ms: mediana superior **entre as instâncias** de cada configuração,
                depois da seleção por repetição. O tempo exato inclui eventuais observações censuradas
                por timeout: não é uma estimativa de tempo até o ótimo nessas observações.
                Razão média utiliza somente as linhas com prova e OPT>0; a coluna N da razão explicita sua amostra.

                | n | p | instâncias | OPTIMAL | TIME_LIMIT | mediana exato (ms) | mediana aprox (ms) | N razão | razão média |
                |---:|---:|---:|---:|---:|---:|---:|---:|---:|
                """);
        Map<String, List<Map<String, String>>> configurations = new LinkedHashMap<>();
        for (Map<String, String> row : rows) {
            if (row.get("family").equals("erdos_renyi")) {
                configurations.computeIfAbsent(row.get("n") + "|" + row.get("p"), ignored -> new ArrayList<>()).add(row);
            }
        }
        for (List<Map<String, String>> group : configurations.values()) {
            long solved = group.stream().filter(ExperimentReport::proven).count();
            List<Map<String, String>> ratios = group.stream()
                    .filter(row -> proven(row) && !missing(row.get("approximation_ratio"))).toList();
            text.append("| ").append(group.get(0).get("n")).append(" | ").append(group.get(0).get("p"))
                    .append(" | ").append(group.size()).append(" | ").append(solved).append(" | ")
                    .append(group.size() - solved).append(" | ")
                    .append(decimal(median(group, row -> value(row, "exact_runtime_ns")) / 1_000_000.0))
                    .append(" | ").append(decimal(median(group, row -> value(row, "approx_runtime_ns")) / 1_000_000.0))
                    .append(" | ").append(ratios.size()).append(" | ")
                    .append(ratios.isEmpty() ? "NA" : decimal(mean(ratios, row -> value(row, "approximation_ratio"))))
                    .append(" |\n");
        }
        text.append("""

                ## Instâncias estruturadas

                Ótimo abaixo significa certificado pelo solver nesta execução, não uma fórmula externa.
                Para TIME_LIMIT, o ótimo permanece NA mesmo em famílias cujo ótimo é conhecido teoricamente.

                | instância | status | melhor conhecida | ótimo comprovado | aprox | tempo exato (ms) | nós explorados |
                |---|---|---:|---:|---:|---:|---:|
                """);
        for (Map<String, String> row : rows) {
            if (!row.get("family").equals("erdos_renyi")) {
                text.append("| ").append(row.get("instance_id")).append(" | ").append(row.get("exact_status"))
                        .append(" | ").append(row.get("exact_best_known_size")).append(" | ")
                        .append(row.get("exact_optimal_size")).append(" | ").append(row.get("approx_solution_size"))
                        .append(" | ").append(decimal(value(row, "exact_runtime_ns") / 1_000_000.0))
                        .append(" | ").append(row.get("exact_nodes_explored")).append(" |\n");
            }
        }
        text.append("""

                ## Interrupções selecionadas no resumo

                As linhas seguintes não fornecem razão/gap nem certificado de ótimo.

                | instância | melhor conhecida | aprox | tempo parcial (ms) | nós parciais |
                |---|---:|---:|---:|---:|
                """);
        for (Map<String, String> row : rows) {
            if (!proven(row)) {
                text.append("| ").append(row.get("instance_id")).append(" | ")
                        .append(row.get("exact_best_known_size")).append(" | ").append(row.get("approx_solution_size"))
                        .append(" | ").append(decimal(value(row, "exact_runtime_ns") / 1_000_000.0))
                        .append(" | ").append(row.get("exact_nodes_explored")).append(" |\n");
            }
        }
        text.append("""

                ## Interpretação e limites

                - Os testes independentes verificam corretude em grafos pequenos; um benchmark não substitui a prova.
                - O matching aproxima rapidamente, mas pode selecionar até o dobro dos vértices necessários.
                  Há famílias estruturadas em que o fator 2 é atingido; não se promete que a aproximação seja sempre ótima.
                - A busca exata depende da estrutura, não apenas de n ou p. O matching inferior pode ser fraco em completos.
                - TIME_LIMIT é uma observação parcial, não uma solução ótima nem um tempo completo de resolução.
                - Não se deve generalizar as médias de razão às instâncias sem ótimo comprovado.
                - Tempos muito pequenos estão sujeitos à resolução do relógio/JIT/GC. Os resultados são desta máquina/execução.
                - Os seis PNGs ficam em `figures/` na execução guiada, ou `data/results/figures/` no benchmark padrão.
                  As figuras distinguem medições de referências teóricas.

                """);
        text.append("Regenerar este relatório (a partir da raiz do projeto):\n\n```text\n")
                .append("java -cp out Main report --input \"").append(input)
                .append("\" --output \"").append(output).append("\"\n```\n");
        Files.createDirectories(output.toAbsolutePath().getParent());
        Files.writeString(output, text.toString(), StandardCharsets.UTF_8);
        ConsoleView.success("Relatório dos dados reais gerado.");
        ConsoleView.file("Leia as tabelas e explicações", output);
    }

    private static boolean proven(Map<String, String> row) {
        return row.get("exact_status").equals("OPTIMAL");
    }

    private static boolean missing(String value) {
        return value == null || value.isBlank() || value.equals("NA") || value.equals("N/A");
    }

    private static double value(Map<String, String> row, String field) {
        return Double.parseDouble(row.get(field));
    }

    private static double mean(List<Map<String, String>> rows, ToDoubleFunction<Map<String, String>> metric) {
        return rows.stream().mapToDouble(metric).average().orElseThrow();
    }

    private static double min(List<Map<String, String>> rows, ToDoubleFunction<Map<String, String>> metric) {
        return rows.stream().mapToDouble(metric).min().orElseThrow();
    }

    private static double max(List<Map<String, String>> rows, ToDoubleFunction<Map<String, String>> metric) {
        return rows.stream().mapToDouble(metric).max().orElseThrow();
    }

    private static double median(List<Map<String, String>> rows, ToDoubleFunction<Map<String, String>> metric) {
        return metric.applyAsDouble(rows.stream().sorted(Comparator.comparingDouble(metric))
                .toList().get(rows.size() / 2));
    }

    private static String decimal(double value) {
        return String.format(Locale.ROOT, "%.6f", value);
    }

    private static String sha256(Path input) throws IOException {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(input)));
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException("SHA-256 indisponível na JVM.", error);
        }
    }
}
