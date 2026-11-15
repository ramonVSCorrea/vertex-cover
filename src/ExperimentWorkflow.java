import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Fluxo guiado: instâncias -> benchmark -> gráficos -> relatório, sem sobrescrever resultados. */
public final class ExperimentWorkflow {
    private ExperimentWorkflow() { }

    public static List<GraphGenerator.Instance> quickInstances() {
        return List.of(
                new GraphGenerator.Instance("caminho_n10", "path", null, null, GraphGenerator.path(10)),
                new GraphGenerator.Instance("ciclo_n9", "cycle", null, null, GraphGenerator.cycle(9)),
                new GraphGenerator.Instance("estrela_n10", "star", null, null, GraphGenerator.star(9)),
                new GraphGenerator.Instance("completo_n10", "complete", null, null, GraphGenerator.complete(10)),
                new GraphGenerator.Instance("bipartido_n10", "complete_bipartite", null, null,
                        GraphGenerator.completeBipartite(5, 5)),
                new GraphGenerator.Instance("sem_arestas_n10", "empty", null, null, GraphGenerator.empty(10)),
                new GraphGenerator.Instance("aleatorio_n10_p03", "erdos_renyi", 20261115L, 0.3,
                        GraphGenerator.random(10, 0.3, 20261115L)),
                new GraphGenerator.Instance("aleatorio_n12_p05", "erdos_renyi", 20261116L, 0.5,
                        GraphGenerator.random(12, 0.5, 20261116L)));
    }

    public static Path newOutputPath(Path parent, String mode) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS"));
        return parent.resolve(mode + "-" + timestamp);
    }

    public static void run(List<GraphGenerator.Instance> instances, Path output, long timeoutMs,
                           int repetitions, int warmupRuns, String description) throws IOException {
        if (instances.isEmpty() || repetitions < 1 || warmupRuns < 0 || timeoutMs < 0
                || timeoutMs > Long.MAX_VALUE / 1_000_000L) {
            throw new IllegalArgumentException("Parâmetros inválidos de experimento.");
        }
        // createDirectory é intencional: um destino existente nunca é regravado pelo fluxo guiado.
        Files.createDirectories(output.toAbsolutePath().getParent());
        if (Files.exists(output)) {
            throw new IOException("A pasta de saída já existe. Escolha outra para preservar os resultados: " + output);
        }
        Files.createDirectory(output);
        Files.writeString(output.resolve("LEIA-ME.txt"), """
                EXPERIMENTO DE COBERTURA POR VÉRTICES
                ===================================
                Modalidade: %s
                Instâncias: %d
                Repetições por algoritmo/instância: %d
                Timeout do exato por medição: %d ms

                Esta pasta foi criada para uma nova execução e não substitui o benchmark original.
                O modo rápido é didático: não substitui as 230 instâncias do protocolo completo.

                Arquivos:
                  instances/                 Grafos realmente usados e manifesto com sementes.
                  benchmark.csv              Uma linha por instância, selecionada pela mediana.
                  benchmark-runs.csv         Todas as medições, inclusive timeouts.
                  benchmark-environment.md   Ambiente e parâmetros da execução.
                  figures/                   Seis PNGs; abra-os para visualizar os resultados.
                  relatorio.md               Tabelas e análise dos CSVs reais.
                  CONCLUIDO.txt              Existe somente quando todas as etapas terminam.

                OPTIMAL: mínimo comprovado.
                TIME_LIMIT: melhor cobertura conhecida, sem prova de ótimo.
                NA: valor desconhecido ou não aplicável; não significa zero.

                Comece por relatorio.md e depois abra figures/.
                """.formatted(description, instances.size(), repetitions, timeoutMs), StandardCharsets.UTF_8);
        ConsoleView.section("EXPERIMENTO | " + description, "Etapas automáticas; cada saída fica nesta nova pasta.");
        ConsoleView.file("Destino", output);
        ConsoleView.info("[1/4] Salvando os grafos e suas sementes...");
        GraphGenerator.writeInstances(instances, output.resolve("instances"));
        ConsoleView.info("[2/4] Medindo os dois algoritmos...");
        Path csv = output.resolve("benchmark.csv");
        BenchmarkRunner.run(instances, csv, timeoutMs, repetitions, warmupRuns);
        ConsoleView.info("[3/4] Criando seis gráficos a partir dos dados reais...");
        ChartGenerator.generate(csv, output.resolve("figures"));
        ConsoleView.info("[4/4] Gerando o relatório com tabelas e explicações...");
        ExperimentReport.generate(csv, output.resolve("relatorio.md"));
        Files.writeString(output.resolve("CONCLUIDO.txt"),
                "As quatro etapas terminaram. Comece por relatorio.md e pela pasta figures/.\n", StandardCharsets.UTF_8);
        ConsoleView.section("EXPERIMENTO CONCLUÍDO", "Você não precisa abrir o CSV para começar a análise.");
        ConsoleView.file("1. Leia o relatório", output.resolve("relatorio.md"));
        ConsoleView.file("2. Abra os gráficos PNG", output.resolve("figures"));
        ConsoleView.file("3. Consulte os dados completos", csv);
    }
}
