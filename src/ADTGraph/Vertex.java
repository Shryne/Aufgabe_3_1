package ADTGraph;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by remen on 27.10.15.
 */
public class Vertex {
    // ##########################################
    // vars
    // ##########################################
    private final String vertexName;

    private static final ArrayList<Vertex> vertexes = new ArrayList<>();
    // ##########################################
    // methods
    // ##########################################
    private Vertex(String name) {
        vertexes.add(this);
        vertexName = name;
    }

    public static Vertex createV(String name) {
        if (!isCorrectName(name)) return null;

        Vertex output = getVertex(name);
        return (output == null) ? (new Vertex(name)) : (output);
    }

    public String getName() { return vertexName; }
    // ##########################################
    // bonus
    // ##########################################
    @Override
    public String toString() {
        return "V(" + vertexName + ")";
    }
    // ##########################################
    // invisible
    // ##########################################
    private static Vertex getVertex(String name) {
        for (Vertex v : vertexes)
            if (v.getName().equals(name)) return v;

        return null;
    }

    private static boolean isCorrectName(String name) {
        return !(name == null || name.equals("") || name.contains(" ") || name.contains(","));
    }
}
