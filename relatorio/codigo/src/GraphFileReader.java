import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Formato: n m, seguido de exatamente m arestas base um. Linhas vazias são ignoradas. */
public final class GraphFileReader {
    private GraphFileReader() { }

    public static Graph read(Path path) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String header = nextNonBlank(reader);
            if (header == null) {
                throw new IOException("Arquivo de grafo vazio: " + path);
            }
            if (header.startsWith("\uFEFF")) {
                header = header.substring(1);
            }
            int[] counts = pair(header);
            if (counts[0] < 0 || counts[1] < 0
                    || counts[1] > (long) counts[0] * (counts[0] - 1) / 2) {
                throw new IllegalArgumentException("Contagens n/m inválidas.");
            }
            Graph graph = new Graph(counts[0]);
            for (int index = 0; index < counts[1]; index++) {
                String line = nextNonBlank(reader);
                if (line == null) {
                    throw new IllegalArgumentException("Menos arestas que o m declarado.");
                }
                int[] endpoints = pair(line);
                // Validar antes da subtração também impede overflow com Integer.MIN_VALUE.
                if (endpoints[0] < 1 || endpoints[1] < 1
                        || endpoints[0] > counts[0] || endpoints[1] > counts[0]) {
                    throw new IllegalArgumentException("Identificador de vértice fora de 1..n.");
                }
                graph.addEdge(endpoints[0] - 1, endpoints[1] - 1);
            }
            if (nextNonBlank(reader) != null) {
                throw new IllegalArgumentException("Mais arestas/dados que o m declarado.");
            }
            return graph;
        } catch (IllegalArgumentException error) {
            throw new IOException("Grafo inválido em " + path + ": " + error.getMessage(), error);
        }
    }

    public static void write(Graph graph, Path path) throws IOException {
        if (path.toAbsolutePath().getParent() != null) {
            Files.createDirectories(path.toAbsolutePath().getParent());
        }
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write(graph.vertexCount() + " " + graph.edgeCount());
            writer.newLine();
            for (Edge edge : graph.edges()) {
                writer.write((edge.u() + 1) + " " + (edge.v() + 1));
                writer.newLine();
            }
        }
    }

    private static int[] pair(String line) {
        String[] tokens = line.trim().split("\\s+");
        if (tokens.length != 2) {
            throw new IllegalArgumentException("Esperados exatamente dois inteiros: " + line);
        }
        return new int[] {Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1])};
    }

    private static String nextNonBlank(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.isBlank()) {
                return line;
            }
        }
        return null;
    }
}
