package code.baseline;

import org.apache.log4j.Logger;
import code.graph.Graph;
import code.service.GraphSerializer;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;


public class TDBottomUp implements Serializable {
    private Graph G;
    //将相同support的edge分为一类
    private final HashMap<Integer, LinkedList<Map.Entry<Integer, Integer>>> classifySup = new HashMap<>();
    //记录trussness
    private final HashMap<Integer, HashMap<Integer, Integer>> trussness = new HashMap<>();
    private final static Logger logger = Logger.getLogger("InfoLogger");
    public TDBottomUp(String gFilePath) throws IOException {
        this.G = new Graph(gFilePath);
        for (int i : this.G.getSup().keySet()) {//初始化
            this.trussness.put(i, new HashMap<>());
        }
    }

    public TDBottomUp(String dat, boolean flag) throws IOException, ClassNotFoundException {
        if (flag) {
            this.G = GraphSerializer.antiSerialize(dat);
            for (int i : this.G.getSup().keySet()) {//初始化
                this.trussness.put(i, new HashMap<>());
            }
        }
        else {
            this.G = new Graph(dat);
            for (int i : this.G.getSup().keySet()) {//初始化
                this.trussness.put(i, new HashMap<>());
            }
        }
    }

    public TDBottomUp(HashMap<Integer, HashSet<Integer>> $graph) throws IOException, InterruptedException {
        this.G = new Graph($graph);
        for (int i : this.G.getSup().keySet()) {//初始化
            this.trussness.put(i, new HashMap<>());
        }
    }

    //将相同support的edge分为一类
    public void supClassify(){
        this.classifySup.clear();//在分类前清空
        for (int i : this.G.getSup().keySet()) {
            for (int j : this.G.getSup().get(i).keySet()) {
                int support = this.G.getSup(i, j);
                if (this.classifySup.get(support) == null)
                    this.classifySup.put(support, new LinkedList<>());
                this.classifySup.get(support).add(Map.entry(i, j));
            }
        }
    }

    //剥离所有不满足k-2条件的edge
    //为提高效率, 删边时不更新maxSup, 在结束后做一次maxSup更新
    public void getTruss(int k) {
        long startTime = System.currentTimeMillis();
        this.supClassify();//support分类
        Queue<Map.Entry<Integer, Integer>> q = new LinkedList<>();//删除队列
        HashMap<Integer, HashMap<Integer, Boolean>> deleted = new HashMap<>();//是否被删除
        for (int i : this.G.getSup().keySet()) {//初始化删除位
            deleted.put(i, new HashMap<>());
            for (int j : this.G.getSup().get(i).keySet()) {
                deleted.get(i).put(j, false);
            }
        }
        logger.debug("迭代删除不满足sup >= " + (k - 2) + "的边");
        for (int i = 0; i < k - 2; i++) {
            if (this.classifySup.get(i) != null) {
                q.addAll(this.classifySup.get(i));//所有不满足条件的边加入
            }
        }
        while (!q.isEmpty()) {
            Map.Entry<Integer, Integer> edge = q.poll();//出队列
            int x = edge.getKey();
            int y = edge.getValue();
            if (!deleted.get(x).get(y)){//还没有被删掉
                HashSet<Integer> comNeibor = this.G.removeEdge(x, y, false);//修改support值，删除记录值
                for (int w : comNeibor) {//其他边受影响，support小于k-2
                    if (this.G.getSup(x, w) < k - 2 && !deleted.get(x).get(w))
                        q.add(Map.entry(x, w));
                    if (this.G.getSup(y, w) < k - 2 && !deleted.get(y).get(w))
                        q.add(Map.entry(y, w));
                }
                //删除孤立点
                if (this.G.getGraph().get(x).isEmpty())
                    this.G.removeNode(x);
                if (this.G.getGraph().get(y).isEmpty())
                    this.G.removeNode(y);
                //设置trussness
                this.trussness.get(x).put(y, k - 1);
                this.trussness.get(y).put(x, k - 1);

                //设置删除位
                deleted.get(x).replace(y, true);
                deleted.get(y).replace(x, true);
            }
        }
        logger.debug("删边完成");
        //全部删完后一次性更新最大support和degree
        this.G.maxSupport();
        this.G.maxDegree();
        long endTime = System.currentTimeMillis();
        logger.debug(k + "-truss计算，耗时: " + (double) (endTime - startTime) / 1000);
    }

    public boolean trussDecomposition(int k, HashSet<Integer> source) {
        this.getTruss(k);
        return UniCompoDelete(source);
    }

    //删除不包括source的连通分量
    public boolean UniCompoDelete(HashSet<Integer> source){
        boolean exist = false;//是否存在包含所有querynode的连通分量
        int uc = -1;//包含source的连通分量编号
        UniComponent uniCompo = new UniComponent(this.G.getGraph());//构造连通分量数据结构
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
                    delNodes.forEach(node -> this.G.removeNode(node, false));
                }
            }
            this.G.maxDegree();
            this.G.maxSupport();
            long endTime = System.currentTimeMillis();
            logger.debug("删除连通分量，耗时: " + (double) (endTime - startTime) / 1000);
            return true;
        }
        return false;
    }

    public Graph getG() {
        return G;
    }

    public HashMap<Integer, LinkedList<Map.Entry<Integer, Integer>>> getClassifySup() {
        return classifySup;
    }

    public HashMap<Integer, HashMap<Integer, Integer>> getTrussness() {
        return trussness;
    }
}
