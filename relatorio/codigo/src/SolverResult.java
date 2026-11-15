/** Tempo inclui preparação interna e, no exato, a aproximação que fornece o upper bound. */
public record SolverResult(VertexCoverSolution solution, Status status, long runtimeNs,
                           long nodesExplored, long prunedNodes) {
    public enum Status {
        OPTIMAL, TIME_LIMIT, APPROXIMATE
    }

    public SolverResult {
        if (solution == null || status == null || runtimeNs < 0 || nodesExplored < 0 || prunedNodes < 0) {
            throw new IllegalArgumentException("Resultado inválido.");
        }
    }

    public boolean optimalityProven() {
        return status == Status.OPTIMAL;
    }
}
