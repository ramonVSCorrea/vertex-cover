import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Interface de terminal sem dependências. Execute a partir da raiz de vertex-cover/. */
public final class Main {
    private Main() { }

    public static void main(String[] args) {
        Locale.setDefault(Locale.ROOT);
        try {
            execute(args);
        } catch (IllegalArgumentException | IOException | IllegalStateException error) {
            ConsoleView.error(error.getMessage());
            System.err.println("  Dica: execute executar.cmd para usar o menu, ou java -cp out Main help.");
            System.exit(1);
        }
    }

    static void execute(String[] args) throws IOException {
        if (args.length == 0 || args[0].equals("menu")) {
            if (args.length > 1) {
                throw new IllegalArgumentException("O menu não recebe opções.");
            }
            BufferedReader reader = System.console() == null
                    ? new BufferedReader(new InputStreamReader(System.in))
                    : new BufferedReader(System.console().reader());
            new InteractiveMenu(reader, projectRoot()).run();
            return;
        }
        if (args[0].equals("help") || args[0].equals("--help")) {
            help();
            return;
        }
        Map<String, String> options = parse(args);
        switch (args[0]) {
            case "solve" -> solve(options);
            case "demo" -> {
                allow(options);
                demo();
            }
            case "compare" -> {
                allow(options, "input", "timeout-ms");
                Path input = Path.of(required(options, "input"));
                compare(GraphFileReader.read(input), input.toString(), timeout(options, 5000));
            }
            case "test" -> {
                allow(options);
                runTests();
            }
            case "experiment" -> experiment(options);
            case "generate" -> {
                allow(options, "output", "seed");
                Path output = Path.of(options.getOrDefault("output", "data/instances"));
                long seed = integer(options, "seed", GraphGenerator.DEFAULT_SEED);
                List<GraphGenerator.Instance> instances = GraphGenerator.writeSuite(output, seed);
                ConsoleView.section("INSTÂNCIAS GERADAS", "Grafos prontos para os experimentos.");
                ConsoleView.field("Total", instances.size() + " (200 aleatórias + 30 estruturadas)");
                ConsoleView.field("Semente base", seed);
                ConsoleView.file("Manifesto com sementes", output.resolve("manifest.csv"));
            }
            case "benchmark" -> {
                allow(options, "input", "output", "timeout-ms", "repetitions", "warmup");
                Path input = Path.of(options.getOrDefault("input", "data/instances"));
                Path output = Path.of(options.getOrDefault("output", "data/results/benchmark.csv"));
                long timeout = integer(options, "timeout-ms", 5000);
                int repetitions = positiveInt(options, "repetitions", 1, false);
                int warmup = positiveInt(options, "warmup", 100, true);
                if (timeout < 0 || timeout > Long.MAX_VALUE / 1_000_000L) {
                    throw new IllegalArgumentException("Timeout inválido.");
                }
                List<GraphGenerator.Instance> instances;
                if (!options.containsKey("input") && !Files.exists(input.resolve("manifest.csv"))) {
                    instances = GraphGenerator.writeSuite(input, GraphGenerator.DEFAULT_SEED);
                } else {
                    instances = GraphGenerator.readSuite(input);
                }
                BenchmarkRunner.run(instances, output, timeout, repetitions, warmup);
            }
            case "charts" -> {
                allow(options, "input", "output");
                ChartGenerator.generate(Path.of(options.getOrDefault("input", "data/results/benchmark.csv")),
                        Path.of(options.getOrDefault("output", "data/results/figures")));
            }
            case "report" -> {
                allow(options, "input", "output");
                ExperimentReport.generate(Path.of(options.getOrDefault("input", "data/results/benchmark.csv")),
                        Path.of(options.getOrDefault("output", "docs/experimental-results.md")));
            }
            default -> throw new IllegalArgumentException("Comando desconhecido: " + args[0]);
        }
    }

    private static void solve(Map<String, String> options) throws IOException {
        allow(options, "algorithm", "input", "timeout-ms");
        String algorithm = required(options, "algorithm");
        if (!algorithm.equals("exact") && !algorithm.equals("approximation")) {
            throw new IllegalArgumentException("Algoritmo deve ser exact ou approximation.");
        }
        long timeout = integer(options, "timeout-ms", 5000);
        if (timeout < 0 || timeout > Long.MAX_VALUE / 1_000_000L) {
            throw new IllegalArgumentException("Timeout inválido.");
        }
        Graph graph = GraphFileReader.read(Path.of(required(options, "input")));
        SolverResult result = algorithm.equals("exact")
                ? new ExactBranchAndBound().solve(graph, timeout)
                : new MatchingTwoApproximation().solve(graph);
        ConsoleView.graph(graph, options.get("input"));
        ConsoleView.solution(graph, result, algorithm.equals("exact") ? "BRANCH AND BOUND" : "MATCHING 2-APROXIMADO");
    }

    static void demo() {
        ConsoleView.section("DEMONSTRAÇÃO | DO PROBLEMA À SOLUÇÃO", "Um exemplo pequeno para entender sem configurar nada.");
        System.out.println("""
                  1. O grafo tem 5 pontos (vértices) e 6 ligações (arestas).
                  2. Precisamos escolher pontos para que cada ligação tenha uma ponta escolhida.
                  3. Queremos escolher o menor número possível de pontos.

                                    (1)
                                   /   \\
                                 (2)---(3)
                                  |     |
                                 (4)---(5)

                  Ligações: (1,2), (1,3), (2,3), (2,4), (3,5), (4,5).
                  Observe na tabela quais pontos cada algoritmo seleciona.
                """);
        Graph graph = new Graph(5);
        for (int[] endpoints : new int[][] {{0, 1}, {0, 2}, {1, 2}, {1, 3}, {2, 4}, {3, 4}}) {
            graph.addEdge(endpoints[0], endpoints[1]);
        }
        SolverResult exact = compare(graph, "Grafo de demonstração (equivalente a data/instances/exemplo.txt)", 5000);
        ConsoleView.section("COMO LER A RESPOSTA", "O número de vértices selecionados é o tamanho da cobertura.");
        if (exact.optimalityProven()) {
            ConsoleView.success("O exato comprovou o mínimo de " + exact.solution().size() + " vértices.");
        } else {
            ConsoleView.warning("O exato foi interrompido: a cobertura abaixo é válida, mas não comprova o mínimo.");
        }
        List<String[]> coveredEdges = new ArrayList<>();
        for (Edge edge : graph.edges()) {
            String endpoints = (exact.solution().contains(edge.u()) ? "" + (edge.u() + 1) : "")
                    + (exact.solution().contains(edge.u()) && exact.solution().contains(edge.v()) ? " e " : "")
                    + (exact.solution().contains(edge.v()) ? "" + (edge.v() + 1) : "");
            coveredEdges.add(new String[] {"(" + (edge.u() + 1) + ", " + (edge.v() + 1) + ")", endpoints});
        }
        ConsoleView.info("Confira por que a solução do exato cobre todas as ligações:");
        ConsoleView.table(new String[] {"Ligação", "Ponta(s) selecionada(s)"}, coveredEdges);
        System.out.println();
        System.out.println("""
                  A aproximação seleciona 4 vértices nesta ordem de arestas.
                  As duas soluções são válidas: toda ligação tem uma ponta selecionada.
                  Essa diferença é esperada; aproximação não significa solução inválida.

                  Próximo passo: opção 4 do menu para medir vários grafos e ver os gráficos.
                """);
    }

    static SolverResult compare(Graph graph, String name, long timeoutMs) {
        ConsoleView.graph(graph, name);
        SolverResult exact = new ExactBranchAndBound().solve(graph, timeoutMs);
        SolverResult approx = new MatchingTwoApproximation().solve(graph);
        ConsoleView.comparison(graph, exact, approx);
        return exact;
    }

    static void runTests() {
        // Evita uma dependência de compilação de src/ para tests/; os testes continuam independentes.
        try {
            Class.forName("TestRunner").getMethod("main", String[].class).invoke(null, (Object) new String[0]);
        } catch (ClassNotFoundException error) {
            throw new IllegalStateException("TestRunner não foi compilado. Abra executar.cmd ou compile src/ e tests/.", error);
        } catch (InvocationTargetException error) {
            throw new IllegalStateException("Existem testes reprovados; consulte o resumo acima.", error.getCause());
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("Não foi possível iniciar os testes.", error);
        }
    }

    private static void experiment(Map<String, String> options) throws IOException {
        allow(options, "mode", "output", "timeout-ms", "repetitions", "warmup");
        String mode = options.getOrDefault("mode", "quick");
        if (!mode.equals("quick") && !mode.equals("full")) {
            throw new IllegalArgumentException("--mode deve ser quick (8 grafos) ou full (230 grafos).");
        }
        boolean full = mode.equals("full");
        Path output = options.containsKey("output") ? Path.of(options.get("output"))
                : ExperimentWorkflow.newOutputPath(projectRoot().resolve("data/results/execucoes"), full ? "completo" : "rapido");
        List<GraphGenerator.Instance> instances = full ? GraphGenerator.defaultInstances(GraphGenerator.DEFAULT_SEED)
                : ExperimentWorkflow.quickInstances();
        ExperimentWorkflow.run(instances, output, timeout(options, full ? 5000 : 1000),
                positiveInt(options, "repetitions", 3, false), positiveInt(options, "warmup", full ? 100 : 20, true),
                full ? "Completo (230 grafos padrão)" : "Rápido e didático (8 grafos)");
    }

    private static long timeout(Map<String, String> options, long defaultValue) {
        long value = integer(options, "timeout-ms", defaultValue);
        if (value < 0 || value > Long.MAX_VALUE / 1_000_000L) {
            throw new IllegalArgumentException("Timeout inválido.");
        }
        return value;
    }

    static Path projectRoot() {
        Path current = Path.of("").toAbsolutePath().normalize();
        if (Files.isRegularFile(current.resolve("src/Main.java"))) {
            return current;
        }
        try {
            Path location = Path.of(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Path parent = location.getParent();
            if (parent != null && Files.isRegularFile(parent.resolve("src/Main.java"))) {
                return parent;
            }
        } catch (URISyntaxException | IllegalArgumentException ignored) {
            // O launcher já define a raiz; em uma IDE não convencional usa-se o diretório atual.
        }
        return current;
    }

    private static Map<String, String> parse(String[] args) {
        Map<String, String> options = new HashMap<>();
        for (int i = 1; i < args.length; i += 2) {
            if (!args[i].startsWith("--") || args[i].length() <= 2 || i + 1 >= args.length) {
                throw new IllegalArgumentException("Esperado --opção valor: " + args[i]);
            }
            String name = args[i].substring(2);
            if (options.putIfAbsent(name, args[i + 1]) != null) {
                throw new IllegalArgumentException("Opção repetida: --" + name);
            }
        }
        return options;
    }

    private static void allow(Map<String, String> options, String... names) {
        Set<String> allowed = Set.of(names);
        for (String option : options.keySet()) {
            if (!allowed.contains(option)) {
                throw new IllegalArgumentException("Opção desconhecida: --" + option);
            }
        }
    }

    private static String required(Map<String, String> options, String key) {
        String value = options.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Opção obrigatória: --" + key);
        }
        return value;
    }

    private static long integer(Map<String, String> options, String key, long defaultValue) {
        try {
            return options.containsKey(key) ? Long.parseLong(options.get(key)) : defaultValue;
        } catch (NumberFormatException error) {
            throw new IllegalArgumentException("--" + key + " deve ser um inteiro.", error);
        }
    }

    private static int positiveInt(Map<String, String> options, String key, int defaultValue, boolean allowZero) {
        long value = integer(options, key, defaultValue);
        if (value < (allowZero ? 0 : 1) || value > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("--" + key + " fora do intervalo.");
        }
        return (int) value;
    }

    private static void help() {
        ConsoleView.section("AJUDA | VERTEX COVER", "Início fácil: executar.cmd (Windows) ou java -cp out Main.");
        System.out.println("""
                SEM DECORAR ARGUMENTOS
                java -cp out Main                         Abre o menu interativo
                java -cp out Main demo                    Demonstração explicada
                java -cp out Main test                    Testes de corretude
                java -cp out Main compare --input arquivo.txt [--timeout-ms 5000]
                java -cp out Main experiment --mode quick 8 grafos + CSV + PNGs + relatório
                java -cp out Main experiment --mode full  230 grafos (pode demorar)
                     [--output pasta-nova] [--timeout-ms 5000] [--repetitions 3] [--warmup 100]

                COMANDOS INDIVIDUAIS (CONTINUAM DISPONÍVEIS)
                java -cp out Main solve --algorithm exact --input arquivo.txt [--timeout-ms 5000]
                java -cp out Main solve --algorithm approximation --input arquivo.txt
                java -cp out Main generate [--output data/instances/] [--seed 20261115]
                java -cp out Main benchmark [--input data/instances/] [--timeout-ms 5000]
                     [--output data/results/benchmark.csv] [--repetitions 1] [--warmup 100]
                java -cp out Main charts [--input data/results/benchmark.csv] [--output data/results/figures/]
                java -cp out Main report [--input data/results/benchmark.csv] [--output docs/experimental-results.md]

                Grafos usam vértices 1..n no arquivo; internamente 0..n-1.
                Benchmark lê manifest.csv; sem --input, gera o conjunto padrão se o manifesto não existir.
                Timeout 0 interrompe imediatamente grafos não vazios, mas preserva uma cobertura válida.
                """);
    }
}
