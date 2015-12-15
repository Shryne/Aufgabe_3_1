package OptimizedFlow.tests;

import ADTGraph.Graph;
import ADTGraph.Vertex;
import OptimizedFlow.fordf;
import org.junit.Test;

/**
 * Created by remen on 13.12.15.
 */
public class FlowTest {
    private static final String GRAPH_PATH = "graphs/";

    @Test
    public void testEasy() {

    }

    @Test
    public void testNormal() {
        Graph g = Graph.importG(GRAPH_PATH + "graph_own_3");
        fordf.fordfulkerson(g, v("q"), v("s"));
        //System.out.println(g);
        //System.out.println(fordf.fordfulkerson(g, v("q"), v("s")));
        //System.out.println(g);
    }

    @Test
    public void testFailure() {

    }

    //#####################################################
    // alias
    //#####################################################
    private static Vertex v(String name) {
        return Vertex.createV(name);
    }
}
