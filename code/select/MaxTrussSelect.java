package code.select;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import code.baseline.TDBottomUp;
import code.baseline.UniComponent;
import code.graph.Graph;
import code.service.GraphSerializer;
import code.util.DeepCopy;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 选择最大truss的最大连通分量周围的点
 */
public class MaxTrussSelect {
    private final static Logger logger = Logger.getLogger("InfoLogger");

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        String file = args[0];//graph
        int scope = Integer.parseInt(args[1]);//scope

        FileAppender fileAppender = (FileAppender) logger.getAppender("I");
        fileAppender.setFile("query/" + file + "_MTS_scope" + scope + ".txt");
        fileAppender.activateOptions();
        fileAppender = (FileAppender) Logger.getRootLogger().getAppender("D");
        fileAppender.setFile("query/" + "$debug_" + file + "_MTS_scope" + scope + ".log");
        fileAppender.activateOptions();

        Graph G = GraphSerializer.antiSerialize("./graph/" + file + ".dat");
        TDBottomUp tdBottomUp = new TDBottomUp("./graph/" + file + ".dat", true);
        HashMap<Integer, HashSet<Integer>> tmpGraph = null;
        int k;
        for (k = 2; ; k++) {
            tdBottomUp.getTruss(k);
            if (tdBottomUp.getG().getNodenum() == 0)
                break;
            //暂存当前graph
            tmpGraph = (HashMap<Integer, HashSet<Integer>>) DeepCopy.copy(tdBottomUp.getG().getGraph());
        }
        UniComponent uniComponent = new UniComponent(tmpGraph);
        HashMap<Integer, HashSet<Integer>> compo = uniComponent.getComponent();
        AtomicInteger maxCompoNum = new AtomicInteger();
        compo.forEach((key, val) -> {
            if (compo.get(key).size() > compo.get(maxCompoNum.intValue()).size())
                maxCompoNum.set(key);
        });
        HashSet<Integer> maxCompo = compo.get(maxCompoNum.intValue());
        HashSet<Integer> neibors = G.nodeInScope(maxCompo, scope);
        LinkedList<Map.Entry<Integer, Double>> distSort = G.getNodeDistSort(maxCompo, neibors);
        LinkedList<Integer> candidate = new LinkedList<>();
        distSort.forEach((o) -> {
            candidate.add(o.getKey());
        });
        logger.info(candidate);
        logger.debug(candidate.size());
    }
}
