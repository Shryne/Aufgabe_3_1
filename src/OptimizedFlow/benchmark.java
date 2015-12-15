package OptimizedFlow;

import ADTGraph.Graph;
import ADTGraph.Vertex;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by remen on 13.12.15.
 */
public class benchmark {
    //#####################################################
    // output
    //#####################################################
    private static final String EXPORT_FILENAME = "results_01.csv";
    private static final String DELIMETER = ";";

    private static final String TITLES[] = {
            "Ford Fulkerson",
            "Edmonds & Karb"
    };

    private static final String COLUMN_NAMES[] = {
            "graph", "time", "access"
    };
    //#####################################################
    // methods to be benchmarked
    //#####################################################
    private static final int METHOD_AMOUNT = 2;
    private static final FlowMethod RTM_METHODS[] = {
            ((graph, source, target) -> fordf.fordfulkersonRtm(graph, source, target)),
            ((graph, source, target) -> edmondsk.edmondskarpRtm(graph, source, target)),
    };

    private static final FlowMethod ACC_METHODS[] = {
            ((graph, source, target) -> fordf.fordfulkersonAcc(graph, source, target)),
            ((graph, source, target) -> edmondsk.edmondskarpAcc(graph, source, target)),
    };

    static {
        // They have to be equal in size
        assert METHOD_AMOUNT == RTM_METHODS.length;
        assert METHOD_AMOUNT == ACC_METHODS.length;
        assert METHOD_AMOUNT == TITLES.length;
        assert METHOD_AMOUNT == COLUMN_NAMES.length - 1;
    }
    //#####################################################
    // input for the methods
    //#####################################################
    private static String FILEBASE = "graph_";
    private static String PATH = "graphs/";

    private static final int GRAPH_AMOUNT = 14;
    public static final String INPUT_FILENAMES[] = new String[GRAPH_AMOUNT];
    static {
        for (int i = 1; i <= INPUT_FILENAMES.length; i++)
            INPUT_FILENAMES[i - 1] = FILEBASE + i;
    }

    private static final Graph GRAPHS[] = new Graph[GRAPH_AMOUNT];
    static {
        for (int i = 0; i < GRAPH_AMOUNT; i++)
            GRAPHS[i] = Graph.importG(PATH + INPUT_FILENAMES[i]);
    }

    //#####################################################
    // the benchmark method
    //#####################################################
    public static void main(String args[]) {
        StringBuilder output = new StringBuilder();

        for (int i = 0; i < METHOD_AMOUNT; i++) {
            String title = TITLES[i];
            StringBuilder firstLine = getFirstLine(INPUT_FILENAMES);
            StringBuilder time = calcLine(GRAPHS, RTM_METHODS[i], COLUMN_NAMES[0]);
            StringBuilder accesses = calcLine(GRAPHS, ACC_METHODS[i], COLUMN_NAMES[1]);

            output.append(title + "\n" + firstLine + "\n" + time + "\n" + accesses + "\n\n");
        }

        writeFile(EXPORT_FILENAME, output.toString());
    }

    /**
     * Creates the title Line. This will probably look like this:
     * graph_1;graph_2;graph_3; ...
     *
     * It will be the first line after the title.
     */
    private static StringBuilder getFirstLine(String graphNames[]) {
        return createLine(graphNames).insert(0, COLUMN_NAMES[0]);
    }

    /**
     * Creates the other lines for the table. It will use the given method to get the results.
     * This will probably look like this:
     *
     * columnName;31032;2302;493;2301;24983; ...
     *
     * The columnName is "time" or "access" at the moment.
     */
    private static StringBuilder calcLine(Graph graphs[], FlowMethod method, String columnName) {
        Long result[] = new Long[GRAPH_AMOUNT];

        for (int i = 0; i < GRAPH_AMOUNT; i++) {
            ArrayList<Vertex> vertexes = graphs[i].getVertexes();

            for (Vertex v1 : vertexes)
                for (Vertex v2 : vertexes)
                    result[i] += method.apply(graphs[i], v1, v2);
        }

        return createLine(result).insert(0, columnName);
    }

    /**
     * Takes an array and converts it into a String with one line with
     * the DELIMENTER between the elements.
     *
     * {1, 2, 3} -> "1;2;3"
     */
    private static StringBuilder createLine(Object content[]) {
        StringBuilder result = new StringBuilder();
        for (Object o : content)
            result.append(o + DELIMETER);

        return result;
    }

    /**
     * Interface for the lambdas.
     */
    private interface FlowMethod {
        long apply(Graph graph, Vertex source, Vertex target);
    }

    /**
     * Creates a file with the given filename and writes the given
     * String into it.
     */
    private static void writeFile(String filename, String output) {
        try {
            Files.write(Paths.get(filename), output.getBytes());
        } catch (IOException e) {}
    }
}
