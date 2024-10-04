package code.runnable;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import code.graph.RWGraph;
import code.service.RWGraphSerializer;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

public class TrussnessDistribution {
    private final static Logger logger = Logger.getLogger("InfoLogger");

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        String file = args[0];
        FileAppender fileAppender = (FileAppender) logger.getAppender("I");
        fileAppender.setFile("./distribution/" + file + ".log");
        fileAppender.activateOptions();
        fileAppender = (FileAppender) Logger.getRootLogger().getAppender("D");
        fileAppender.setFile("./distribution/" + "$debug_" + file + ".log");
        fileAppender.activateOptions();

        RWGraph g = RWGraphSerializer.antiSerialize("./rwgraph/" + file + ".dat");
        HashMap<Integer, HashSet<Integer>> distribution = new HashMap<>();
        HashMap<Integer, HashMap<Integer, Integer>> trussness = g.getTrussness();
        HashMap<Integer, HashSet<Integer>> graph = g.getGraph();
        //先计算点的trussness
        for (int i : graph.keySet()) {
            int maxTruss = 0;
            for (int j : graph.get(i)) {//找到邻边中trussness最大值
                if (maxTruss < trussness.get(i).get(j))
                    maxTruss = trussness.get(i).get(j);
            }
            if (distribution.get(maxTruss) == null) distribution.put(maxTruss, new HashSet<>());
            distribution.get(maxTruss).add(i);
        }

        for (int k : distribution.keySet()) {
            logger.info("trussness-" + k + ": " + distribution.get(k));
            logger.info("size:" + distribution.get(k).size());
        }

    }
}
