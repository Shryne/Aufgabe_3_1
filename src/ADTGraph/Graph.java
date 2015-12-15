package ADTGraph;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;

/**
 * Created by remen on 27.10.15.
 */
public class Graph {

    // ##########################################
    // vars
    // ##########################################
    public final boolean directedGraph;

    private ArrayList<Vertex> vertexes = new ArrayList<>();
    private ArrayList<ArrayList<String>> vertexNames = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> vertexValues = new ArrayList<>();

    private ArrayList<Edge> edges = new ArrayList<>();
    // ##########################################
    // methods
    // ##########################################

    /**
     * This private constructor makes importG a lot easier.
     */
    private Graph(boolean directed) { directedGraph = directed; }

    private Graph(Vertex vertex, boolean directed) {
        addVertex(vertex);
        directedGraph = directed;
    }

    public static Graph createG(Vertex vertex) {
        return createG(vertex, true);
    }

    public static Graph createG(Vertex vertex, boolean directed) {
        if (vertex == null) return null;
        return new Graph(vertex, directed);
    }

    public Graph addVertex(Vertex vertex) {
        if (vertex == null || vertexes.contains(vertex)) return this;
        vertexes.add(vertex);
        vertexNames.add(new ArrayList<>());
        vertexValues.add(new ArrayList<>());
        return this;
    }

    public Graph deleteVertex(Vertex vertex) {
        if (!vertexes.contains(vertex)) return this;
        if (vertexes.size() > 1) {
            deleteEdges(vertex);

            int vIndex = vertexes.indexOf(vertex);
            vertexNames.remove(vIndex);
            vertexValues.remove(vIndex);

            vertexes.remove(vertex);
        }
        return this;
    }

    public Graph addEdge(Vertex v1, Vertex v2) {
        if (vertexes.contains(v1) && vertexes.contains(v2))
            edges.add(new Edge(v1, v2));

        return this;
    }

    public Graph deleteEdge(Vertex v1, Vertex v2) {
        for (Edge e : edges)
            if (e.source == v1 && e.target == v2) {
                edges.remove(e);
                break;
            }
        return this;
    }

    public Graph setAtE(Vertex v1, Vertex v2, String name, int value) {
        if (!isCorrectName(name)) return this;
        for (Edge e : edges)
            if (e.source == v1 && e.target == v2) {
                if (e.getValue(name) == null) {
                    e.addAttribute(name, value);
                    break;
                }
                else {
                    int nIndex = e.names.indexOf(name);
                    e.values.set(nIndex, value);
                    break;
                }
            }

        return this;
    }

    public Graph setAtV(Vertex vertex, String name, int value) {
        if (vertex == null || name == null || name.equals("") || name.contains(" ")) return this;

        if (vertexes.contains(vertex)) {
            int vIndex = vertexes.indexOf(vertex);
            if (vIndex != -1) {
                int nIndex = vertexNames.get(vIndex).indexOf(name);
                if (nIndex != -1) {
                    vertexValues.get(vIndex).set(nIndex, value);
                } else {
                    addAttribute(vertex, name, value);
                }
            }
        }
        return this;
    }

    public static Graph importG(String filename) {
        if (filename == null || filename.isEmpty() || filename.matches("\\s") || filename.matches(".*,+?.*")) {
            return null;
        }

        Graph graph = null;
        Path inputFile = Paths.get(filename + ".graph");
        try (BufferedReader reader = Files.newBufferedReader(inputFile, StandardCharsets.UTF_8)) {
            boolean directedGraph = importGraphVariation(reader.readLine());

            ArrayList<Vertex> vertexes = new ArrayList<>();
            ArrayList<ArrayList<Integer>> values = new ArrayList<>();

            String line;
            while ((line = reader.readLine()) != null) {
                String parts[] = line.split(",");
                vertexes.add(Vertex.createV(parts[0]));
                vertexes.add(Vertex.createV(parts[1]));

                values.add(new ArrayList<>());
                for (int i = 2; i < parts.length; i++)
                    values.get(values.size() - 1).add(Integer.parseInt(parts[i]));
            }

            graph = new Graph(directedGraph);

            for (int i = 0; i < vertexes.size(); i += 2) {
                graph.addVertex(vertexes.get(i));
                graph.addVertex(vertexes.get(i + 1));

                graph.addEdge(vertexes.get(i), vertexes.get(i + 1));

                for (int j = 0; j < values.get(i / 2).size(); j++)
                    graph.setAtE(vertexes.get(i), vertexes.get(i + 1), "max", values.get(i / 2).get(j));
            }
        } catch (ArrayIndexOutOfBoundsException e){
            return null;
        } catch (IOException e) {
            return null;
        }

        return graph;
    }

    public File exportG(String filename) throws IOException {
        if(isCorrectName(filename)) throw new IOException();
        filename += ".txt";

        Path outputFile = Paths.get(filename);
        BufferedWriter writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8);

        String direction;
        if (directedGraph) {
            writer.write("digraph G { \n");
            direction = "->";
        } else {
            writer.write("graph G { \n");
            direction = "--";
        }

        for (Edge e : edges) {
            String attributes = "";
            for (int i = 0; i < e.names.size(); i++)
                attributes = vertexNames.get(i) + " " + vertexValues.get(i) + " ";

            writer.write(e.source.getName() + direction + e.target.getName() + "[label=\"" + attributes + "\"];\n");
        }

        writer.write("}\n");
        writer.close();
        return new File(filename);
    }

    public ArrayList<Vertex> getIncident(Vertex vertex) {
        ArrayList<Vertex> incidents = new ArrayList<>();

        for (Edge e : edges)
            if (e.source == vertex || e.target == vertex) {
                addTwo(incidents, e.source, e.target);
            } else if (!directedGraph)
                addTwo(incidents, e.target, e.source);

        return incidents;
    }

    public ArrayList<Vertex> getAdjacent(Vertex vertex) {
        ArrayList<Vertex> adjacent = new ArrayList<>();

        for (Edge e : edges)
            if      (e.source == vertex) adjacent.add(e.target);
            else if (e.target == vertex) adjacent.add(e.source);

        return adjacent;
    }

    public ArrayList<Vertex> getTarget(Vertex vertex) {
        ArrayList<Vertex> targets = new ArrayList<>();

        for (Edge e : edges)
            if (e.source == vertex)
                targets.add(e.target);
            else if (!directedGraph && e.target == vertex)
                targets.add(e.source);

        return targets;
    }

    public ArrayList<Vertex> getSource(Vertex vertex) {
        ArrayList<Vertex> sources = new ArrayList<>();

        for (Edge e : edges)
            if (e.target == vertex)
                sources.add(e.source);
            else if (!directedGraph && e.source == vertex)
                sources.add(e.target);

        return sources;
    }

    public ArrayList<Vertex> getEdges() {
        ArrayList<Vertex> edgeVertices = new ArrayList<>();

        for (Edge e : edges) {
            edgeVertices.add(e.source);
            edgeVertices.add(e.target);
            if (!directedGraph) {
                edgeVertices.add(e.target);
                edgeVertices.add(e.source);
            }
        }

        return edgeVertices;
    }

    public ArrayList<Vertex> getVertexes() {
        return new ArrayList<>(vertexes);
    }

    public int getValE(Vertex v1, Vertex v2, String name) {
        for (Edge e : edges)
            if (e.source == v1 && e.target == v2)
                return e.getValue(name) == null ? 0 : e.getValue(name);
            else if (!directedGraph && e.source == v2 && e.target == v1)
                return e.getValue(name) == null ? 0 : e.getValue(name);

        throw new IllegalArgumentException();
    }

    public int getValV(Vertex vertex, String name) {
        if (vertex == null || name == null) throw new IllegalArgumentException();

        int vertexIndex = vertexes.indexOf(vertex);
        if (vertexIndex == -1) throw new IllegalArgumentException();

        int attributeIndex = vertexNames.get(vertexIndex).indexOf(name);
        if (attributeIndex == -1) throw new IllegalArgumentException();

        return vertexValues.get(vertexIndex).get(attributeIndex);
    }
    // ##########################################
    // bonus
    // ##########################################
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (!(obj instanceof Graph)) return false;

        Graph other = (Graph) obj;
        if (directedGraph != ((Graph) obj).directedGraph) return false;
        if (!arrayEquals(getVertexes(), other.getVertexes())) return false;

        if (vertexes.size() != other.vertexes.size()) return false;
        for (int i = 0; i < vertexes.size(); i++) {
            if (vertexNames.get(i).size() != other.vertexNames.get(i).size()) return false;
            if (vertexValues.get(i).size() != other.vertexValues.get(i).size()) return false;

            if (!arrayEquals(vertexNames, other.vertexNames)) return false;
            if (!arrayEquals(vertexValues, other.vertexValues)) return false;
        }

        if (!arrayEquals(edges, other.edges)) return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder("G{");
        for (Edge e : edges) {
            output.append("\n\t" + e);

            if (!directedGraph) output.append("\n\t" + e.reverse());
        }

        return output.toString() + "\n}\n";
    }
    // ##########################################
    // invisible
    // ##########################################
    private void deleteEdges(Vertex vertex) {
        Iterator<Edge> i = edges.iterator();

        while (i.hasNext()) {
            Edge actualEdge = i.next();
            if (actualEdge.source == vertex || actualEdge.target == vertex)
                i.remove();
        }
    }

    private boolean containsEdge(Vertex v1, Vertex v2, boolean directedGraph) {
        if (directedGraph) return containsVertexCombo(v1, v2);

        return containsVertexCombo(v1, v2) || containsVertexCombo(v2, v1);
    }

    private boolean containsVertexCombo(Vertex v1, Vertex v2) {
        for (Edge e : edges)
            if (e.source == v1 && e.target == v2) return true;

        return false;
    }

    private void addTwo(List destiny, Object element1, Object element2) {
        destiny.add(element1);
        destiny.add(element2);
    }

    private void addAttribute(Vertex vertex, String name, int value) {
        int index = vertexes.indexOf(vertex);

        vertexNames.get(index).add(name);
        vertexValues.get(index).add(value);
    }

    private static boolean importGraphVariation(String string) {
        if (string.contains("#gerichtet")) return true;
        if (string.contains("#ungerichtet")) return false;
        throw new IllegalArgumentException("The input has to be #gerichtet or #ungerichtet, but is: " + string);
    }

    private static ArrayList<Vertex> importEdges(Scanner input) {
        ArrayList<Vertex> result = new ArrayList<>();

        while (input.hasNext())
            result.add(Vertex.createV(input.next()));


        return result;
    }

    private static boolean isCorrectName(String name) {
        return !(name == null || name.equals("") || name.contains(" ") || name.contains(","));
    }

    private static boolean arrayEquals(List<?> arr1, List<?> arr2) {
        List<?> arr1copy = new LinkedList<>(arr1);
        Iterator<?> i = arr2.iterator();

        while (i.hasNext()) {
            Object actualVertex = i.next();
            if (!arr1copy.contains(actualVertex)) return false;
            else arr1copy.remove(actualVertex);
        }
        return true;
    }

    private static boolean edgeEquals(List<Vertex> arr1, List<Vertex> arr2) {
        if (arr1.size() != arr2.size()) return false;

        Iterator<Vertex> i = arr1.iterator();
        while (i.hasNext()) {
            Vertex actualVertex = i.next();
            if (!arr2.contains(i.next())) return false;
            else arr2.remove(actualVertex);
        }
        return true;
    }

    private static List<Vertex> toList(Vertex... vertex) {
        return new LinkedList<>(Arrays.asList(vertex));
    }

    private class Edge {
        public final Vertex source;
        public final Vertex target;

        public ArrayList<String> names = new ArrayList<>();
        public ArrayList<Integer> values = new ArrayList<>();

        public Edge(Vertex source, Vertex target) {
            this.target = target;
            this.source = source;
        }

        public Edge reverse() {
            Edge newEdge = new Edge(target, source);
            newEdge.names = names;
            newEdge.values = values;
            return newEdge;
        }

        public void addAttribute(String name, int value) {
            names.add(name);
            values.add(value);
        }

        public Integer getValue(String name) {
            int attributeIndex = names.indexOf(name);
            if (attributeIndex == -1) return null;

            return values.get(attributeIndex);
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Edge)) return false;

            Edge other = (Edge) o;
            if (source != other.source || target != other.target) return false;
            if (!names.equals(other.names) || !values.equals(other.values)) return false;
            return true;
        }

        @Override
        public String toString() {
            String output = "E[" + source + ", " + target;
            assert names.size() == values.size();

            for (int i = 0; i < names.size(); i++)
                output += ", (" + names.get(i) + ": " + values.get(i) + ")";
            return output + "]";
        }
    }
}