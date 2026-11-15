import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/** Matching maximal guloso. Nunca confundir maximal (não extensível) com máximo. */
public final class MatchingTwoApproximation {
    public record Result(SolverResult solverResult, List<Edge> matching) {
        public Result {
            matching = List.copyOf(matching);
        }
    }

    public SolverResult solve(Graph graph) {
        return solveWithMatching(graph).solverResult();
    }

    public Result solveWithMatching(Graph graph) {
        long start = System.nanoTime();
        BitSet selected = new BitSet(graph.vertexCount());
        List<Edge> matching = new ArrayList<>();
        for (Edge edge : graph.edges()) {
            if (!selected.get(edge.u()) && !selected.get(edge.v())) {
                matching.add(edge);
                selected.set(edge.u());
                selected.set(edge.v());
            }
        }
        VertexCoverSolution solution = new VertexCoverSolution(selected);
        return new Result(new SolverResult(solution, SolverResult.Status.APPROXIMATE,
                System.nanoTime() - start, 0, 0), matching);
    }
}
