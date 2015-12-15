package OptimizedFlow;

import ADTGraph.Graph;
import ADTGraph.Vertex;

/**
 * Created by remen on 13.12.15.
 */
public class maxflow {
    public enum FlowAlgorithm {
        FORD_FULKERSON,
        EDMONDS_KARP,
    }

    /**
     * Uses the ford fulkerson or the edmonds & karb algorithm to calculate the value of the
     * greatest flow.
     *
     * @param variant if (variant == 1) fulkerson
     *                else if (variant == 2) edmonds & karb
     *                else 0
     */
    public int findMaxFlow(Graph graph, Vertex source, Vertex target, FlowAlgorithm variant) {
        switch (variant) {
            case FORD_FULKERSON: return fordf.fordfulkerson(graph, source, target);
            case EDMONDS_KARP: return edmondsk.edmondskarp(graph, source, target);
            default: return 0;
        }
    }
}
