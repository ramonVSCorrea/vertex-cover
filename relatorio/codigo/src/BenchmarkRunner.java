import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public final class BenchmarkRunner {
    public static final List<String> HEADER = List.of(
            "instance_id", "family", "seed", "n", "m", "density", "p",
            "exact_status", "exact_runtime_ns", "exact_best_known_size",
            "exact_optimal_size", "exact_optimality_proven", "exact_nodes_explored",
            "exact_pruned_nodes", "approx_runtime_ns", "approx_solution_size",
            "approx_valid", "approximation_ratio", "gap_percent", "exact_valid",
            "repetitions", "exact_selected_repetition", "approx_selected_repetition");

    private record Measurement(int repetition, SolverResult exact, SolverResult approx) { }

    private BenchmarkRunner() { }

    public static void run(List<GraphGenerator.Instance> instances, Path output,
                           long timeoutMs, int repetitions, int warmupRuns) throws IOException {
        if (instances.isEmpty() || repetitions < 1 || warmupRuns < 0
                || timeoutMs < 0 || timeoutMs > Long.MAX_VALUE / 1_000_000L) {
            throw new IllegalArgumentException("Parâmetros inválidos de benchmark.");
        }
        Path parent = output.toAbsolutePath().getParent();
        Files.createDirectories(parent);
        ConsoleView.section("BENCHMARK | MEDIÇÃO DOS ALGORITMOS", "Cada linha aparece ao concluir as repetições de um grafo.");
        ConsoleView.field("Instâncias", instances.size());
        ConsoleView.field("Medições por algoritmo/grafo", repetitions);
        ConsoleView.field("Limite por medição exata", timeoutMs + " ms");
        ConsoleView.field("Aquecimentos separados", warmupRuns);
        ConsoleView.info("Aquecendo a JVM antes das medições oficiais...");
        long warmupStart = System.nanoTime();
        // Grafos e sementes separados dos experimentos oficiais.
        for (int i = 0; i < warmupRuns; i++) {
            Graph graph = GraphGenerator.random(8 + i % 3, 0.35, -100000L - i);
            new ExactBranchAndBound().solve(graph, 5000);
            new MatchingTwoApproximation().solve(graph);
        }
        long warmupNs = System.nanoTime() - warmupStart;
        List<List<?>> summary = new ArrayList<>();
        List<List<?>> raw = new ArrayList<>();
        int optimal = 0;
        int timeouts = 0;
        int rawTimeouts = 0;
        ConsoleView.info("Melhor = tamanho da melhor cobertura conhecida; TIMEOUT não comprova o mínimo.");
        ConsoleView.benchmarkHeader();
        long start = System.nanoTime();
        for (int instanceIndex = 0; instanceIndex < instances.size(); instanceIndex++) {
            GraphGenerator.Instance instance = instances.get(instanceIndex);
            List<Measurement> measurements = new ArrayList<>();
            for (int repetition = 1; repetition <= repetitions; repetition++) {
                Graph graph = instance.graph();
                SolverResult exact;
                SolverResult approx;
                // Alternância reduz viés de executar sempre o mesmo algoritmo primeiro.
                if ((instanceIndex + repetition) % 2 == 0) {
                    exact = new ExactBranchAndBound().solve(graph, timeoutMs);
                    approx = new MatchingTwoApproximation().solve(graph);
                } else {
                    approx = new MatchingTwoApproximation().solve(graph);
                    exact = new ExactBranchAndBound().solve(graph, timeoutMs);
                }
                validate(graph, exact, approx);
                Measurement measurement = new Measurement(repetition, exact, approx);
                measurements.add(measurement);
                raw.add(row(instance, exact, approx, 1, repetition, repetition));
                if (exact.status() == SolverResult.Status.TIME_LIMIT) {
                    rawTimeouts++;
                }
            }
            // Seleciona uma execução real na mediana superior, sem inventar estatísticas de nós/status.
            List<Measurement> exactSorted = new ArrayList<>(measurements);
            exactSorted.sort(Comparator.comparingLong(value -> value.exact().runtimeNs()));
            List<Measurement> approxSorted = new ArrayList<>(measurements);
            approxSorted.sort(Comparator.comparingLong(value -> value.approx().runtimeNs()));
            Measurement exactMedian = exactSorted.get(repetitions / 2);
            Measurement approxMedian = approxSorted.get(repetitions / 2);
            summary.add(row(instance, exactMedian.exact(), approxMedian.approx(), repetitions,
                    exactMedian.repetition(), approxMedian.repetition()));
            if (exactMedian.exact().optimalityProven()) {
                optimal++;
            } else {
                timeouts++;
            }
            ConsoleView.benchmarkRow(instanceIndex + 1, instances.size(), instance.id(),
                    exactMedian.exact(), approxMedian.approx());
        }
        CsvExporter.write(output, HEADER, summary);
        CsvExporter.write(sidecar(output, "-runs.csv"), HEADER, raw);
        long experimentNs = System.nanoTime() - start;
        String metadata = """
                # Ambiente e parâmetros da execução

                - Data/hora UTC: %s
                - SO: %s %s (%s)
                - Processadores lógicos disponíveis à JVM: %d
                - Processador informado pelo SO: %s
                - Memória física informada pelo ambiente: %s
                - Heap máximo da JVM (bytes): %d
                - Java: %s; fornecedor: %s; VM: %s
                - Comando Java: %s
                - Argumentos da JVM: %s
                - Compilação documentada: javac -encoding UTF-8 --release 17 -Xlint:all -d out src/*.java tests/*.java
                - Instâncias: %d
                - Repetições medidas por instância/algoritmo: %d
                - Timeout do exato por execução: %d ms
                - Aquecimento: %d pares de execuções, %.3f ms; n=8..10, p=0.35, seed=-100000-i
                - Duração experimental incluindo validação/exportação/progresso: %.3f s
                - Resumo: %d OPTIMAL; %d TIME_LIMIT
                - Execuções individuais interrompidas: %d de %d
                - Resumo temporal: execução na mediana superior por algoritmo (índice floor(r/2), base zero).
                - Status, tamanho, nós e podas pertencem à execução exata selecionada; consulte também todos os runs.
                - Geração/leitura de grafos, validação e escrita CSV fora dos tempos individuais.
                - Todas as sementes e probabilidades estão no CSV e no manifesto; NA significa não aplicável/desconhecido.
                - Tempos do exato incluem seu upper bound inicial por aproximação.
                - O limite é cooperativo: o retorno pode exceder ligeiramente o orçamento durante preparação/GC.
                """.formatted(Instant.now(), System.getProperty("os.name"), System.getProperty("os.version"),
                System.getProperty("os.arch"), Runtime.getRuntime().availableProcessors(),
                environment("PROCESSOR_IDENTIFIER"), environment("VERTEX_COVER_PHYSICAL_MEMORY"),
                Runtime.getRuntime().maxMemory(), System.getProperty("java.version"),
                System.getProperty("java.vendor"), System.getProperty("java.vm.name"),
                System.getProperty("sun.java.command"), ManagementFactory.getRuntimeMXBean().getInputArguments(),
                instances.size(), repetitions, timeoutMs, warmupRuns, warmupNs / 1_000_000.0,
                experimentNs / 1_000_000_000.0, optimal, timeouts, rawTimeouts, instances.size() * repetitions);
        Files.writeString(sidecar(output, "-environment.md"), metadata, StandardCharsets.UTF_8);
        ConsoleView.section("RESUMO DO BENCHMARK", "Os contadores do resumo usam a execução selecionada por mediana.");
        ConsoleView.field("Ótimos comprovados", optimal);
        ConsoleView.field("Timeouts no resumo", timeouts);
        ConsoleView.field("Timeouts em todas as medições", rawTimeouts + " / " + (instances.size() * repetitions));
        ConsoleView.field("Coberturas validadas", "Todas (exato e aproximação)");
        ConsoleView.field("Duração experimental", String.format(java.util.Locale.ROOT, "%.3f s", experimentNs / 1e9));
        ConsoleView.file("CSV resumido", output);
        ConsoleView.file("Todas as medições", sidecar(output, "-runs.csv"));
        ConsoleView.file("Ambiente e parâmetros", sidecar(output, "-environment.md"));
    }

    static List<?> row(GraphGenerator.Instance instance, SolverResult exact, SolverResult approx,
                       int repetitions, int exactRepetition, int approxRepetition) {
        boolean proven = exact.optimalityProven();
        int optimum = exact.solution().size();
        boolean ratioKnown = proven && optimum > 0;
        Graph graph = instance.graph();
        return Arrays.asList(instance.id(), instance.family(), instance.seed(), graph.vertexCount(),
                graph.edgeCount(), graph.density(), instance.p(), exact.status(), exact.runtimeNs(),
                optimum, proven ? optimum : null, proven, exact.nodesExplored(), exact.prunedNodes(),
                approx.runtimeNs(), approx.solution().size(), approx.solution().isValid(graph),
                ratioKnown ? (double) approx.solution().size() / optimum : null,
                ratioKnown ? 100.0 * (approx.solution().size() - optimum) / optimum : null,
                exact.solution().isValid(graph), repetitions, exactRepetition, approxRepetition);
    }

    private static void validate(Graph graph, SolverResult exact, SolverResult approx) {
        if (!exact.solution().isValid(graph) || !approx.solution().isValid(graph)) {
            throw new IllegalStateException("Algoritmo devolveu cobertura inválida.");
        }
        if (exact.optimalityProven() && (approx.solution().size() < exact.solution().size()
                || approx.solution().size() > 2 * exact.solution().size())) {
            throw new IllegalStateException("Inconsistência entre ótimo e aproximação.");
        }
        if (exact.optimalityProven() && exact.solution().size() == 0 && approx.solution().size() != 0) {
            throw new IllegalStateException("Ótimo zero exige aproximação vazia.");
        }
    }

    public static Path sidecar(Path csv, String suffix) {
        String name = csv.getFileName().toString();
        int dot = name.lastIndexOf('.');
        return csv.resolveSibling((dot >= 0 ? name.substring(0, dot) : name) + suffix);
    }

    private static String environment(String name) {
        return System.getenv().getOrDefault(name, "não disponível (consulte docs/experimental-methodology.md)");
    }
}
