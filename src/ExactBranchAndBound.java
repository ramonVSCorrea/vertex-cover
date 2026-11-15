import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.Deque;

/**
 * Branch and Bound binário por extremos de uma aresta residual.
 * Estados copiados e pilha explícita evitam restaurações incorretas e recursão profunda.
 * Não há estado compartilhado entre chamadas a solve.
 */
public final class ExactBranchAndBound {
    private record State(BitSet remaining, BitSet selected) { }

    public SolverResult solve(Graph graph, long timeoutMs) {
        if (timeoutMs < 0 || timeoutMs > Long.MAX_VALUE / 1_000_000L) {
            throw new IllegalArgumentException("Timeout em ms fora do intervalo suportado.");
        }
        long start = System.nanoTime();
        long budgetNs = timeoutMs * 1_000_000L;
        VertexCoverSolution best = new MatchingTwoApproximation().solve(graph).solution();
        long nodes = 0;
        long prunes = 0;
        if (graph.edgeCount() == 0) {
            return result(best, SolverResult.Status.OPTIMAL, start, nodes, prunes);
        }
        if (expired(start, budgetNs)) {
            return result(best, SolverResult.Status.TIME_LIMIT, start, nodes, prunes);
        }

        Edge[] edges = graph.edges().toArray(Edge[]::new);
        BitSet[] incident = new BitSet[graph.vertexCount()];
        for (int vertex = 0; vertex < incident.length; vertex++) {
            incident[vertex] = new BitSet();
            if ((vertex & 1023) == 0 && expired(start, budgetNs)) {
                return result(best, SolverResult.Status.TIME_LIMIT, start, nodes, prunes);
            }
        }
        for (int index = 0; index < edges.length; index++) {
            incident[edges[index].u()].set(index);
            incident[edges[index].v()].set(index);
            if ((index & 1023) == 0 && expired(start, budgetNs)) {
                return result(best, SolverResult.Status.TIME_LIMIT, start, nodes, prunes);
            }
        }
        BitSet initial = new BitSet(edges.length);
        initial.set(0, edges.length);
        Deque<State> pending = new ArrayDeque<>();
        pending.push(new State(initial, new BitSet(graph.vertexCount())));

        while (!pending.isEmpty()) {
            if (expired(start, budgetNs)) {
                return result(best, SolverResult.Status.TIME_LIMIT, start, nodes, prunes);
            }
            State state = pending.pop();
            nodes++;
            int selectedCount = state.selected().cardinality();
            if (state.remaining().isEmpty()) {
                if (selectedCount < best.size()) {
                    best = new VertexCoverSolution(state.selected());
                }
                continue;
            }
            if (selectedCount >= best.size()) {
                prunes++;
                continue;
            }

            int[] degrees = new int[graph.vertexCount()];
            BitSet matched = new BitSet(graph.vertexCount());
            int lowerBound = 0;
            int visited = 0;
            for (int i = state.remaining().nextSetBit(0); i >= 0;
                 i = state.remaining().nextSetBit(i + 1)) {
                Edge edge = edges[i];
                degrees[edge.u()]++;
                degrees[edge.v()]++;
                if (!matched.get(edge.u()) && !matched.get(edge.v())) {
                    matched.set(edge.u());
                    matched.set(edge.v());
                    lowerBound++;
                }
                if ((++visited & 1023) == 0 && expired(start, budgetNs)) {
                    return result(best, SolverResult.Status.TIME_LIMIT, start, nodes, prunes);
                }
            }
            // Qualquer cobertura residual usa pelo menos um extremo de cada aresta disjunta.
            if (selectedCount + lowerBound >= best.size()) {
                prunes++;
                continue;
            }
            Edge branchingEdge = null;
            long highestScore = -1;
            for (int i = state.remaining().nextSetBit(0); i >= 0;
                 i = state.remaining().nextSetBit(i + 1)) {
                Edge edge = edges[i];
                long score = (long) degrees[edge.u()] + degrees[edge.v()];
                if (score > highestScore) {
                    highestScore = score;
                    branchingEdge = edge;
                }
                if ((++visited & 1023) == 0 && expired(start, budgetNs)) {
                    return result(best, SolverResult.Status.TIME_LIMIT, start, nodes, prunes);
                }
            }
            int first = branchingEdge.u();
            int second = branchingEdge.v();
            if (degrees[second] > degrees[first]) {
                int temporary = first;
                first = second;
                second = temporary;
            }
            // LIFO: empilhar o menor grau primeiro faz o maior grau ser explorado antes.
            pending.push(select(state, second, incident[second]));
            pending.push(select(state, first, incident[first]));
        }
        // Apenas o esgotamento de todos os estados relevantes certifica a otimalidade.
        return result(best, SolverResult.Status.OPTIMAL, start, nodes, prunes);
    }

    private static State select(State parent, int vertex, BitSet incident) {
        BitSet remaining = (BitSet) parent.remaining().clone();
        remaining.andNot(incident);
        BitSet selected = (BitSet) parent.selected().clone();
        selected.set(vertex);
        return new State(remaining, selected);
    }

    private static boolean expired(long start, long budgetNs) {
        return System.nanoTime() - start >= budgetNs;
    }

    private static SolverResult result(VertexCoverSolution best, SolverResult.Status status,
                                       long start, long nodes, long prunes) {
        return new SolverResult(best, status, System.nanoTime() - start, nodes, prunes);
    }
}
