import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Pós-processamento exclusivo do relatório: não mede algoritmos nem altera a aplicação.
 * Executar na raiz de relatorio/. As tabelas são derivadas das cópias literais dos CSVs.
 */
public final class ReportStats {
    private static final Path DATA = Path.of("dados");
    private static final Path TABLES = Path.of("tables");
    private static final Locale PT = Locale.forLanguageTag("pt-BR");
    private static final Map<String, String> FAMILIES = Map.of(
            "erdos_renyi", "$G(n,p)$", "path", "Caminho", "cycle", "Ciclo", "star", "Estrela",
            "complete", "Completo", "complete_bipartite", "Bipartido completo", "empty", "Sem arestas");

    private ReportStats() { }

    public static void main(String[] args) throws Exception {
        List<Map<String, String>> rows = CsvExporter.read(DATA.resolve("benchmark.csv"));
        List<Map<String, String>> raw = CsvExporter.read(DATA.resolve("benchmark-runs.csv"));
        List<GraphGenerator.Instance> instances = GraphGenerator.readSuite(DATA.resolve("instancias"));
        require(hash(DATA.resolve("benchmark.csv")).equals("e4e1d95f17c429d6e29fa7955b611f72e93de5824a04df5f1d6e196065a07607"),
                "O CSV não é o corpus congelado deste artigo. Uma nova execução exige revisar também a redação.");
        require(hash(DATA.resolve("benchmark-runs.csv")).equals("a58b4644f3349ee267273e1751f0e026c32151c58fd3d558e298d0234afe3a53"),
                "As execuções individuais não correspondem ao corpus documentado.");
        audit(rows, raw, instances);
        Files.createDirectories(TABLES);
        List<Map<String, String>> random = rows.stream().filter(row -> row.get("family").equals("erdos_renyi")).toList();
        List<Map<String, String>> quality = rows.stream().filter(ReportStats::hasRatio).toList();
        List<Map<String, String>> structured = rows.stream().filter(row -> !row.get("family").equals("erdos_renyi")).toList();
        long optimal = rows.stream().filter(ReportStats::proven).count();
        long rawTimeout = raw.stream().filter(row -> !proven(row)).count();
        long faster = rows.stream().filter(row -> v(row, "approx_runtime_ns") < v(row, "exact_runtime_ns")).count();
        long equalPositive = quality.stream().filter(row -> v(row, "approximation_ratio") == 1).count();
        long equalZero = rows.stream().filter(row -> proven(row) && v(row, "exact_optimal_size") == 0).count();
        long factorTwo = quality.stream().filter(row -> v(row, "approximation_ratio") == 2).count();
        String macros = "% Gerado por ferramentas/ReportStats.java; não editar valores manualmente.\n"
                + macro("TotalInstances", "" + rows.size()) + macro("RandomInstances", "" + random.size())
                + macro("StructuredInstances", "" + structured.size())
                + macro("OptimalInstances", "" + optimal) + macro("TimeoutInstances", "" + (rows.size() - optimal))
                + macro("RawMeasurements", "" + raw.size()) + macro("RawTimeouts", "" + rawTimeout)
                + macro("QualityInstances", "" + quality.size()) + macro("ZeroInstances", "" + equalZero)
                + macro("FasterApproxInstances", "" + faster) + macro("EqualPositiveInstances", "" + equalPositive)
                + macro("EqualTotalInstances", "" + (equalPositive + equalZero))
                + macro("FactorTwoInstances", "" + factorTwo)
                + macro("MeanRatio", fmt(mean(quality, "approximation_ratio"), 3))
                + macro("MedianRatio", fmt(med(quality, "approximation_ratio"), 3))
                + macro("MeanGap", fmt(mean(quality, "gap_percent"), 2))
                + macro("RandomMeanRatio", fmt(mean(random.stream().filter(ReportStats::hasRatio).toList(), "approximation_ratio"), 3))
                + macro("StructuredMeanRatio", fmt(mean(structured.stream().filter(ReportStats::hasRatio).toList(), "approximation_ratio"), 3));
        write("generated-macros.tex", macros);
        characterize(rows);
        configurations(random);
        sizes(random);
        quality(quality);
        structured(structured);
        timeouts(rows.stream().filter(row -> !proven(row)).toList());
        String summary = """
                AUDITORIA DOS DADOS DO RELATÓRIO
                ==============================
                CSVs: %d linhas-resumo, %d linhas individuais.
                Instâncias: %d aleatórias, %d estruturadas.
                Provas de ótimo: %d; timeouts no resumo: %d; timeouts individuais: %d.
                Razão definida: %d; ótimo zero: %d.
                Razão média: %.12f; mediana superior: %.12f; gap médio: %.12f%%.
                Aproximação com tempo menor no resumo: %d de %d.
                Cardinalidade igual ao ótimo positivo: %d; incluindo ótimos zero: %d.
                Razão exatamente 2: %d.
                Seleções por mediana e repetições conferidas contra todas as linhas individuais.
                Contagens n/m, densidades, sementes e aproximações conferidas com as instâncias.
                Flags de validade e convenções de ótimo/NA conferidas em todas as linhas.
                Os CSVs não guardam os vértices das soluções exatas: sua validade foi verificada
                pelo benchmark na execução original, não reconstruída desta exportação.
                Testes atuais são executados separadamente; não são novas medições do benchmark.
                Quartis: postos ceil(q*N), base um, para q=0.25 e 0.75.
                Mediana entre instâncias: elemento floor(N/2), base zero (mediana superior).
                Intervalos das tabelas são [Q1; Q3], não intervalos de confiança.
                SHA-256 benchmark.csv: %s
                SHA-256 benchmark-runs.csv: %s
                """.formatted(rows.size(), raw.size(), random.size(), structured.size(), optimal,
                rows.size() - optimal, rawTimeout, quality.size(), equalZero, mean(quality, "approximation_ratio"),
                med(quality, "approximation_ratio"), mean(quality, "gap_percent"), faster, rows.size(),
                equalPositive, equalPositive + equalZero, factorTwo, hash(DATA.resolve("benchmark.csv")),
                hash(DATA.resolve("benchmark-runs.csv")));
        Files.writeString(Path.of("evidencias/auditoria-dados.txt"), summary, StandardCharsets.UTF_8);
        System.out.print(summary);
    }

    private static void audit(List<Map<String, String>> rows, List<Map<String, String>> raw,
                              List<GraphGenerator.Instance> instances) throws IOException {
        require(rows.size() == 230 && raw.size() == 690 && instances.size() == 230, "Corpus diferente do protocolo documentado.");
        Map<String, GraphGenerator.Instance> graphs = new LinkedHashMap<>();
        for (GraphGenerator.Instance instance : instances) {
            require(graphs.put(instance.id(), instance) == null, "Instância repetida no manifesto.");
        }
        Map<String, List<Map<String, String>>> repetitions = group(raw, "instance_id");
        java.util.Set<String> ids = new java.util.HashSet<>();
        for (Map<String, String> row : raw) {
            ChartGenerator.validate(row);
            require(row.get("exact_valid").equals("true"), "Incumbente marcada como inválida.");
        }
        for (Map<String, String> row : rows) {
            ChartGenerator.validate(row);
            String id = row.get("instance_id");
            require(ids.add(id), "Identificador repetido no resumo.");
            GraphGenerator.Instance instance = graphs.get(id);
            require(instance != null, "Instância ausente do manifesto: " + id);
            Graph graph = instance.graph();
            require(Integer.parseInt(row.get("n")) == graph.vertexCount()
                    && Integer.parseInt(row.get("m")) == graph.edgeCount(), "n/m divergentes: " + id);
            require(Math.abs(v(row, "density") - graph.density()) < 1e-12, "Densidade divergente: " + id);
            require(row.get("family").equals(instance.family()) && row.get("exact_valid").equals("true"), "Metadados inválidos.");
            if (instance.seed() != null) {
                require(row.get("seed").equals(instance.seed().toString()) && Double.compare(v(row, "p"), instance.p()) == 0,
                        "Semente ou p divergente.");
                require(GraphGenerator.random(graph.vertexCount(), instance.p(), instance.seed()).edges().equals(graph.edges()),
                        "Arquivo não reproduz a semente.");
            }
            SolverResult approx = new MatchingTwoApproximation().solve(graph);
            require(approx.solution().isValid(graph) && approx.solution().size() == v(row, "approx_solution_size"),
                    "Aproximação não reproduz o tamanho registrado.");
            List<Map<String, String>> runs = repetitions.get(id);
            require(runs != null && runs.size() == 3 && row.get("repetitions").equals("3"), "Repetições incorretas.");
            require(runs.stream().map(value -> value.get("exact_selected_repetition")).distinct().count() == 3,
                    "Repetição individual duplicada.");
            for (String algorithm : List.of("exact", "approx")) {
                String runtime = algorithm + "_runtime_ns";
                require(v(row, runtime) == med(runs, runtime), "Tempo não é a mediana: " + id);
                String selected = row.get(algorithm + "_selected_repetition");
                Map<String, String> origin = runs.stream()
                        .filter(run -> run.get(algorithm + "_selected_repetition").equals(selected)).findFirst().orElseThrow();
                List<String> fields = algorithm.equals("exact")
                        ? List.of(runtime, "exact_status", "exact_best_known_size", "exact_optimal_size",
                                "exact_optimality_proven", "exact_nodes_explored", "exact_pruned_nodes")
                        : List.of(runtime, "approx_solution_size", "approx_valid");
                for (String field : fields) {
                    require(row.get(field).equals(origin.get(field)), "Campo não pertence à execução selecionada: " + field);
                }
            }
            if (hasRatio(row)) {
                require(v(row, "approximation_ratio") >= 1 && v(row, "approximation_ratio") <= 2, "Fator fora da garantia.");
                require(Math.abs(v(row, "gap_percent") - 100 * (v(row, "approximation_ratio") - 1)) < 1e-9, "Gap inconsistente.");
            }
        }
        require(ids.size() == graphs.size() && repetitions.size() == rows.size(), "Corpus incompleto.");
    }

    private static void characterize(List<Map<String, String>> rows) throws IOException {
        StringBuilder table = new StringBuilder("""
                \\begin{table}[htbp]
                \\centering
                \\caption{Caracterização das 230 instâncias efetivamente medidas. Intervalos indicam mínimo e máximo observados por família; $d$ é a densidade.}
                \\label{tab:characterization}
                \\small
                \\begin{tabular}{lrrrr}
                \\toprule
                Família & Quantidade & $n$ & $m$ & $d$ \\\\
                \\midrule
                """);
        for (var entry : group(rows, "family").entrySet()) {
            List<Map<String, String>> group = entry.getValue();
            table.append(FAMILIES.get(entry.getKey())).append(" & ").append(group.size()).append(" & ")
                    .append(range(group, "n", 0)).append(" & ").append(range(group, "m", 0)).append(" & ")
                    .append(range(group, "density", 3)).append(" \\\\\n");
        }
        table.append("\\bottomrule\n\\end{tabular}\n\\end{table}\n");
        write("characterization.tex", table.toString());
    }

    private static void configurations(List<Map<String, String>> rows) throws IOException {
        StringBuilder table = new StringBuilder("""
                \\begin{table}[htbp]
                \\centering
                \\caption{Tempos nos 200 grafos aleatórios: dez instâncias por configuração. Valores são medianas superiores entre instâncias, seguidas de $[Q_1;Q_3]$. A unidade do exato é ms e a da aproximação é $\\mu$s. O tempo parcial do único timeout aleatório foi mantido; O/T conta ótimo/timeout.}
                \\label{tab:configurations}
                \\small
                \\setlength{\\tabcolsep}{3pt}
                \\begin{tabular}{rrrrrl}
                \\toprule
                $n$ & $p$ & $\\bar d$ & Exato (ms) & Aprox. ($\\mu$s) & O/T \\\\
                \\midrule
                """);
        Map<String, List<Map<String, String>>> configurations = new LinkedHashMap<>();
        for (var row : rows) {
            configurations.computeIfAbsent(row.get("n") + "/" + row.get("p"), ignored -> new ArrayList<>()).add(row);
        }
        for (var group : configurations.values()) {
            long solved = group.stream().filter(ReportStats::proven).count();
            table.append(group.get(0).get("n")).append(" & ").append(fmt(v(group.get(0), "p"), 1)).append(" & ")
                    .append(fmt(mean(group, "density"), 3)).append(" & ")
                    .append(interval(group, "exact_runtime_ns", 1e6, 3)).append(" & ")
                    .append(interval(group, "approx_runtime_ns", 1e3, 2)).append(" & ")
                    .append(solved).append("/").append(group.size() - solved).append(" \\\\\n");
        }
        table.append("\\bottomrule\n\\end{tabular}\n\\end{table}\n");
        write("configurations.tex", table.toString());
    }

    private static void sizes(List<Map<String, String>> rows) throws IOException {
        StringBuilder table = new StringBuilder("""
                \\begin{table}[htbp]
                \\centering
                \\caption{Resumo por tamanho nos grafos aleatórios (40 instâncias por linha, quatro probabilidades). Tempos, nós e podas são medianas superiores entre instâncias. Contagens em timeout são parciais.}
                \\label{tab:sizes}
                \\small
                \\begin{tabular}{rrrrrr}
                \\toprule
                $n$ & Exato (ms) & Aprox. ($\\mu$s) & Nós & Podas & O/T \\\\
                \\midrule
                """);
        for (var group : group(rows, "n").values()) {
            long solved = group.stream().filter(ReportStats::proven).count();
            table.append(group.get(0).get("n")).append(" & ").append(fmt(med(group, "exact_runtime_ns") / 1e6, 3))
                    .append(" & ").append(fmt(med(group, "approx_runtime_ns") / 1e3, 2))
                    .append(" & ").append(integer(med(group, "exact_nodes_explored")))
                    .append(" & ").append(integer(med(group, "exact_pruned_nodes"))).append(" & ")
                    .append(solved).append("/").append(group.size() - solved).append(" \\\\\n");
        }
        table.append("\\bottomrule\n\\end{tabular}\n\\end{table}\n");
        write("sizes.tex", table.toString());
    }

    private static void quality(List<Map<String, String>> all) throws IOException {
        StringBuilder table = new StringBuilder("""
                \\begin{table}[htbp]
                \\centering
                \\caption{Qualidade somente com ótimo comprovado e positivo. $\\bar R$ é a média das razões individuais, não a razão de médias. Mediana e quartis referem-se a instâncias; gap médio em porcentagem.}
                \\label{tab:quality}
                \\small
                \\setlength{\\tabcolsep}{4pt}
                \\begin{tabular}{lrrrrr}
                \\toprule
                Amostra & $N$ & $\\bar R$ & $R_{\\mathrm{med}}$ $[Q_1;Q_3]$ & $R_{\\max}$ & Gap médio (\\%) \\\\
                \\midrule
                """);
        List<List<Map<String, String>>> groups = List.of(all,
                all.stream().filter(row -> row.get("family").equals("erdos_renyi")).toList(),
                all.stream().filter(row -> !row.get("family").equals("erdos_renyi")).toList());
        String[] labels = {"Todas elegíveis", "Aleatórias", "Estruturadas"};
        for (int i = 0; i < groups.size(); i++) {
            var group = groups.get(i);
            table.append(labels[i]).append(" & ").append(group.size()).append(" & ")
                    .append(fmt(mean(group, "approximation_ratio"), 3)).append(" & ")
                    .append(interval(group, "approximation_ratio", 1, 3)).append(" & ")
                    .append(fmt(max(group, "approximation_ratio"), 3)).append(" & ")
                    .append(fmt(mean(group, "gap_percent"), 2)).append(" \\\\\n");
        }
        table.append("\\bottomrule\n\\end{tabular}\n\\end{table}\n");
        write("quality.tex", table.toString());
    }

    private static void structured(List<Map<String, String>> rows) throws IOException {
        StringBuilder table = new StringBuilder("""
                \\begin{table}[htbp]
                \\centering
                \\caption{Resultados das 30 instâncias estruturadas (três medições por algoritmo; linha da mediana). Melhor é a incumbente exata; OPT é preenchido somente com prova. Tempos do exato em ms; nós são estados explorados.}
                \\label{tab:structured}
                \\small
                \\setlength{\\tabcolsep}{4pt}
                \\begin{tabular}{lrrrrrr}
                \\toprule
                Família & $n$ & Melhor & OPT & Aprox. & Exato (ms) & Nós \\\\
                \\midrule
                """);
        for (var row : rows) {
            table.append(FAMILIES.get(row.get("family"))).append(" & ").append(row.get("n")).append(" & ")
                    .append(row.get("exact_best_known_size")).append(" & ").append(row.get("exact_optimal_size"))
                    .append(" & ").append(row.get("approx_solution_size")).append(" & ")
                    .append(fmt(v(row, "exact_runtime_ns") / 1e6, 4)).append(proven(row) ? "" : "$^{*}$")
                    .append(" & ").append(integer(v(row, "exact_nodes_explored"))).append(" \\\\\n");
        }
        table.append("""
                \\bottomrule
                \\end{tabular}

                \\smallskip
                {\\footnotesize $^{*}$TIME\\_LIMIT: tempo e nós parciais; sem certificado de ótimo.}
                \\end{table}
                """);
        write("structured.tex", table.toString());
    }

    private static void timeouts(List<Map<String, String>> rows) throws IOException {
        StringBuilder table = new StringBuilder("""
                \\begin{table}[htbp]
                \\centering
                \\caption{As duas instâncias com TIME\\_LIMIT no resumo. Tempos em ms e contagens pertencem às execuções selecionadas; ótimo, razão e gap são NA em ambas. Cada instância teve três timeouts nas medições individuais.}
                \\label{tab:timeouts}
                \\small
                \\setlength{\\tabcolsep}{4pt}
                \\begin{tabular}{lrrrrr}
                \\toprule
                Instância & Melhor & Aprox. & Tempo parcial & Nós & Podas \\\\
                \\midrule
                """);
        for (var row : rows) {
            String label = row.get("family").equals("complete") ? "$K_{30}$" : "$G(30;0{,}7)$, $i=6$";
            table.append(label).append(" & ").append(row.get("exact_best_known_size")).append(" & ")
                    .append(row.get("approx_solution_size")).append(" & ")
                    .append(fmt(v(row, "exact_runtime_ns") / 1e6, 4)).append(" & ")
                    .append(integer(v(row, "exact_nodes_explored"))).append(" & ")
                    .append(integer(v(row, "exact_pruned_nodes"))).append(" \\\\\n");
        }
        table.append("\\bottomrule\n\\end{tabular}\n\\end{table}\n");
        write("timeouts.tex", table.toString());
    }

    private static Map<String, List<Map<String, String>>> group(List<Map<String, String>> rows, String key) {
        Map<String, List<Map<String, String>>> groups = new LinkedHashMap<>();
        for (var row : rows) {
            groups.computeIfAbsent(row.get(key), ignored -> new ArrayList<>()).add(row);
        }
        return groups;
    }

    private static boolean proven(Map<String, String> row) { return row.get("exact_status").equals("OPTIMAL"); }
    private static boolean hasRatio(Map<String, String> row) { return proven(row) && !row.get("approximation_ratio").equals("NA"); }
    private static double v(Map<String, String> row, String key) { return Double.parseDouble(row.get(key)); }
    private static double mean(List<Map<String, String>> rows, String key) { return rows.stream().mapToDouble(row -> v(row, key)).average().orElseThrow(); }
    private static double max(List<Map<String, String>> rows, String key) { return rows.stream().mapToDouble(row -> v(row, key)).max().orElseThrow(); }
    private static double min(List<Map<String, String>> rows, String key) { return rows.stream().mapToDouble(row -> v(row, key)).min().orElseThrow(); }

    private static double med(List<Map<String, String>> rows, String key) {
        return rows.stream().mapToDouble(row -> v(row, key)).sorted().toArray()[rows.size() / 2];
    }

    private static double quartile(List<Map<String, String>> rows, String key, double q) {
        double[] sorted = rows.stream().mapToDouble(row -> v(row, key)).sorted().toArray();
        return sorted[(int) Math.ceil(q * sorted.length) - 1];
    }

    private static String interval(List<Map<String, String>> rows, String key, double divisor, int decimals) {
        return fmt(med(rows, key) / divisor, decimals) + " [" + fmt(quartile(rows, key, 0.25) / divisor, decimals)
                + ";" + fmt(quartile(rows, key, 0.75) / divisor, decimals) + "]";
    }

    private static String range(List<Map<String, String>> rows, String key, int decimals) {
        return fmt(min(rows, key), decimals) + "--" + fmt(max(rows, key), decimals);
    }

    private static String fmt(double value, int decimals) {
        return String.format(PT, "%." + decimals + "f", value);
    }

    private static String integer(double value) {
        return String.format(Locale.ROOT, "%.0f", value).replaceAll("(?<=\\d)(?=(\\d{3})+$)", "\\\\,");
    }

    private static String macro(String name, String value) {
        return "\\newcommand{\\" + name + "}{" + value + "}\n";
    }

    private static void write(String file, String text) throws IOException {
        Files.writeString(TABLES.resolve(file), "% Gerado dos CSVs reais por ReportStats.java.\n" + text, StandardCharsets.UTF_8);
    }

    private static String hash(Path path) throws IOException, NoSuchAlgorithmException {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));
    }

    private static void require(boolean condition, String message) throws IOException {
        if (!condition) {
            throw new IOException(message);
        }
    }
}
