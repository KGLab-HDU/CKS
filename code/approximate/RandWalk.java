package code.approximate;

import org.apache.log4j.Logger;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.DMatrixSparseTriplet;
import org.ejml.ops.ConvertDMatrixStruct;
import org.ejml.sparse.csc.CommonOps_DSCC;
import code.graph.RWGraph;

import java.util.*;

public class RandWalk {
    protected RWGraph G;
    protected final HashMap<Integer, HashMap<Integer, Double>> transitionMatrix = new HashMap<>();//转移概率矩阵
    protected HashMap<Integer, Integer> node2index = new HashMap<>();//node真实序号->游走范围内下标
    protected HashMap<Integer, Integer> index2node = new HashMap<>();//游走范围内下标node真实序号->node真实序号
    //游走范围
    protected int nodeNum;
    protected int edgeNum;
    protected int scope;
    protected double alpha;
    //输入
    protected HashSet<Integer> source;//查询节点
    protected int topN;//得到top-n节点

    public long time = 0;

    protected final static Logger logger = Logger.getLogger("InfoLogger");

    public RandWalk(RWGraph G, HashSet<Integer> source, int scope, int topN) {
        this.G = G;
        this.source = source;
        this.scope = scope;
        this.topN = topN;
    }
    public RandWalk(){

    }
    public void setParam(RWGraph G, HashSet<Integer> source, int scope, int topN, double alpha){
        this.G = G;
        this.source = source;
        this.scope = scope;
        this.topN = topN;
        this.alpha = alpha;
    }

    //获得scope范围内的节点作为游走范围
    //计算得到nodeNum
    public HashSet<Integer> selectNodes() {
        this.nodeNum = 0;
        this.node2index.clear();
        this.index2node.clear();
        long startTime = System.currentTimeMillis();
        HashSet<Integer> selected = new HashSet<>();//scope范围内的点
        HashSet<Integer> current = new HashSet<>();
        selected.addAll(this.source);//起点
        current.addAll(this.source);
        int i = this.scope;
        while (i-- > 0) {
            HashSet<Integer> tmp = new HashSet<>();
            Iterator<Integer> it = current.iterator();
            while (it.hasNext()) {
                tmp.addAll(this.G.getGraph().get(it.next()));
            }
            current.clear();
            current.addAll(tmp);//新加入的点作为下一轮起点
            selected.addAll(tmp);
        }
        int index = 0;//index
        for (int node : selected) {
            this.node2index.put(node, index);
            this.index2node.put(index, node);
            index++;
        }
        this.nodeNum = selected.size();
        long endTime = System.currentTimeMillis();
        logger.debug("m-bound筛点，节点个数" + this.nodeNum + "，耗时: " + (double) (endTime - startTime) / 1000);
        return selected;
    }

    //计算得到edgeNum
    public void computePossibility() {
        this.edgeNum = 0;
        this.transitionMatrix.clear();
        long startTime = System.currentTimeMillis();
        if (this.node2index.size() < this.topN) {//top-n过大
            logger.debug("top-n过大，检查topN输入是否有误，或者调大scope");
            this.topN = this.node2index.size();
        }
        HashSet<Integer> nodes = new HashSet<>();
        nodes.addAll(this.node2index.keySet());
        double p;
        for (int i : nodes) {//遍历游走范围内的点
            int supSum = 0;
            HashMap<Integer, Integer> tmp = new HashMap<>();//临时记录邻居j的sup
            this.transitionMatrix.put(i, new HashMap<>());
            for (int j : this.G.getGraph().get(i)) {
                if (nodes.contains(j)) {
                    int sup = this.G.getSup(i, j);
                    tmp.put(j, sup);
                    supSum += sup;
                    this.edgeNum++;
                }
            }
            for (int j : tmp.keySet()) {
                //邻边的support都是0, 等概率转移
                if (supSum == 0) {
                    p = 1.0 / tmp.size();
                }
                else {
                    p = (double)tmp.get(j) / supSum;
                }
                this.transitionMatrix.get(i).put(j, p);
            }
        }
        this.edgeNum /= 2;
        long endTime = System.currentTimeMillis();
        logger.debug("转移概率计算，耗时: " + (double) (endTime - startTime) / 1000);
    }

    public DMatrixRMaj rank(int start, int iterations){
        DMatrixRMaj entity = new DMatrixRMaj(this.nodeNum,1);//nodenum行1列
        DMatrixRMaj tmp = new DMatrixRMaj(this.nodeNum,1);
        //ejml中生成双实数稀疏CSC矩阵，一种只存储非零元素的矩阵
        DMatrixSparseCSC work = new DMatrixSparseCSC(this.nodeNum, this.nodeNum, this.edgeNum);
        //构建稀疏矩阵三元组
        DMatrixSparseTriplet work_triplet = new DMatrixSparseTriplet(this.nodeNum, this.nodeNum, this.edgeNum);

        //entity.set(this.node2index.get(start),0,1.0);
        for (int i = 0; i < this.nodeNum; i++) {
            entity.set(i,0,1.0 / this.nodeNum);
        }
        for(int i : this.transitionMatrix.keySet()){
            for(int j : this.transitionMatrix.get(i).keySet()){
                work_triplet.addItem(this.node2index.get(i),this.node2index.get(j),this.transitionMatrix.get(j).get(i));
            }
        }
        ConvertDMatrixStruct.convert(work_triplet,work);
        for(int times = 0; times < iterations; times++){
            CommonOps_DSCC.mult(work, entity, tmp);
            entity.set(tmp);
        }
        long endTime = System.currentTimeMillis();
        return entity;
    }

    public LinkedList<Integer> TopN(int iterations) {
        long startTime = System.currentTimeMillis();
        DMatrixRMaj ranks = rank(this.source.iterator().next(), iterations);//随机游走iterations次
        long endTime = System.currentTimeMillis();
        this.time = endTime - startTime;
        logger.debug("随机游走，耗时: " + (double) (endTime - startTime) / 1000);

        double[] rankList = ranks.data;
        PriorityQueue<Integer> SRHeap = new PriorityQueue<>(this.topN, (o1, o2) -> rankList[o1] - rankList[o2] > 0 ? 1 : -1);
        //初始小根堆
        for (int i = 0; i < this.topN; i++) {
            SRHeap.add(i);
        }
        //构建小根堆, 不断更新最小值
        for (int i = this.topN; i < this.nodeNum; i++) {
            if (rankList[SRHeap.peek()] < rankList[i]) {
                SRHeap.poll();
                SRHeap.add(i);
            }
        }
        LinkedList<Integer> result = new LinkedList<>();
        for (int i = 0; i < this.topN; i++) {
            result.add(this.index2node.get(SRHeap.poll()));
        }
        return result;
    }

    public LinkedList<Integer> getTopNode(int iterations, int n) {
        long startTime = System.currentTimeMillis();
        DMatrixRMaj ranks = rank(this.source.iterator().next(), iterations);//随机游走iterations次
        long endTime = System.currentTimeMillis();
        this.time = endTime - startTime;
        logger.debug("随机游走，耗时: " + (double) (endTime - startTime) / 1000);

        double[] rankList = ranks.data;
        PriorityQueue<Integer> SRHeap = new PriorityQueue<>(this.topN, (o1, o2) -> rankList[o1] - rankList[o2] > 0 ? 1 : -1);
        //初始小根堆
        for (int i = 0; i < this.topN; i++) {
            SRHeap.add(i);
        }
        //构建小根堆, 不断更新最小值
        for (int i = this.topN; i < this.nodeNum; i++) {
            if (rankList[SRHeap.peek()] < rankList[i]) {
                SRHeap.poll();
                SRHeap.add(i);
            }
        }
        LinkedList<Integer> result = new LinkedList<>();
        for (int i = 0; i < n; i++) {
            result.add(this.index2node.get(SRHeap.poll()));
        }
        return result;
    }

    public LinkedList<Integer> optimization(LinkedList<Integer> topn) {
        long startTime = System.currentTimeMillis();
        HashSet<Integer> oldSet = new HashSet<>();
        HashSet<Integer> newSet = new HashSet<>();
        oldSet.addAll(topn);
        for (int node : oldSet) {
            newSet.addAll(this.G.getGraph().get(node));
        }
        newSet.removeAll(oldSet);
        ArrayList<Map.Entry<Integer, Integer>> degree = new ArrayList<>();
        for (int i : oldSet) {
            int num = 0;
            for (int j : oldSet) {
                if (this.G.getGraph().get(i).contains(j))
                    num++;//在old中i的邻边数
            }
            degree.add(Map.entry(i, num));
        }
        for (int i : newSet) {
            int num = 0;
            for (int j : oldSet) {
                if (this.G.getGraph().get(i).contains(j))
                    num++;//在old中i的邻边数
            }
            degree.add(Map.entry(i, num));
        }
        Collections.sort(degree, (o1, o2) -> o2.getValue() - o1.getValue());
        LinkedList<Integer> refine = new LinkedList<>();
        for (int i = 0; i < this.topN; i++) {
            refine.add(degree.get(i).getKey());
        }
        long endTime = System.currentTimeMillis();
        logger.debug("结果集优化完成，耗时: " + (double) (endTime - startTime) / 1000);
        return refine;
    }

    public RWGraph getG() {
        return G;
    }

    public HashMap<Integer, HashMap<Integer, Double>> getTransitionMatrix() {
        return transitionMatrix;
    }

    public HashMap<Integer, Integer> getNode2index() {
        return node2index;
    }

    public HashMap<Integer, Integer> getIndex2node() {
        return index2node;
    }

    public int getNodeNum() {
        return nodeNum;
    }

    public int getEdgeNum() {
        return edgeNum;
    }

    public int getScope() {
        return scope;
    }

    public HashSet<Integer> getSource() {
        return source;
    }

    public int getTopN() {
        return topN;
    }

    public void setScope(int scope) {
        this.scope = scope;
    }

    public void setTopN(int topN) {
        this.topN = topN;
    }
}
