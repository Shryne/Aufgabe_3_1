package OptimizedFlow;

import ADTGraph.Graph;
import ADTGraph.Vertex;

import java.lang.reflect.Array;
import java.util.*;

import static OptimizedFlow.fordf.AdjacentPos.*;

/**
 * Created by remen on 13.12.15.
 */
public class fordf {
    enum AdjacentPos {
        PRE,
        SUCC,
    }

    //#####################################################
    /*
        Class to mark the vertices in the fordfulkerson method.
        It's the programmed version of (+vi, dj) or (-vi, dj).
     */
    //#####################################################
    private static class Mark {
        private final Vertex marked;
        private final Vertex adjacent;
        private final AdjacentPos adjacentPos;
        private final int flow;
        private boolean inspected;

        public Mark(Vertex source) {
            this(source, null, null, Integer.MAX_VALUE);
        }

        public Mark(Vertex marked, Vertex adjacent, AdjacentPos adjacentPos, int flow) {
            this.marked = marked;
            this.adjacent = adjacent;
            this.adjacentPos = adjacentPos;
            this.flow = flow;
        }

        @Override
        public String toString() {
            return "M[" + marked + "](" + adjacentPos + ", " + adjacent + ", " + flow + ")";
        }
    }

    // ####################################################
    // graph value names and helper
    // ####################################################
    private static final String FLOW_ARG_NAME = "flow";
    private static final String CAPACITY_ARG_NAME = "max";

    public static void main(String args[]) {
        Graph g = Graph.importG("graphs/graph_own_3");
        System.out.println(fordfulkerson(g, Vertex.createV("q"), Vertex.createV("s")));
    }

    /**
     * Uses the ford fulkerson algorithm to calculate the value of the
     * greatest flow.
     */
    public static int fordfulkerson(Graph graph, Vertex source, Vertex target) {
        // ################################################
        // init
        // ################################################
        ArrayList<Vertex> vertexes = graph.getVertexes();
        if (!preconditions(vertexes, source, target)) return 0;

        ArrayList<Mark> marked = new ArrayList<>();
        init(graph, source, marked);

        while (!allMarkedInspected(marked)) {
            inspectAndMark(target, marked, graph);
            augmentFlow(target, source, graph, marked);
        }

        //return end(target, graph);
        return 0;
    }

    /**
     * Give all edges a valid initial value (0) and mark the source with (undef, infinite)
     */
    private static void init(Graph graph, Vertex source, List<Mark> marked) {
        ArrayList<Vertex> edges = graph.getEdges();

        for (int i = 0; i < edges.size(); i += 2) { // TODO: Is size() or size() - 1 correct?
            Vertex localSource = edges.get(i);
            Vertex localTarget = edges.get(i + 1);
            graph.setAtE(localSource, localTarget, FLOW_ARG_NAME, 0);
        }

        marked.add(new Mark(source));
    }

    private static void inspectAndMark(Vertex target, List<Mark> marked, Graph graph) {
        for (int i = 0; /* i < marked.size()  &&*/ !isVertexMarked(target, marked); i++) { //TODO: commented code implement
            Vertex markedVertex = marked.get(i).marked;
            inspectVertex(markedVertex, marked, graph);

            setInspected(markedVertex, marked);
        }
    }

    private static void augmentFlow(Vertex actualVertex, Vertex source, Graph graph, List<Mark> marked) {
        final int crement = getMark(actualVertex, marked).flow;

        while (actualVertex != source) {
            List<Vertex> incidents = graph.getIncident(actualVertex);

            if (markedSuccessor(actualVertex, marked))
                for (int i = 0; i < incidents.size(); i += 2) {
                    Vertex localSource = incidents.get(i);
                    Vertex localTarget = incidents.get(i + 1);

                    if (localTarget == actualVertex) {
                        int oldFlow = graph.getValE(localSource, localTarget, FLOW_ARG_NAME);
                        graph.setAtE(localSource, localTarget, FLOW_ARG_NAME, oldFlow + crement);
                    }
                }
            else
                for (int i = 0; i < incidents.size(); i += 2) {
                    Vertex localSource = incidents.get(i);
                    Vertex localTarget = incidents.get(i + 1);

                    if (localTarget == actualVertex) {
                        int oldFlow = graph.getValE(localSource, localTarget, FLOW_ARG_NAME);
                        graph.setAtE(localSource, localTarget, FLOW_ARG_NAME, oldFlow - crement);
                    }
                }
            actualVertex = getMark(actualVertex, marked).adjacent;
        }
        removeMarks(marked, source);
    }

    private static int end(Vertex target, Graph graph) {
        ArrayList<Vertex> incidents = graph.getIncident(target);
        int flow = 0;

        for (int i = 0; i < incidents.size(); i += 2)
            flow += capacity(graph, incidents.get(i), incidents.get(i + 1));

        return flow;
    }

    private static void removeMarks(List<Mark> marked, Vertex source) {
        marked.clear();
        marked.add(new Mark(source));
    }

    private static Mark getMark(Vertex vertex, List<Mark> marked) {
        for (Mark m : marked)
            if (m.marked == vertex) return m;

        return null;
    }

    private static boolean markedSuccessor(Vertex vertex, List<Mark> marked) {
        return getMark(vertex, marked).adjacentPos == SUCC;
    }

    private static void setInspected(Vertex markedVertex, List<Mark> marked) {
        getMark(markedVertex, marked).inspected = true;
    }

    private static boolean allMarkedInspected(List<Mark> marked) {
        for (Mark m : marked)
            if (m.inspected == false) return false;

        return true;
    }

    private static void inspectVertex(Vertex markedVertex, List<Mark> marked, Graph graph) {
        List<Vertex> incidents = graph.getIncident(markedVertex);

        for (int i = 0; i < incidents.size(); i += 2) { // TODO: Is size() or size() - 1 correct?
            Vertex localSource = incidents.get(i);

            if (localSource == markedVertex) {
                if (!isFull(graph, localSource, incidents.get(i + 1))
                        && !isVertexMarked(incidents.get(i + 1), marked))

                    markForward(localSource, incidents.get(i + 1), marked, graph);

            } else {
                if (!isEmpty(graph, localSource, incidents.get(i + 1))
                        && !isVertexMarked(localSource, marked))

                    markBackward(incidents.get(i + 1), localSource, marked, graph);
            }
        }
    }

    private static void markForward(Vertex markedVertex, Vertex toMark, List<Mark> marked, Graph graph) {
        int di = getFlow(markedVertex, marked);
        int dj = Math.min(capacity(graph, markedVertex, toMark) - flow(graph, markedVertex, toMark),
                di);

        marked.add(new Mark(toMark, markedVertex, AdjacentPos.SUCC, dj));
    }

    private static void markBackward(Vertex markedVertex, Vertex toMark, List<Mark> marked, Graph graph) {
        int di = getFlow(markedVertex, marked);
        int dj = Math.min(flow(graph, toMark, markedVertex), di);

        marked.add(new Mark(toMark, markedVertex, AdjacentPos.PRE, dj));
    }

    private static boolean isVertexMarked(Vertex vertex, List<Mark> marked) {
        for (Mark m : marked)
            if (m.marked == vertex) return true;
        return false;
    }

    private static int getFlow(Vertex vertex, List<Mark> marked) {
        for (Mark m : marked)
            if (m.marked == vertex) return m.flow;
        throw new IllegalArgumentException(vertex + " is not marked: " + marked);
    }

    /**
     * Checks whether the preconditions for the {@link fordf#fordfulkerson(Graph, Vertex, Vertex)} method
     * are given or not.
     *
     * @return true if the preconditions are given and false if not.
     */
    private static boolean preconditions(ArrayList<Vertex> vertexes, Vertex source, Vertex target) {
        if (source == null || target == null) return false;
        if (source == target) return false;
        if (!vertexes.contains(source) || !vertexes.contains(target)) return false;
        return true;
    }



    /**
     * Like {@link fordf#fordfulkerson(Graph, Vertex, Vertex)}, but returns the
     * time needed for this algorithm.
     */
    public static long fordfulkersonRtm(Graph graph, Vertex source, Vertex target) {
        return 0;
    }

    /**
     * Like {@link fordf#fordfulkerson(Graph, Vertex, Vertex)}, but returns the
     * amount of accesses on the graph.
     */
    public static long fordfulkersonAcc(Graph graph, Vertex source, Vertex target) {
        return 0;
    }

    /*
    //#####################################################
    // returns the predecessor of the given vertex.
    //#####################################################
    private static void initMarker(HashMap<Vertex, Marker> marked, Vertex source) {
        marked.clear();
        marked.put(source, new Marker());
    }

    //#####################################################
    // returns the predecessor of the given vertex.
    //#####################################################
    private static void setCapacity(Graph graph, Vertex source, Vertex target, int capacity) {
        graph.setAtE(source, target, FLOW_ARG_NAME, capacity);
    }

    //#####################################################
    // returns the predecessor of the given vertex.
    //#####################################################
    private static Vertex getPredecessor(Vertex vertex, HashMap<Vertex, Marker> marked) {
        return marked.get(vertex).vertex;
    }

    /**
     * Method to inspect and/or mark the vertex in the entry and the
     * subsequent vertices.
     * The algorithm:
     *
     * For all forward edges consisting of the given vertex and
     * an unmarked, non full vertex, mark the unmarked vertex with
     * (given vertex, PRE, min(capacity(edge) - flow(edge), flow(given vertex)))
     *
     * For all backward edges consisting of the given vertex and
     * an unmarked, non empty vertex, mark the unmarked vertex with
     * (given vertex, SUCC, min(flow(edge), flow(given vertex)))
     */
    /*
    private static Vertex inspect(Graph graph, Map.Entry<Vertex, Marker> entry,
                                  HashMap<Vertex, Marker> marked, HashMap<Vertex, Marker> nextMarked) {
        Vertex vi = entry.getKey();
        ArrayList<Vertex> incidents = graph.getIncident(vi);

        for (int i = 0; i < incidents.size(); i += 2)
            if (incidents.get(i) == vi)
                inspectForward(graph, vi, incidents.get(i + 1), marked, nextMarked);
            else
                inspectBackward(graph, incidents.get(i + 1), vi, marked, nextMarked);

        return vi;
    }

    //#####################################################
    // method to inspect a backward edge.
    //#####################################################
    private static void inspectBackward(Graph graph, Vertex source, Vertex target,
                                        HashMap<Vertex, Marker> marked, HashMap<Vertex, Marker> nextMarked) {

        if (isMarked(marked, source) || isEmpty(graph, source, target)) return;

        int di = marked.get(target).flow;
        int dj = Math.min(flow(graph, source, target), di);

        mark(nextMarked, source, new Marker(PRE, target, dj));
    }

    //#####################################################
    // Marks the target with (+vi, dj) or (-vi, dj)
    //#####################################################
    private static void mark(HashMap<Vertex, Marker> container, Vertex toMark,
                             Marker marker) {
        container.put(toMark, marker);
    }

    //#####################################################
    // method to inspect a forward edge.
    //#####################################################
    private static void inspectForward(Graph graph, Vertex source, Vertex target,
                                       HashMap<Vertex, Marker> marked, HashMap<Vertex, Marker> nextMarked) {

        if (isMarked(marked, target) || isFull(graph, source, target)) return;

        int di = marked.get(source).flow;
        int dj = Math.min(capacity(graph, source, target) - flow(graph, source, target), di);

        nextMarked.put(target, new Marker(SUCC, source, dj));
    }

    //#####################################################
    /*
        Needed for the class "Marker" to distinguish between
        a predecessor vertex and a successor vertex.
        (You can mark a vertex with a one of them)
     */
    //#####################################################


    //#####################################################
    // Some shortcuts
    //#####################################################
    private static int capacity(Graph graph, Vertex source, Vertex target) {
        return graph.getValE(source, target, CAPACITY_ARG_NAME);
    }

    private static int flow(Graph graph, Vertex source, Vertex target) {
        return graph.getValE(source, target, FLOW_ARG_NAME);
    }

    private static boolean isFull(Graph graph, Vertex source, Vertex target) {
        return flow(graph, source, target) == capacity(graph, source, target);
    }

    private static boolean isEmpty(Graph graph, Vertex source, Vertex target) {
        return flow(graph, source, target) == 0;
    }
}
