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
            return "M(" + adjacentPos + ", " + adjacent + ", " + flow + ")";
        }
    }

    // ####################################################
    // graph value names and helper
    // ####################################################
    private static final String FLOW_ARG_NAME = "flow";
    private static final String CAPACITY_ARG_NAME = "max";

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
            inspectAndMark(source, marked, graph);
        }


        return 0;
        /*
        // ################################################
        // init
        // ################################################
        if (graph == null) return 0;

        ArrayList<Vertex> vertexes = graph.getVertexes();
        if (!preconditions(vertexes, source, target)) return 0;

        ArrayList<Vertex> edges = graph.getEdges();
        for (int i = 0; i < edges.size(); i += 2)
            setCapacity(graph, edges.get(i), edges.get(i + 1), 0);

        //TODO What about backward edges?

        HashMap<Vertex, Marker> marked = new HashMap<>();

        ArrayList<Vertex> marked1 = new ArrayList<>();
        ArrayList<Vertex> inspected = new ArrayList<>();

        while (inspected.size() < vertexes.size()) {

            initMarker(marked, source);
            // ################################################
            // inspect and mark
            // ################################################

            while (!marked.containsKey(target)) {

                Iterator<Map.Entry<Vertex, Marker>> iterator = marked.entrySet().iterator();
                HashMap<Vertex, Marker> actualMarked = new HashMap<>();
                HashMap<Vertex, Marker> nextMarked = new HashMap<>();

                while (iterator.hasNext()) {
                    Map.Entry<Vertex, Marker> entry = iterator.next();
                    inspected.add(inspect(graph, entry, marked, nextMarked));
                    iterator.remove();
                }

                marked = nextMarked;
                //System.out.println(nextMarked);
            }

            while (!marked1.contains(target)) {

                for (int i = 0; i < marked1.size(); i++) {

                }
            }

            // ################################################
            // enlarge
            // ################################################
            Vertex actualVertex = target;
            int crement = marked.get(target).flow;

            while (actualVertex != source) {
                ArrayList<Vertex> incidents = graph.getIncident(actualVertex);
                for (int i = 0; i < incidents.size(); i += 2) {
                    Vertex s = incidents.get(i);
                    Vertex t = incidents.get(i + 1);

                    if (t == actualVertex) // forward edge
                        graph.setAtE(s, t, FLOW_ARG_NAME, graph.getValE(s, t, FLOW_ARG_NAME) + crement);
                    else // backward edge
                        graph.setAtE(s, t, FLOW_ARG_NAME, graph.getValE(s, t, FLOW_ARG_NAME) - crement);
                }
                actualVertex = getPredecessor(target, marked);
            }
            System.out.println("HEY");
        }

        // ################################################
        // end
        // ################################################
        ArrayList<Vertex> incidents = graph.getIncident(target);
        int flow = 0;

        for (int i = 0; i < incidents.size(); i += 2)
            flow += capacity(graph, incidents.get(i), incidents.get(i + 1));

        return flow;
        */
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

    private static void inspectAndMark(Vertex source, List<Mark> marked, Graph graph) {
        for (int i = 0; i < marked.size() || marked.contains(source); i++) {
            Vertex markedVertex = marked.get(i).marked;
            inspectVertex(markedVertex, marked, graph);
        }
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
                        && !isVertexMarked(localSource, marked))

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
        int dj = Math.min(flow(graph, markedVertex, toMark), di);

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
