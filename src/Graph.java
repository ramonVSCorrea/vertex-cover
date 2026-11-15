import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Grafo simples. A ordem de inserção das arestas é preservada para reprodutibilidade. */
public final class Graph {
    private final int vertexCount;
    private final List<Edge> edges = new ArrayList<>();
    private final Set<Edge> uniqueEdges = new HashSet<>();
    private final List<List<Integer>> adjacency = new ArrayList<>();

    public Graph(int vertexCount) {
        if (vertexCount < 0) {
            throw new IllegalArgumentException("Número de vértices negativo.");
        }
        this.vertexCount = vertexCount;
        for (int vertex = 0; vertex < vertexCount; vertex++) {
            adjacency.add(new ArrayList<>());
        }
    }

    public void addEdge(int u, int v) {
        checkVertex(u);
        checkVertex(v);
        Edge edge = new Edge(u, v);
        if (!uniqueEdges.add(edge)) {
            throw new IllegalArgumentException("Aresta duplicada: " + edge);
        }
        edges.add(edge);
        adjacency.get(u).add(v);
        adjacency.get(v).add(u);
    }

    public int vertexCount() {
        return vertexCount;
    }

    public int edgeCount() {
        return edges.size();
    }

    public List<Edge> edges() {
        return Collections.unmodifiableList(edges);
    }

    public List<Integer> neighbors(int vertex) {
        checkVertex(vertex);
        return Collections.unmodifiableList(adjacency.get(vertex));
    }

    public int degree(int vertex) {
        return neighbors(vertex).size();
    }

    public double density() {
        return vertexCount < 2 ? 0.0 : 2.0 * edgeCount() / ((double) vertexCount * (vertexCount - 1));
    }

    public List<Integer> isolatedVertices() {
        List<Integer> isolated = new ArrayList<>();
        for (int vertex = 0; vertex < vertexCount; vertex++) {
            if (degree(vertex) == 0) {
                isolated.add(vertex);
            }
        }
        return List.copyOf(isolated);
    }

    public boolean isVertexCover(VertexCoverSolution solution) {
        if (solution.vertices().length() > vertexCount) {
            return false;
        }
        for (Edge edge : edges) {
            if (!solution.contains(edge.u()) && !solution.contains(edge.v())) {
                return false;
            }
        }
        return true;
    }

    private void checkVertex(int vertex) {
        if (vertex < 0 || vertex >= vertexCount) {
            throw new IllegalArgumentException("Vértice fora de [0, " + vertexCount + "): " + vertex);
        }
    }
}
