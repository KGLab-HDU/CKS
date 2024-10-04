package code.exec;

import code.approximate.RWAvgSkewUB;
import code.approximate.RandWalk;
import code.graph.RWGraph;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;

public class CoraRW {
    public static void main(String[] args) throws IOException {
        RWGraph graph = new RWGraph("./cora.cites");
        HashSet<Integer> query = new HashSet<>();
        query.add(35);
        int scope = 7;
        int topN = 20;
        double alpha = 1.0;
        int it = 150;
        RandWalk rw = new RWAvgSkewUB(graph, query, scope, topN, alpha);
        rw.selectNodes();
        rw.computePossibility();
        LinkedList<Integer> keyMember = rw.TopN(it);// 裸跑
        System.out.println(keyMember);
    }
}
