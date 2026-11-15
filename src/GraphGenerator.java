import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public final class GraphGenerator {
    public static final long DEFAULT_SEED = 20261115L;
    public static final int[] SIZES = {10, 15, 20, 25, 30};
    public static final double[] PROBABILITIES = {0.1, 0.3, 0.5, 0.7};

    public record Instance(String id, String family, Long seed, Double p, Graph graph) { }

    private GraphGenerator() { }

    public static Graph random(int n, double p, long seed) {
        if (!Double.isFinite(p) || p < 0 || p > 1) {
            throw new IllegalArgumentException("Probabilidade deve estar entre zero e um.");
        }
        Graph graph = new Graph(n);
        Random random = new Random(seed);
        for (int u = 0; u < n; u++) {
            for (int v = u + 1; v < n; v++) {
                if (random.nextDouble() < p) {
                    graph.addEdge(u, v);
                }
            }
        }
        return graph;
    }

    public static Graph path(int n) {
        Graph graph = new Graph(n);
        for (int v = 1; v < n; v++) {
            graph.addEdge(v - 1, v);
        }
        return graph;
    }

    public static Graph cycle(int n) {
        if (n < 3) {
            throw new IllegalArgumentException("Ciclo simples requer n >= 3.");
        }
        Graph graph = path(n);
        graph.addEdge(0, n - 1);
        return graph;
    }

    /** r folhas, mais o centro (total r + 1). */
    public static Graph star(int leaves) {
        if (leaves < 0 || leaves == Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Quantidade de folhas inválida.");
        }
        Graph graph = new Graph(leaves + 1);
        for (int v = 1; v <= leaves; v++) {
            graph.addEdge(0, v);
        }
        return graph;
    }

    public static Graph complete(int n) {
        Graph graph = new Graph(n);
        for (int u = 0; u < n; u++) {
            for (int v = u + 1; v < n; v++) {
                graph.addEdge(u, v);
            }
        }
        return graph;
    }

    public static Graph completeBipartite(int a, int b) {
        if (a < 0 || b < 0 || (long) a + b > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Tamanhos de partes inválidos.");
        }
        Graph graph = new Graph(a + b);
        for (int u = 0; u < a; u++) {
            for (int v = a; v < a + b; v++) {
                graph.addEdge(u, v);
            }
        }
        return graph;
    }

    public static Graph empty(int n) {
        return new Graph(n);
    }

    public static List<Instance> defaultInstances(long baseSeed) {
        if (baseSeed > Long.MAX_VALUE - 199) {
            throw new IllegalArgumentException("Semente base não permite 200 sementes consecutivas.");
        }
        List<Instance> instances = new ArrayList<>();
        int counter = 0;
        for (int n : SIZES) {
            for (double p : PROBABILITIES) {
                for (int repetition = 0; repetition < 10; repetition++) {
                    long seed = baseSeed + counter++;
                    String id = String.format(Locale.ROOT, "random_n%d_p%.1f_i%02d", n, p, repetition);
                    instances.add(new Instance(id, "erdos_renyi", seed, p, random(n, p, seed)));
                }
            }
        }
        for (int n : SIZES) {
            instances.add(new Instance("path_n" + n, "path", null, null, path(n)));
            instances.add(new Instance("cycle_n" + n, "cycle", null, null, cycle(n)));
            instances.add(new Instance("star_n" + n, "star", null, null, star(n - 1)));
            instances.add(new Instance("complete_n" + n, "complete", null, null, complete(n)));
            instances.add(new Instance("bipartite_n" + n, "complete_bipartite", null, null,
                    completeBipartite(n / 2, n - n / 2)));
            instances.add(new Instance("empty_n" + n, "empty", null, null, empty(n)));
        }
        return List.copyOf(instances);
    }

    public static List<Instance> writeSuite(Path directory, long baseSeed) throws IOException {
        List<Instance> instances = defaultInstances(baseSeed);
        writeInstances(instances, directory);
        return instances;
    }

    public static void writeInstances(List<Instance> instances, Path directory) throws IOException {
        List<List<?>> manifest = new ArrayList<>();
        for (Instance instance : instances) {
            String file = instance.id() + ".txt";
            GraphFileReader.write(instance.graph(), directory.resolve(file));
            manifest.add(java.util.Arrays.asList(instance.id(), instance.family(), instance.seed(),
                    instance.graph().vertexCount(), instance.graph().edgeCount(), instance.p(), file));
        }
        CsvExporter.write(directory.resolve("manifest.csv"),
                List.of("instance_id", "family", "seed", "n", "m", "p", "file"), manifest);
    }

    public static List<Instance> readSuite(Path directory) throws IOException {
        List<Instance> instances = new ArrayList<>();
        java.util.Set<String> identifiers = new java.util.HashSet<>();
        for (Map<String, String> row : CsvExporter.read(directory.resolve("manifest.csv"))) {
            try {
                String id = row.get("instance_id");
                String family = row.get("family");
                if (id == null || id.isBlank() || family == null || family.isBlank() || !identifiers.add(id)) {
                    throw new IllegalArgumentException("Identificador/família ausente ou identificador repetido.");
                }
                Graph graph = GraphFileReader.read(directory.resolve(row.get("file")));
                if (graph.vertexCount() != Integer.parseInt(row.get("n"))
                        || graph.edgeCount() != Integer.parseInt(row.get("m"))) {
                    throw new IllegalArgumentException("Contagens do manifesto divergem do arquivo.");
                }
                Long seed = missing(row.get("seed")) ? null : Long.valueOf(row.get("seed"));
                Double p = missing(row.get("p")) ? null : Double.valueOf(row.get("p"));
                if (p != null && (!Double.isFinite(p) || p < 0 || p > 1)) {
                    throw new IllegalArgumentException("Probabilidade inválida no manifesto.");
                }
                instances.add(new Instance(id, family, seed, p, graph));
            } catch (IllegalArgumentException | NullPointerException error) {
                throw new IOException("Manifesto inválido: " + error.getMessage(), error);
            }
        }
        if (instances.isEmpty()) {
            throw new IOException("Manifesto sem instâncias.");
        }
        return List.copyOf(instances);
    }

    private static boolean missing(String text) {
        return text == null || text.isBlank() || text.equals("NA");
    }
}
