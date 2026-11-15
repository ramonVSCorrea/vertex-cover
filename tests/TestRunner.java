import java.util.BitSet;
import java.util.Random;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.Locale;

/** Testes independentes de bibliotecas externas; qualquer falha resulta em saída não zero. */
public final class TestRunner {
    private static int passed;
    private static int failed;

    public static void main(String[] args) throws Exception {
        passed = 0;
        failed = 0;
        ConsoleView.section("TESTES DE CORRETUDE", "Sem JUnit. Os detalhes de integração só aparecem se houver uma falha.");
        run("modelo, graus, densidade e isolados", TestRunner::testGraph);
        run("validação de arestas e índices", TestRunner::testInvalidEdges);
        run("imutabilidade e validade das soluções", TestRunner::testSolutions);
        run("aproximação e matching maximal", TestRunner::testMatching);
        run("exato e aproximação contra força bruta independente", TestRunner::testBruteForce);
        run("timeout preserva cobertura sem declarar ótimo", TestRunner::testTimeout);
        run("ótimos de todas as famílias estruturadas", TestRunner::testStructured);
        run("geradores e reprodutibilidade de G(n,p)", TestRunner::testGenerators);
        run("leitura, escrita e rejeição de arquivos inválidos", TestRunner::testGraphFiles);
        run("CSV: escaping e campos ausentes", TestRunner::testCsv);
        run("200 instâncias aleatórias + 30 estruturadas e manifesto", TestRunner::testManifest);
        run("benchmark real, repetições, zero e timeout no CSV", TestRunner::testBenchmark);
        run("seis PNGs de CSV real e tratamento de NA/timeout", TestRunner::testCharts);
        run("interface e rejeição de argumentos inválidos", TestRunner::testCli);
        run("menu guiado, demonstração e comparação com timeout", TestRunner::testGuidedUi);
        run("experimento rápido completo sem sobrescrever dados", TestRunner::testWorkflow);
        ConsoleView.section("RESUMO DOS TESTES", failed == 0 ? "Todas as verificações passaram." : "Há falhas que precisam ser corrigidas.");
        ConsoleView.field("Grupos aprovados", passed);
        ConsoleView.field("Grupos reprovados", failed);
        ConsoleView.info("Os arquivos temporários dos testes não alteram os experimentos salvos.");
        if (failed != 0) {
            throw new AssertionError("Existem testes reprovados.");
        }
    }

    private static void testGraph() {
        Graph graph = new Graph(4);
        graph.addEdge(1, 0);
        graph.addEdge(1, 2);
        check(graph.edges().get(0).equals(new Edge(0, 1)), "normalização");
        check(graph.edgeCount() == 2 && graph.vertexCount() == 4, "contagens");
        check(graph.degree(1) == 2 && graph.neighbors(0).equals(java.util.List.of(1)), "adjacência");
        check(Math.abs(graph.density() - 1.0 / 3.0) < 1e-12, "densidade");
        check(graph.isolatedVertices().equals(java.util.List.of(3)), "isolados");
        check(new Graph(0).density() == 0 && new Graph(1).density() == 0, "densidade n < 2");
        expect(UnsupportedOperationException.class, () -> graph.edges().clear());
        expect(UnsupportedOperationException.class, () -> graph.neighbors(0).clear());
    }

    private static void testInvalidEdges() {
        expect(IllegalArgumentException.class, () -> new Graph(-1));
        expect(IllegalArgumentException.class, () -> new Edge(2, 2));
        expect(IllegalArgumentException.class, () -> new Edge(-1, 2));
        Graph graph = new Graph(3);
        expect(IllegalArgumentException.class, () -> graph.addEdge(0, 0));
        expect(IllegalArgumentException.class, () -> graph.addEdge(-1, 2));
        expect(IllegalArgumentException.class, () -> graph.addEdge(0, 3));
        expect(IllegalArgumentException.class, () -> graph.degree(3));
        graph.addEdge(0, 1);
        expect(IllegalArgumentException.class, () -> graph.addEdge(1, 0));
        check(graph.edgeCount() == 1 && graph.degree(0) == 1, "rejeição não altera grafo");
    }

    private static void testSolutions() {
        Graph graph = new Graph(4);
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        BitSet bits = new BitSet();
        bits.set(1);
        VertexCoverSolution solution = new VertexCoverSolution(bits);
        bits.clear();
        solution.vertices().clear();
        check(solution.size() == 1 && solution.contains(1), "cópias defensivas");
        check(solution.isValid(graph) && solution.toString().equals("{2}"), "solução válida");
        check(!VertexCoverSolution.of(0).isValid(graph), "aresta descoberta");
        check(!VertexCoverSolution.of(1, 4).isValid(graph), "vértice inexistente");
        check(VertexCoverSolution.of().isValid(new Graph(0)), "grafo vazio");
    }

    private static void testMatching() {
        Graph graph = new Graph(8);
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        graph.addEdge(3, 4);
        graph.addEdge(4, 5);
        verifyMatching(graph);
        verifyMatching(new Graph(0));
        verifyMatching(new Graph(10));
    }

    private static void verifyMatching(Graph graph) {
        MatchingTwoApproximation.Result result = new MatchingTwoApproximation().solveWithMatching(graph);
        BitSet used = new BitSet();
        for (Edge edge : result.matching()) {
            check(graph.edges().contains(edge), "aresta do matching pertence ao grafo");
            check(!used.get(edge.u()) && !used.get(edge.v()), "matching sem extremos repetidos");
            used.set(edge.u());
            used.set(edge.v());
        }
        for (Edge edge : graph.edges()) {
            check(used.get(edge.u()) || used.get(edge.v()), "matching maximal");
        }
        check(used.equals(result.solverResult().solution().vertices()), "cobertura = extremos do matching");
        check(result.solverResult().solution().isValid(graph), "aproximação válida");
        check(result.solverResult().solution().size() == 2 * result.matching().size(), "tamanho 2M");
    }

    private static void testBruteForce() {
        // Todas as 1.024 estruturas de grafos rotulados com cinco vértices.
        for (int mask = 0; mask < (1 << 10); mask++) {
            Graph graph = new Graph(5);
            int position = 0;
            for (int u = 0; u < 5; u++) {
                for (int v = u + 1; v < 5; v++) {
                    if ((mask & (1 << position)) != 0) {
                        graph.addEdge(u, v);
                    }
                    position++;
                }
            }
            compareWithBruteForce(graph);
        }
        Random random = new Random(77123L);
        for (int n = 0; n <= 16; n++) {
            for (int repetition = 0; repetition < 5; repetition++) {
                Graph graph = new Graph(n);
                double p = repetition * 0.23;
                for (int u = 0; u < n; u++) {
                    for (int v = u + 1; v < n; v++) {
                        if (random.nextDouble() < p) {
                            graph.addEdge(u, v);
                        }
                    }
                }
                compareWithBruteForce(graph);
            }
        }
    }

    private static void compareWithBruteForce(Graph graph) {
        int optimum = bruteForce(graph);
        SolverResult exact = new ExactBranchAndBound().solve(graph, 5000);
        SolverResult approx = new MatchingTwoApproximation().solve(graph);
        check(exact.status() == SolverResult.Status.OPTIMAL && exact.optimalityProven(), "prova do ótimo");
        check(exact.solution().isValid(graph) && exact.solution().size() == optimum, "exato = força bruta");
        check(approx.solution().isValid(graph) && approx.solution().size() <= 2 * optimum, "garantia fator 2");
        verifyMatching(graph);
    }

    /** Enumera subconjuntos sem reutilizar podas, matching ou código do solucionador exato. */
    private static int bruteForce(Graph graph) {
        if (graph.vertexCount() > 20) {
            throw new IllegalArgumentException("Força bruta limitada a 20 vértices.");
        }
        int best = graph.vertexCount();
        for (long subset = 0; subset < (1L << graph.vertexCount()); subset++) {
            int size = Long.bitCount(subset);
            if (size >= best) {
                continue;
            }
            boolean valid = true;
            for (Edge edge : graph.edges()) {
                if ((subset & ((1L << edge.u()) | (1L << edge.v()))) == 0) {
                    valid = false;
                    break;
                }
            }
            if (valid) {
                best = size;
            }
        }
        return best;
    }

    private static void testTimeout() {
        Graph graph = new Graph(4);
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        ExactBranchAndBound solver = new ExactBranchAndBound();
        SolverResult result = solver.solve(graph, 0);
        check(result.status() == SolverResult.Status.TIME_LIMIT && !result.optimalityProven(), "status timeout");
        check(result.solution().isValid(graph), "incumbente factível no timeout");
        check(result.runtimeNs() >= 0, "tempo medido");
        SolverResult positiveTimeout = solver.solve(GraphGenerator.complete(45), 5);
        check(positiveTimeout.status() == SolverResult.Status.TIME_LIMIT
                && !positiveTimeout.optimalityProven() && positiveTimeout.solution().isValid(GraphGenerator.complete(45)),
                "timeout positivo durante busca difícil");
        check(solver.solve(graph, 5000).solution().size() == 1, "reutilização sem estado residual");
        check(solver.solve(new Graph(0), 0).optimalityProven(), "ótimo trivial demonstrável sem busca");
        expect(IllegalArgumentException.class, () -> solver.solve(graph, -1));
        expect(IllegalArgumentException.class, () -> solver.solve(graph, Long.MAX_VALUE));
    }

    private static void testStructured() {
        for (int n = 0; n <= 12; n++) {
            knownOptimum(GraphGenerator.empty(n), 0);
            knownOptimum(GraphGenerator.path(n), n / 2);
            knownOptimum(GraphGenerator.complete(n), Math.max(0, n - 1));
            if (n >= 3) {
                knownOptimum(GraphGenerator.cycle(n), (n + 1) / 2);
            }
            knownOptimum(GraphGenerator.star(n), n == 0 ? 0 : 1);
        }
        for (int a = 0; a <= 6; a++) {
            for (int b = 0; b <= 6; b++) {
                knownOptimum(GraphGenerator.completeBipartite(a, b), Math.min(a, b));
            }
        }
        Graph disconnected = new Graph(10);
        disconnected.addEdge(0, 1);
        disconnected.addEdge(1, 2);
        disconnected.addEdge(3, 4);
        disconnected.addEdge(4, 5);
        disconnected.addEdge(3, 5);
        // P3 + K3 + quatro isolados.
        knownOptimum(disconnected, 3);
        compareWithBruteForce(GraphGenerator.path(20));
    }

    private static void knownOptimum(Graph graph, int optimum) {
        SolverResult result = new ExactBranchAndBound().solve(graph, 5000);
        check(result.optimalityProven() && result.solution().size() == optimum, "ótimo estruturado");
        check(result.solution().isValid(graph), "cobertura estruturada");
        check(new MatchingTwoApproximation().solve(graph).solution().size() <= 2 * optimum, "2OPT estruturado");
        verifyMatching(graph);
    }

    private static void testGenerators() {
        Graph first = GraphGenerator.random(20, 0.3, 123L);
        Graph second = GraphGenerator.random(20, 0.3, 123L);
        check(first.edges().equals(second.edges()), "semente fixa");
        check(GraphGenerator.random(8, 0, 1).edgeCount() == 0, "p=0");
        check(GraphGenerator.random(8, 1, 1).edgeCount() == 28, "p=1");
        check(GraphGenerator.star(5).vertexCount() == 6, "estrela r+1");
        expect(IllegalArgumentException.class, () -> GraphGenerator.random(2, Double.NaN, 1));
        expect(IllegalArgumentException.class, () -> GraphGenerator.random(2, Double.POSITIVE_INFINITY, 1));
        expect(IllegalArgumentException.class, () -> GraphGenerator.random(2, -0.1, 1));
        expect(IllegalArgumentException.class, () -> GraphGenerator.random(2, 1.1, 1));
        expect(IllegalArgumentException.class, () -> GraphGenerator.cycle(2));
        expect(IllegalArgumentException.class, () -> GraphGenerator.star(-1));
        expect(IllegalArgumentException.class, () -> GraphGenerator.completeBipartite(-1, 2));
        expect(IllegalArgumentException.class, () -> GraphGenerator.defaultInstances(Long.MAX_VALUE));
    }

    private static void testGraphFiles() throws Exception {
        Path directory = temporaryDirectory();
        Path file = directory.resolve("grafo.txt");
        Graph original = GraphGenerator.random(10, 0.4, 9);
        GraphFileReader.write(original, file);
        Graph restored = GraphFileReader.read(file);
        check(restored.vertexCount() == original.vertexCount()
                && restored.edges().equals(original.edges()), "round-trip grafo");
        for (String invalid : List.of("", "-1 0\n", "3 -1\n", "2 2\n1 2\n1 2\n",
                "3 2\n1 2\n", "3 0\n1 2\n", "3 1\n2 2\n", "3 1\n0 2\n",
                "3 1\n1 4\n", "3 2\n1 2\n2 1\n", "3 1 extra\n1 2\n", "3 a\n",
                "3 1\n-2147483648 2\n", "3 1\n1 2 3\n")) {
            Files.writeString(file, invalid, StandardCharsets.UTF_8);
            expect(IOException.class, () -> GraphFileReader.read(file));
        }
        Files.writeString(file, "\uFEFF3 1\n\n 1   2 \n\n", StandardCharsets.UTF_8);
        check(GraphFileReader.read(file).edges().equals(List.of(new Edge(0, 1))), "BOM e espaços");
        GraphFileReader.write(new Graph(0), file);
        check(GraphFileReader.read(file).vertexCount() == 0, "grafo vazio em arquivo");
    }

    private static void testCsv() throws Exception {
        Path file = temporaryDirectory().resolve("csv.csv");
        CsvExporter.write(file, List.of("id", "text", "missing"), List.of(
                java.util.Arrays.asList("a,b", "linha \"citada\"\nseguinte", null),
                List.of("x", "", "")));
        List<Map<String, String>> rows = CsvExporter.read(file);
        check(rows.size() == 2 && rows.get(0).get("id").equals("a,b"), "vírgula escapada");
        check(rows.get(0).get("text").equals("linha \"citada\"\nseguinte"), "aspas e quebra de linha");
        check(rows.get(0).get("missing").equals("NA") && rows.get(1).get("missing").isEmpty(), "NA/vazio");
        for (String invalid : List.of("", "a,a\n1,2\n", "a,b\n1\n", "a,b\n\"ab,cd\n", "a,b\n\"x\"y,z\n")) {
            Files.writeString(file, invalid, StandardCharsets.UTF_8);
            expect(IOException.class, () -> CsvExporter.read(file));
        }
    }

    private static void testManifest() throws Exception {
        Path directory = temporaryDirectory();
        List<GraphGenerator.Instance> originals = GraphGenerator.writeSuite(directory, GraphGenerator.DEFAULT_SEED);
        List<GraphGenerator.Instance> restored = GraphGenerator.readSuite(directory);
        check(restored.size() == 230, "total 230 instâncias");
        check(restored.stream().filter(value -> value.family().equals("erdos_renyi")).count() == 200, "200 G(n,p)");
        for (int i = 0; i < restored.size(); i++) {
            GraphGenerator.Instance first = originals.get(i);
            GraphGenerator.Instance second = restored.get(i);
            check(first.id().equals(second.id()) && first.graph().edges().equals(second.graph().edges()), "manifesto");
            if (i < 200) {
                check(second.seed() == GraphGenerator.DEFAULT_SEED + i, "sementes consecutivas");
                check(second.graph().edges().equals(GraphGenerator.random(second.graph().vertexCount(),
                        second.p(), second.seed()).edges()), "regeneração exata");
            }
        }
    }

    private static List<GraphGenerator.Instance> smallSuite() {
        return List.of(new GraphGenerator.Instance("path", "path", null, null, GraphGenerator.path(6)),
                new GraphGenerator.Instance("empty", "empty", null, null, GraphGenerator.empty(4)));
    }

    private static void testBenchmark() throws Exception {
        Path directory = temporaryDirectory();
        Path csv = directory.resolve("results.csv");
        BenchmarkRunner.run(smallSuite(), csv, 5000, 3, 5);
        List<Map<String, String>> rows = CsvExporter.read(csv);
        check(rows.size() == 2, "uma linha por instância");
        check(rows.get(0).get("exact_status").equals("OPTIMAL"), "benchmark ótimo");
        check(rows.get(0).get("exact_optimal_size").equals("3"), "ótimo caminho");
        check(rows.get(1).get("approximation_ratio").equals("NA")
                && rows.get(1).get("gap_percent").equals("NA"), "ótimo zero não divide");
        List<Map<String, String>> raw = CsvExporter.read(BenchmarkRunner.sidecar(csv, "-runs.csv"));
        check(raw.size() == 6, "todas as repetições preservadas");
        for (Map<String, String> row : rows) {
            List<Map<String, String>> same = raw.stream()
                    .filter(value -> value.get("instance_id").equals(row.get("instance_id")))
                    .sorted(java.util.Comparator.comparingLong(value -> Long.parseLong(value.get("exact_runtime_ns"))))
                    .toList();
            check(row.get("exact_runtime_ns").equals(same.get(1).get("exact_runtime_ns")), "mediana de 3");
        }
        Path timeoutCsv = directory.resolve("timeout.csv");
        BenchmarkRunner.run(smallSuite(), timeoutCsv, 0, 1, 0);
        Map<String, String> timedOut = CsvExporter.read(timeoutCsv).get(0);
        check(timedOut.get("exact_status").equals("TIME_LIMIT")
                && timedOut.get("exact_optimal_size").equals("NA")
                && timedOut.get("exact_optimality_proven").equals("false")
                && timedOut.get("approximation_ratio").equals("NA")
                && timedOut.get("gap_percent").equals("NA")
                && timedOut.get("exact_valid").equals("true"), "timeout sem falso ótimo/razão");
        Path report = directory.resolve("report.md");
        ExperimentReport.generate(timeoutCsv, report);
        String text = Files.readString(report, StandardCharsets.UTF_8);
        check(text.contains("1 OPTIMAL") && text.contains("1 TIME_LIMIT")
                && text.contains("SHA-256") && text.contains("timeouts individuais do exato: **1**"),
                "relatório de medições reais");
        expect(IllegalArgumentException.class, () -> BenchmarkRunner.run(smallSuite(), csv, 1, 0, 0));
    }

    private static void testCharts() throws Exception {
        Path directory = temporaryDirectory();
        Path csv = directory.resolve("measured.csv");
        BenchmarkRunner.run(smallSuite(), csv, 0, 1, 0);
        Path figures = directory.resolve("figures");
        ChartGenerator.generate(csv, figures);
        List<Path> images;
        try (var paths = Files.list(figures)) {
            images = paths.filter(value -> value.toString().endsWith(".png")).toList();
        }
        check(images.size() == 6, "seis PNGs");
        for (Path image : images) {
            var decoded = ImageIO.read(image.toFile());
            check(decoded != null && decoded.getWidth() == 1280 && decoded.getHeight() == 800, "PNG válido");
        }
        // Nenhuma instância elegível para razão/ótimo: os gráficos devem informar ausência de dados.
        Path onlyTimeout = directory.resolve("only-timeout.csv");
        BenchmarkRunner.run(smallSuite().subList(0, 1), onlyTimeout, 0, 1, 0);
        ChartGenerator.generate(onlyTimeout, directory.resolve("only-timeout-figures"));
        List<List<?>> inconsistent = new ArrayList<>();
        List<Object> badRow = new ArrayList<>(BenchmarkRunner.row(smallSuite().get(0),
                new ExactBranchAndBound().solve(smallSuite().get(0).graph(), 0),
                new MatchingTwoApproximation().solve(smallSuite().get(0).graph()), 1, 1, 1));
        badRow.set(BenchmarkRunner.HEADER.indexOf("exact_optimal_size"), 3);
        inconsistent.add(badRow);
        CsvExporter.write(directory.resolve("invalid.csv"), BenchmarkRunner.HEADER, inconsistent);
        expect(IOException.class, () -> ChartGenerator.generate(directory.resolve("invalid.csv"), figures));
    }

    private static void testCli() throws Exception {
        expect(IllegalArgumentException.class, () -> Main.execute(new String[] {"unknown"}));
        expect(IllegalArgumentException.class, () -> Main.execute(new String[] {"solve", "--input"}));
        expect(IllegalArgumentException.class, () -> Main.execute(new String[] {"generate", "--bad", "x"}));
        expect(IllegalArgumentException.class, () -> Main.execute(new String[] {"solve", "--algorithm", "wrong"}));
        expect(IllegalArgumentException.class, () -> Main.execute(new String[] {"solve", "--algorithm", "exact"}));
        expect(IllegalArgumentException.class, () -> Main.execute(new String[] {"benchmark", "--repetitions", "0"}));
        expect(IllegalArgumentException.class, () -> Main.execute(new String[] {"benchmark", "--warmup", "-1"}));
        expect(IllegalArgumentException.class, () -> Main.execute(new String[] {"benchmark", "--timeout-ms", "-1"}));
        expect(IllegalArgumentException.class, () -> Main.execute(new String[] {"generate", "--seed", "abc"}));
        expect(IllegalArgumentException.class, () -> Main.execute(new String[] {
                "generate", "--seed", "1", "--seed", "2"}));
        expect(IllegalArgumentException.class, () -> Main.execute(new String[] {"menu", "--bad", "x"}));
        expect(IllegalArgumentException.class, () -> Main.execute(new String[] {"experiment", "--mode", "wrong"}));
        expect(IllegalArgumentException.class, () -> Main.execute(new String[] {"demo", "--bad", "x"}));
    }

    private static void testGuidedUi() throws Exception {
        String demo = capture(() -> Main.execute(new String[] {"demo"}));
        check(demo.contains("COMPARAÇÃO DOS ALGORITMOS") && demo.contains("OPTIMAL")
                && demo.contains("APPROXIMATE") && demo.contains("3 vértices"), "demonstração explicada");
        Path directory = temporaryDirectory();
        Path file = directory.resolve("grafo com espaços.txt");
        GraphFileReader.write(GraphGenerator.path(5), file);
        String comparison = capture(() -> Main.execute(new String[] {"compare", "--input", file.toString(), "--timeout-ms", "0"}));
        check(comparison.contains("TIME_LIMIT") && comparison.contains("N/A")
                && comparison.contains("NÃO necessariamente ótima"), "comparação não inventa ótimo no timeout");
        String menu = capture(() -> new InteractiveMenu(new BufferedReader(new StringReader("x\n7\n\n1\n\n0\n")), directory).run());
        check(menu.contains("Opção inválida") && menu.contains("ENTENDA O PROJETO")
                && menu.contains("COMPARAÇÃO") && menu.contains("Aplicação encerrada"), "menu navega e recupera escolha inválida");
        String eof = capture(() -> new InteractiveMenu(new BufferedReader(new StringReader("")), directory).run());
        check(eof.contains("Aplicação encerrada"), "EOF não causa laço infinito");
        String invalidFile = capture(() -> new InteractiveMenu(new BufferedReader(
                new StringReader("2\ninexistente.txt\n\n\n0\n")), directory).run());
        check(invalidFile.contains("[ERRO]") && invalidFile.contains("Aplicação encerrada"), "erro de arquivo retorna ao menu");
        String quotedPath = capture(() -> new InteractiveMenu(new BufferedReader(new StringReader(
                "2\n\"" + file + "\"\n-1\nabc\n0\n\n0\n")), directory).run());
        check(quotedPath.contains("Informe um número inteiro") && quotedPath.contains("TIME_LIMIT"),
                "caminho com espaços/aspas e repetição de timeout inválido");
        String cancel = capture(() -> new InteractiveMenu(new BufferedReader(new StringReader("5\nn\n\n0\n")), directory).run());
        check(cancel.contains("Experimento cancelado") && !Files.exists(directory.resolve("data")),
                "cancelar experimento completo não escreve arquivos");
        Path csv = directory.resolve("medicoes.csv");
        BenchmarkRunner.run(smallSuite(), csv, 0, 1, 0);
        String originalCsv = Files.readString(csv, StandardCharsets.UTF_8);
        String analysis = capture(() -> new InteractiveMenu(new BufferedReader(new StringReader(
                "6\n\"" + csv + "\"\n\n0\n")), directory).run());
        check(analysis.contains("Análise concluída") && originalCsv.equals(Files.readString(csv, StandardCharsets.UTF_8)),
                "opção de análise preserva o CSV original");
        try (var paths = Files.walk(directory.resolve("data/results/execucoes"))) {
            check(paths.anyMatch(path -> path.getFileName().toString().equals("relatorio.md")), "análise salva relatório em nova pasta");
        }
        String table = capture(() -> ConsoleView.table(new String[] {"A", "B"}, List.of(new String[] {"1", "2"}, new String[] {"3", "4"})));
        check(table.contains("A | B") && table.contains("1 | 2"), "tabela legível sem códigos ANSI");
        check(!demo.contains("\u001B"), "saída funciona em terminal simples e em arquivo");
    }

    private static void testWorkflow() throws Exception {
        Path directory = temporaryDirectory();
        Path output = directory.resolve("execucao");
        ExperimentWorkflow.run(ExperimentWorkflow.quickInstances(), output, 1000, 3, 5, "Rápido de teste");
        check(CsvExporter.read(output.resolve("benchmark.csv")).size() == 8, "8 linhas didáticas");
        check(CsvExporter.read(output.resolve("benchmark-runs.csv")).size() == 24, "24 medições por algoritmo");
        check(GraphGenerator.readSuite(output.resolve("instances")).size() == 8, "instâncias realmente medidas são salvas");
        check(Files.isRegularFile(output.resolve("relatorio.md"))
                && Files.isRegularFile(output.resolve("CONCLUIDO.txt")), "fluxo termina com relatório e marcador");
        try (var paths = Files.list(output.resolve("figures"))) {
            check(paths.filter(path -> path.toString().endsWith(".png")).count() == 6, "6 gráficos no fluxo");
        }
        String before = Files.readString(output.resolve("benchmark.csv"), StandardCharsets.UTF_8);
        expect(IOException.class, () -> ExperimentWorkflow.run(ExperimentWorkflow.quickInstances(), output, 1000, 1, 0, "Não regravar"));
        check(before.equals(Files.readString(output.resolve("benchmark.csv"), StandardCharsets.UTF_8)), "destino existente permanece intacto");
        Path invalidOutput = directory.resolve("invalid");
        expect(IllegalArgumentException.class, () -> ExperimentWorkflow.run(ExperimentWorkflow.quickInstances(), invalidOutput,
                -1, 1, 0, "Inválido"));
        check(!Files.exists(invalidOutput), "parâmetros inválidos não criam saída");
        // Verifica o encadeamento completo da CLI com a grade integral, sem esperar buscas longas.
        Path fullOutput = directory.resolve("grade-integral-timeout-zero");
        Main.execute(new String[] {"experiment", "--mode", "full", "--output", fullOutput.toString(),
                "--timeout-ms", "0", "--repetitions", "1", "--warmup", "0"});
        List<Map<String, String>> fullRows = CsvExporter.read(fullOutput.resolve("benchmark.csv"));
        check(fullRows.size() == 230 && Files.isRegularFile(fullOutput.resolve("CONCLUIDO.txt")), "modo full mantém as 230 instâncias");
        check(fullRows.stream().filter(row -> row.get("exact_status").equals("TIME_LIMIT")).allMatch(
                row -> row.get("exact_optimal_size").equals("NA") && row.get("approximation_ratio").equals("NA")),
                "fluxo integral preserva convenções de timeout");
    }

    private static String capture(CheckedTest action) throws Exception {
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (PrintStream output = new PrintStream(bytes, true, StandardCharsets.UTF_8)) {
            System.setOut(output);
            System.setErr(output);
            action.execute();
        } finally {
            System.setOut(originalOut);
            System.setErr(originalErr);
        }
        return bytes.toString(StandardCharsets.UTF_8);
    }

    private static Path temporaryDirectory() throws IOException {
        Path base = Path.of(System.getProperty("java.io.tmpdir"), "opencode");
        Files.createDirectories(base);
        return Files.createTempDirectory(base, "vertex-cover-tests-");
    }

    private static void run(String name, CheckedTest test) {
        PrintStream original = System.out;
        ByteArrayOutputStream details = new ByteArrayOutputStream();
        Throwable failure = null;
        long start = System.nanoTime();
        // Os testes continuam executando as integrações reais, mas sem poluir o resumo.
        try (PrintStream output = new PrintStream(details, true, StandardCharsets.UTF_8)) {
            System.setOut(output);
            test.execute();
        } catch (Exception | AssertionError error) {
            failure = error;
        } finally {
            System.setOut(original);
        }
        long elapsed = System.nanoTime() - start;
        if (failure == null) {
            passed++;
            System.out.printf(Locale.ROOT, "  [ OK ] %-65s %14s%n", name, ConsoleView.time(elapsed));
        } else {
            failed++;
            System.err.println("  [FALHA] " + name);
            System.err.print(details.toString(StandardCharsets.UTF_8));
            failure.printStackTrace(System.err);
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void expect(Class<? extends Throwable> type, CheckedTest action) {
        try {
            action.execute();
        } catch (Throwable error) {
            if (type.isInstance(error)) {
                return;
            }
            throw new AssertionError("Exceção inesperada", error);
        }
        throw new AssertionError("Esperava " + type.getSimpleName());
    }

    @FunctionalInterface
    private interface CheckedTest {
        void execute() throws Exception;
    }
}
