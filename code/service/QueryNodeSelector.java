package code.service;

import code.baseline.TDBottomUp;
import code.graph.Graph;
import code.util.DeepCopy;

import java.io.IOException;
import java.util.*;

public class QueryNodeSelector {
    private Graph G;

    private TDBottomUp tdBottomUp;

    private final LinkedList<Integer> candidate = new LinkedList<>();

    /**
     * 找点方法1：找最大核外面一跳邻居
     * @param gFilePath
     * @throws IOException
     * @throws InterruptedException
     * @throws ClassNotFoundException
     */
    public QueryNodeSelector(String gFilePath) throws IOException, InterruptedException, ClassNotFoundException {
        this.G = new Graph(gFilePath);
        tdBottomUp = new TDBottomUp(this.G.getGraph());
        HashMap<Integer, HashSet<Integer>> tmpGraph = null;
        int k;
        for (k = 2; ; k++){
            tdBottomUp.getTruss(k);
            if (tdBottomUp.getG().getNodenum() == 0)
                break;
            //暂存当前graph
            tmpGraph = (HashMap<Integer, HashSet<Integer>>) DeepCopy.copy(tdBottomUp.getG().getGraph());
        }
        HashSet<Integer> kmSet = new HashSet<>();
        kmSet.addAll(tmpGraph.keySet());
        HashSet<Integer> neibors = this.G.nodeInScope(kmSet, 1);
        LinkedList<Map.Entry<Integer, Double>> distSort = this.G.getNodeDistSort(kmSet, neibors);
        for (Map.Entry<Integer, Double> distPair : distSort) {
            this.candidate.add(distPair.getKey());
        }
    }

    public LinkedList<Integer> getCandidate(int n) {
        LinkedList<Integer> res = new LinkedList<>();
        Iterator<Integer> it = this.candidate.iterator();
        if (n > this.candidate.size())
            n = this.candidate.size();
        while (n-- > 0) {
            res.add(it.next());
        }
        return res;
    }

    public LinkedList<Integer> getCandidate() {
        return this.candidate;
    }
}
