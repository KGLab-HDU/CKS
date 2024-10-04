package code.runnable;

import code.graph.Graph;
import code.graph.RWGraph;
import code.service.GraphSerializer;
import code.service.RWGraphSerializer;

import java.io.IOException;

public class GraphSerialize {
    //序列化graph，方便快速读取图
    public static void main(String[] args) throws IOException, InterruptedException {
        String graph = args[0];
        Graph g = new Graph(graph + ".txt");
        RWGraph rg = new RWGraph(graph + ".txt");
        rg.computeTrussness();
        GraphSerializer.serialize(g, "./graph/" + graph + ".dat");
        RWGraphSerializer.serialize(rg, "./rwgraph/" + graph + ".dat");
    }
}
