package code.approximate;

import code.graph.RWGraph;

import java.util.HashMap;
import java.util.HashSet;

public class RWAvgSkew extends RandWalk {

    public RWAvgSkew(RWGraph G, HashSet<Integer> source, int scope, int topN, double alpha) {
        super(G, source, scope, topN);
        this.alpha = alpha;
    }

    public RWAvgSkew(){
        super();
    }


    public double avgRefine(double skew, double alpha) {
        double e = Math.pow(Math.E, skew);
        double result = 1 + alpha * (1.0 / (1 + e) - 0.5);
        return result;
    }

    @Override
    public void computePossibility() {
        this.edgeNum = 0;
        this.transitionMatrix.clear();
        long startTime = System.currentTimeMillis();
        if (this.node2index.size() < this.topN) {//top-n过大
            this.topN = this.node2index.size();
        }
        HashSet<Integer> nodes = new HashSet<>();
        nodes.addAll(this.node2index.keySet());
        double p;
        for (int i : nodes) {//遍历游走范围内的点
            double supSum = 0;
            HashMap<Integer, Double> tmp = new HashMap<>();//临时记录邻居j的sup
            this.transitionMatrix.put(i, new HashMap<>());
            for (int j : this.G.getGraph().get(i)) {
                if (nodes.contains(j)) {
                    double sup = this.G.getSup(i, j);
                    double skewj = this.avgRefine(this.G.getNodeSupSkew().get(j), this.alpha);
                    double skewi = this.avgRefine(this.G.getNodeSupSkew().get(i), this.alpha);
                    sup /= (skewi * this.G.getNodeAvgSup().get(i)) / (skewj * this.G.getNodeAvgSup().get(j));
                    tmp.put(j, sup);
                    supSum += sup;
                    this.edgeNum++;
                }
            }
            for (int j : tmp.keySet()) {
                //邻边的support都是0, 等概率转移
                if (supSum == 0) {
                    p = 1.0 / tmp.size();
                } else {
                    p = tmp.get(j) / supSum;
                }
                this.transitionMatrix.get(i).put(j, p);
            }
        }
        this.edgeNum /= 2;
        long endTime = System.currentTimeMillis();
        logger.debug("转移概率计算，耗时: " + (double) (endTime - startTime) / 1000);
    }

}
