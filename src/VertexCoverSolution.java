import java.util.BitSet;
import java.util.stream.Collectors;

/** Valor imutável; cópias defensivas impedem que o backtracking altere uma solução salva. */
public final class VertexCoverSolution {
    private final BitSet selected;

    public VertexCoverSolution(BitSet selected) {
        this.selected = (BitSet) selected.clone();
    }

    public static VertexCoverSolution of(int... vertices) {
        BitSet selected = new BitSet();
        for (int vertex : vertices) {
            if (vertex < 0) {
                throw new IllegalArgumentException("Vértice negativo.");
            }
            selected.set(vertex);
        }
        return new VertexCoverSolution(selected);
    }

    public int size() {
        return selected.cardinality();
    }

    public boolean contains(int vertex) {
        return vertex >= 0 && selected.get(vertex);
    }

    public BitSet vertices() {
        return (BitSet) selected.clone();
    }

    public boolean isValid(Graph graph) {
        return graph.isVertexCover(this);
    }

    /** Identificadores base um, como nos arquivos de entrada. */
    @Override
    public String toString() {
        return selected.stream().mapToObj(vertex -> Integer.toString(vertex + 1))
                .collect(Collectors.joining(", ", "{", "}"));
    }
}
