package code.graph;

import org.apache.log4j.Logger;
import code.baseline.TDBottomUp;
import code.util.DeepCopy;

import java.io.IOException;
import java.io.Serial;
import java.util.*;


public class RWGraph extends Graph {
    @Serial
    private static final long serialVersionUID = -8493711507905521207L;
    //图属性
    double maxSkew = 0.0;
    double minSkew = 0.0;
    protected HashMap<Integer, Double> nodeAvgSup;//Average Support of node
    protected HashMap<Integer, Double> nodeSupSkew;//Support Skew of node
    protected HashMap<Integer, Integer> nodeMaxTrussness;//node的trussness上限
    protected HashMap<Integer, HashMap<Integer, Integer>> edgeMaxTrussness;//edge的trussness上限

    protected HashMap<Integer, HashMap<Integer, Integer>> trussness;

    private final static Logger logger = Logger.getLogger("InfoLogger");

    public RWGraph(LinkedList<Integer> nodes,
                   HashMap<Integer, HashSet<Integer>> graph,
                   int nodenum,
                   int edgenum,
                   int maxSup,
                   int triNum,
                   int maxDeg,
                   double maxSkew,
                   double minSkew,
                   String gFilePath,
                   HashMap<Integer, HashMap<Integer, Integer>> sup,
                   HashMap<Integer, Double> nodeAvgSup,
                   HashMap<Integer, Double> nodeSupSkew,
                   HashMap<Integer, Integer> nodeMaxTrussness,
                   HashMap<Integer, HashMap<Integer, Integer>> edgeMaxTrussness) throws IOException, InterruptedException {
        super(nodes, graph, nodenum, edgenum, maxSup, triNum, maxDeg, gFilePath, sup);
        this.maxSkew = maxSkew;
        this.minSkew = minSkew;
        this.nodeAvgSup = (HashMap<Integer, Double>) DeepCopy.copy(nodeAvgSup);
        this.nodeSupSkew = (HashMap<Integer, Double>) DeepCopy.copy(nodeSupSkew);
        this.nodeMaxTrussness = (HashMap<Integer, Integer>) DeepCopy.copy(nodeMaxTrussness);
        this.edgeMaxTrussness = (HashMap<Integer, HashMap<Integer, Integer>>) DeepCopy.copy(edgeMaxTrussness);
    }

    public RWGraph(String gFilePath) throws IOException {
        super(gFilePath);//sup, maxsup, nodenum, edgenum, graph, maxdeg, trinum, nodes
        this.nodeAvgSup = new HashMap<>();
        this.nodeSupSkew = new HashMap<>();
        this.nodeMaxTrussness = new HashMap<>();
        this.edgeMaxTrussness = new HashMap<>();
        this.computeNodeAvgSupport();//Avg Support
        logger.debug("节点平均support计算完成");
        this.computeNodeMaxTrussness();//node Support upper bound
        logger.debug("节点Trussness上限计算完成");
        this.computeEdgeMaxTrussness();//edge Support upper bound
        logger.debug("边Trussness上限计算完成");
        this.computeNodeSupSkew();//node Support Skew
    }
    public RWGraph(HashMap<Integer, HashSet<Integer>> $graph) throws IOException, InterruptedException {
        super($graph);
        this.nodeAvgSup = new HashMap<>();
        this.nodeSupSkew = new HashMap<>();
        this.nodeMaxTrussness = new HashMap<>();
        this.edgeMaxTrussness = new HashMap<>();
        this.computeNodeAvgSupport();//Avg Support
        logger.debug("节点平均support计算完成");
        this.computeNodeMaxTrussness();//node Support upper bound
        logger.debug("节点Trussness上限计算完成");
        this.computeEdgeMaxTrussness();//edge Support upper bound
        logger.debug("边Trussness上限计算完成");
        this.computeNodeSupSkew();//node Support Skew
    }

    //计算trussness for every edge
    public void computeTrussness() throws IOException, InterruptedException {
        TDBottomUp tdButtomUp = new TDBottomUp(this.graph);
        for (int i = 2; ; i++) {
            tdButtomUp.getTruss(i);
            if (tdButtomUp.getG().getEdgenum() == 0)
                break;
        }
        this.trussness = tdButtomUp.getTrussness();
    }

    //avgSup
    public void computeNodeAvgSupport() {
        for (int i : this.graph.keySet()) {
            double sum = 0;
            int size = this.graph.get(i).size();//degree
            for (int j : this.graph.get(i)) {
                sum += this.sup.get(i).get(j);
            }
            if (sum == 0)
                this.nodeAvgSup.put(i, 0.1);
            else
                this.nodeAvgSup.put(i, sum / size);
        }
    }

    //maxSkew, minSkew, Skew
    public void computeNodeSupSkew() {
        //int maxSkewNode = 0;
        //int minSkewNode = 0;
        double min = Double.MIN_VALUE;
        double max = -Double.MAX_VALUE;
        long startTime = System.currentTimeMillis();

        for (int i : this.graph.keySet()){
            if (this.graph.get(i).size() <= 1){//degree <= 1则直接赋值
                this.nodeSupSkew.put(i, 0.0);
            }
            else{
                double avgSup = this.nodeAvgSup.get(i);
                double var = 0.0;//标准差辅助
                double standard = 0.0;//标准差
                for (int j : this.graph.get(i)){
                    var += Math.pow(this.sup.get(i).get(j) - avgSup, 2);
                }
                standard = Math.sqrt(var / (this.graph.get(i).size() - 1));
                if (standard == 0){
                    this.nodeSupSkew.put(i, 0.0);
                }
                else{
                    double skew = 0.0;//skew期望（在这里为平均值）
                    double skewSum = 0.0;
                    for (int j : this.graph.get(i)){
                        skewSum += Math.pow((this.sup.get(i).get(j) - avgSup) / standard, 3);
                    }
                    skew = skewSum / this.graph.get(i).size();
                    this.nodeSupSkew.put(i, skew);
                    if (skew > max){
                        //maxSkewNode = i;
                        max = skew;
                    }
                    if (skew < min){
                        //minSkewNode = i;
                        min = skew;
                    }
                }
            }
        }
        this.maxSkew = max;
        this.minSkew = min;

        long endTime = System.currentTimeMillis();
        logger.debug("Skew计算完成，耗时: " + (double) (endTime - startTime) / 1000);
    }

    //再次做了修改，详见ipad笔记
    public void computeNodeMaxTrussness() {
        for(int node : this.graph.keySet()){
            ArrayList<Integer> nei = new ArrayList<>();
            for(int n : this.graph.get(node)){
                nei.add(this.sup.get(node).get(n));// 存放邻居support
            }
            nei.sort((o1, o2) -> o2 - o1);
            boolean flag = false;
            for(int i = 0; i < nei.size(); i++){
                if(nei.get(i) < i){
                    this.nodeMaxTrussness.put(node, i + 1);
                    flag = true;
                    break;
                }
            }
            if (flag == false) {
                this.nodeMaxTrussness.put(node, nei.size() + 1);
            }
        }
    }

    public void computeEdgeMaxTrussness() {
        for (int i : this.sup.keySet()){
            int node1 = this.nodeMaxTrussness.get(i);
            HashMap<Integer, Integer> edgeMaxTrussness = new HashMap<>();//i到j这条边的maxtrussness
            for (int j : this.sup.get(i).keySet()){
                int node2 = this.nodeMaxTrussness.get(j);
                int nodeMaxTruss = node1 > node2 ? node2 : node1;
                int support = this.sup.get(i).get(j);
                edgeMaxTrussness.put(j, support + 2 > nodeMaxTruss ? nodeMaxTruss : support + 2);
            }
            this.edgeMaxTrussness.put(i, edgeMaxTrussness);
        }
    }

    public RWGraph copyGraph() throws IOException, InterruptedException {
        return (RWGraph) DeepCopy.copy(this);
    }

    //getter
    public double getMaxSkew() {
        return maxSkew;
    }

    public void setMaxSkew(double maxSkew) {
        this.maxSkew = maxSkew;
    }

    public double getMinSkew() {
        return minSkew;
    }

    public void setMinSkew(double minSkew) {
        this.minSkew = minSkew;
    }

    public HashMap<Integer, Double> getNodeAvgSup() {
        return nodeAvgSup;
    }

    public void setNodeAvgSup(HashMap<Integer, Double> nodeAvgSup) {
        this.nodeAvgSup = nodeAvgSup;
    }

    public HashMap<Integer, Double> getNodeSupSkew() {
        return nodeSupSkew;
    }

    public void setNodeSupSkew(HashMap<Integer, Double> nodeSupSkew) {
        this.nodeSupSkew = nodeSupSkew;
    }

    public HashMap<Integer, Integer> getNodeMaxTrussness() {
        return nodeMaxTrussness;
    }

    public void setNodeMaxTrussness(HashMap<Integer, Integer> nodeMaxTrussness) {
        this.nodeMaxTrussness = nodeMaxTrussness;
    }

    public HashMap<Integer, HashMap<Integer, Integer>> getEdgeMaxTrussness() {
        return edgeMaxTrussness;
    }

    public void setEdgeMaxTrussness(HashMap<Integer, HashMap<Integer, Integer>> edgeMaxTrussness) {
        this.edgeMaxTrussness = edgeMaxTrussness;
    }

    public HashMap<Integer, HashMap<Integer, Integer>> getTrussness() {
        return trussness;
    }

    public int getTrussness(int i, int j) {
        return trussness.get(i).get(j);
    }
}
