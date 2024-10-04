package code.baseline;

import org.apache.log4j.Logger;
import code.graph.Graph;
import code.service.GraphSerializer;

import java.io.IOException;
import java.util.*;

public class TDTopDown {
    private Graph originGraph;//存储原始图
    private Graph truss;//存储当前truss
    private final static Logger logger = Logger.getLogger("InfoLogger");
    public TDTopDown(String gFilePath) throws IOException {
        this.originGraph = new Graph(gFilePath);
    }

    public TDTopDown(String dat, boolean flag) throws IOException, ClassNotFoundException {
        if (flag) {
            this.originGraph = GraphSerializer.antiSerialize(dat);
        }
        else {
            this.originGraph = new Graph(dat);
        }
    }

    public TDTopDown(HashMap<Integer, HashSet<Integer>> $graph) throws IOException, InterruptedException {
        this.originGraph = new Graph($graph);
    }

    //本次k得到的truss是否有包含source的连通分量
    public boolean trussDecomposition(int k, HashSet<Integer> source) throws IOException, InterruptedException {
        this.getTruss(k);
        return UniCompoDelete(source);
    }

    public boolean trussDecomposition(int k) throws IOException, InterruptedException {
        this.getTruss(k);
        return !this.truss.getGraph().isEmpty();
    }

    public boolean UniCompoDelete (HashSet<Integer> source) {
        boolean exist = false;//是否存在包含所有querynode的连通分量
        int uc = -1;//包含source的连通分量编号
        UniComponent uniCompo = new UniComponent(this.truss.getGraph());//构造连通分量数据结构
        HashMap<Integer, HashSet<Integer>> component = uniCompo.getComponent();//获得构造好的数据结构
        int cnt = uniCompo.getCnt();//连通分量数量
        for (int i = 0; i < cnt; i++) {
            if (component.get(i).containsAll(source)) {
                exist = true;
                uc = i;
                break;
            }
        }
        if (exist) {//删点方式删除连通分量
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < cnt; i++){
                if (i != uc) {
                    HashSet<Integer> delNodes = new HashSet<>();
                    delNodes.addAll(component.get(i));
                    delNodes.forEach(node -> this.truss.removeNode(node, false));
                }

            }
            this.truss.maxSupport();
            this.truss.maxDegree();
            long endTime = System.currentTimeMillis();
            logger.debug("删除连通分量，耗时: " + (double) (endTime - startTime) / 1000);
            return true;
        }
        return false;
    }

    //得到一个k-truss
    public void getTruss(int k) throws IOException, InterruptedException {
        long startTime = System.currentTimeMillis();
        HashMap<Integer, HashSet<Integer>> $graph = new HashMap<>();
        HashMap<Integer, HashMap<Integer, Boolean>> deleted = new HashMap<>();
        // step 1: 将所有sup >= k - 2的边所在的三角形加入，构成一个子图
        // 加入三角形计算效率低，改为加入邻居及邻边
        // step 2: 迭代方式删除所有子图中support < k - 2的边，得到truss
        for (int i : this.originGraph.getSup().keySet()) {
            for (int j : this.originGraph.getSup().get(i).keySet()) {
                if (this.originGraph.getSup(i, j) >= k - 2) { // 将所有sup >= k - 2的边加入graph
                    if ($graph.get(i) == null) $graph.put(i, new HashSet<>());
                    if (deleted.get(i) == null) deleted.put(i, new HashMap<>());
                    $graph.get(i).add(j);
                    deleted.get(i).put(j, false);//未被删除
                }
            }
        }
        //System.out.print(k + "-truss " + "构造truss子图 ");
        logger.debug("构造临时" + k + "-truss子图");
        this.truss = null;
        this.truss = new Graph($graph);
        logger.debug("临时" + k + "-truss子图构造完成");
        logger.debug("迭代删除不满足sup >= " + (k - 2) + "的边");
        // 迭代删除truss中sup < k - 2的边
        Queue<Map.Entry<Integer, Integer>> q = new LinkedList<>();
        for (int i : this.truss.getSup().keySet()) {
            for (int j : this.truss.getSup().get(i).keySet()) {
                if (this.truss.getSup(i, j) < k - 2) {
                    q.add(Map.entry(i, j));
                }
            }
        }
        while (!q.isEmpty()) {
            Map.Entry<Integer, Integer> edge = q.poll();
            int i = edge.getKey();
            int j = edge.getValue();
            if (!deleted.get(i).get(j)) {//还没有被删掉
                HashSet<Integer> comNeibor = this.truss.removeEdge(i, j, false);
                //受影响的边不满足truss条件
                comNeibor.forEach(w -> {
                    if (this.truss.getSup(i, w) < k - 2 && !deleted.get(i).get(w))
                        q.add(Map.entry(i, w));
                    if (this.truss.getSup(j, w) < k - 2 && !deleted.get(j).get(w))
                        q.add(Map.entry(j, w));
                });
                //删除孤立点
                if (this.truss.getGraph().get(i).isEmpty())
                    this.truss.removeNode(i);
                if (this.truss.getGraph().get(j).isEmpty())
                    this.truss.removeNode(j);
                deleted.get(i).replace(j, true);
                deleted.get(j).replace(i, true);
            }
        }
        logger.debug("删边完成");
        //一次性更新support和degree
        this.truss.maxSupport();
        this.truss.maxDegree();
        long endTime = System.currentTimeMillis();
        logger.debug(k + "-truss计算，耗时: " + (double) (endTime - startTime) / 1000);
    }

    public Graph getOriginGraph() {
        return originGraph;
    }

    public Graph getTruss() {
        return truss;
    }
}
