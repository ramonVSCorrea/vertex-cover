/** Aresta simples não direcionada, com extremos normalizados (índices base zero). */
public record Edge(int u, int v) {
    public Edge {
        if (u < 0 || v < 0) {
            throw new IllegalArgumentException("Índices de vértices não podem ser negativos.");
        }
        if (u == v) {
            throw new IllegalArgumentException("Laços não são permitidos.");
        }
        if (u > v) {
            int temporary = u;
            u = v;
            v = temporary;
        }
    }
}
