import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/** Apresentação sem ANSI ou bibliotecas externas; funciona também em saída redirecionada. */
public final class ConsoleView {
    private static final String RULE = "=".repeat(104);

    private ConsoleView() { }

    public static void section(String title, String subtitle) {
        System.out.println();
        System.out.println("  " + RULE);
        System.out.println("  " + title);
        if (!subtitle.isBlank()) {
            System.out.println("  " + subtitle);
        }
        System.out.println("  " + RULE);
    }

    public static void field(String label, Object value) {
        System.out.printf("  %-29s : %s%n", label, value);
    }

    public static void info(String message) {
        System.out.println("  [INFO] " + message);
    }

    public static void success(String message) {
        System.out.println("  [ OK ] " + message);
    }

    public static void warning(String message) {
        System.out.println("  [AVISO] " + message);
    }

    public static void error(String message) {
        PrintStream output = System.err;
        output.println();
        output.println("  " + "-".repeat(104));
        output.println("  [ERRO] " + message);
        output.println("  Nenhum resultado dessa operação deve ser considerado concluído.");
        output.println("  " + "-".repeat(104));
    }

    public static void file(String label, Path path) {
        System.out.println("  " + label + ":");
        System.out.println("    " + path.toAbsolutePath().normalize());
    }

    public static void graph(Graph graph, String name) {
        section("GRAFO EM ANÁLISE", name);
        field("Vértices (pontos)", graph.vertexCount());
        field("Arestas (ligações)", graph.edgeCount());
        field("Densidade", String.format(Locale.ROOT, "%.4f", graph.density()));
        field("Vértices isolados", graph.isolatedVertices().size());
    }

    public static void solution(Graph graph, SolverResult result, String algorithm) {
        section("RESULTADO | " + algorithm, "Os identificadores dos vértices começam em 1.");
        field("Status", status(result));
        field("Cobertura válida", yesNo(result.solution().isValid(graph)));
        field("Otimalidade comprovada", yesNo(result.optimalityProven()));
        field(result.optimalityProven() ? "Quantidade mínima" : "Quantidade selecionada", result.solution().size());
        field("Tempo", time(result.runtimeNs()) + " (" + result.runtimeNs() + " ns)");
        if (result.status() != SolverResult.Status.APPROXIMATE) {
            field("Nós explorados", result.nodesExplored());
            field("Ramos podados", result.prunedNodes());
        }
        System.out.println();
        System.out.println("  Vértices selecionados: " + result.solution());
        explain(result);
    }

    public static void comparison(Graph graph, SolverResult exact, SolverResult approx) {
        section("COMPARAÇÃO DOS ALGORITMOS", "Mesma instância; tempos individuais, sem geração do grafo.");
        table(new String[] {"Métrica", "Branch and Bound", "Matching 2-aproximado"}, List.of(
                new String[] {"Status", exact.status().toString(), approx.status().toString()},
                new String[] {"Vértices selecionados", "" + exact.solution().size(), "" + approx.solution().size()},
                new String[] {"Cobertura válida", yesNo(exact.solution().isValid(graph)), yesNo(approx.solution().isValid(graph))},
                new String[] {"Ótimo comprovado", yesNo(exact.optimalityProven()), "Não (algoritmo aproximado)"},
                new String[] {"Tempo", time(exact.runtimeNs()), time(approx.runtimeNs())},
                new String[] {"Nós explorados", "" + exact.nodesExplored(), "-"},
                new String[] {"Ramos podados", "" + exact.prunedNodes(), "-"}));
        System.out.println();
        field("Cobertura do exato", exact.solution());
        field("Cobertura aproximada", approx.solution());
        if (exact.optimalityProven() && exact.solution().size() > 0) {
            double ratio = (double) approx.solution().size() / exact.solution().size();
            field("Razão aprox / ótimo", String.format(Locale.ROOT, "%.3f (garantia: no máximo 2)", ratio));
            field("Gap da aproximação", String.format(Locale.ROOT, "%.2f%%", (ratio - 1) * 100));
            System.out.println();
            info("Neste grafo, o mínimo comprovado é " + exact.solution().size()
                    + "; a aproximação selecionou " + approx.solution().size() + " vértices.");
        } else if (exact.optimalityProven()) {
            info("Não há arestas: as duas coberturas são vazias. Razão e gap: N/A.");
        } else {
            warning("Razão e gap: N/A, pois o ótimo não foi comprovado.");
        }
        explain(exact);
        info("Uma execução isolada serve à demonstração, não substitui o benchmark com aquecimento.");
    }

    public static void table(String[] header, List<String[]> rows) {
        int[] widths = new int[header.length];
        for (int i = 0; i < widths.length; i++) {
            widths[i] = header[i].length();
        }
        for (String[] row : rows) {
            if (row.length != widths.length) {
                throw new IllegalArgumentException("Tabela com número incorreto de colunas.");
            }
            for (int i = 0; i < widths.length; i++) {
                widths[i] = Math.max(widths[i], row[i].length());
            }
        }
        tableRow(header, widths);
        String[] separators = new String[widths.length];
        for (int i = 0; i < widths.length; i++) {
            separators[i] = "-".repeat(widths[i]);
        }
        tableRow(separators, widths);
        for (String[] row : rows) {
            tableRow(row, widths);
        }
    }

    public static void benchmarkHeader() {
        System.out.println();
        System.out.printf("  %-9s %-29s %-10s %7s %7s %14s %14s%n",
                "Progresso", "Instância", "Exato", "Melhor", "Aprox.", "Exato (ms)", "Aprox. (ms)");
        System.out.println("  " + "-".repeat(104));
    }

    public static void benchmarkRow(int current, int total, String id, SolverResult exact, SolverResult approx) {
        String displayId = id.length() > 29 ? id.substring(0, 26) + "..." : id;
        System.out.printf(Locale.ROOT, "  %-9s %-29s %-10s %7d %7d %14.6f %14.6f%n",
                current + "/" + total, displayId, exact.optimalityProven() ? "ÓTIMO" : "TIMEOUT",
                exact.solution().size(), approx.solution().size(),
                exact.runtimeNs() / 1_000_000.0, approx.runtimeNs() / 1_000_000.0);
    }

    public static String time(long ns) {
        return String.format(Locale.ROOT, "%.6f ms", ns / 1_000_000.0);
    }

    private static void tableRow(String[] cells, int[] widths) {
        System.out.print("  ");
        for (int i = 0; i < cells.length; i++) {
            System.out.printf("%-" + widths[i] + "s", cells[i]);
            if (i + 1 < cells.length) {
                System.out.print(" | ");
            }
        }
        System.out.println();
    }

    private static String yesNo(boolean value) {
        return value ? "Sim" : "Não";
    }

    private static String status(SolverResult result) {
        return switch (result.status()) {
            case OPTIMAL -> "OPTIMAL - mínimo comprovado";
            case TIME_LIMIT -> "TIME_LIMIT - tempo limite atingido";
            case APPROXIMATE -> "APPROXIMATE - cobertura com garantia de fator 2";
        };
    }

    private static void explain(SolverResult result) {
        System.out.println();
        switch (result.status()) {
            case OPTIMAL -> success("O algoritmo provou que não existe cobertura menor que a encontrada.");
            case TIME_LIMIT -> warning("Melhor cobertura conhecida: válida, mas NÃO necessariamente ótima.");
            case APPROXIMATE -> info("Aproximação: cobre todas as arestas, mas pode usar até o dobro do ótimo.");
        }
    }
}
