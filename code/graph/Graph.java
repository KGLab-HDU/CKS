package code.graph;

import org.apache.log4j.Logger;
import code.util.DeepCopy;

import java.io.*;
import java.util.*;


public class Graph implements Serializable {
    @Serial
    private static final long serialVersionUID = 8856712578623485886L;
    // 图基本结构
    protected HashSet<Integer> nodes;//节点集合
    protected HashMap<Integer, HashSet<Integer>> graph;//邻接表，节点 -> 邻居们

    //图属性
    protected int nodenum;//节点数量
    protected int edgenum;//边数量
    protected int maxSup;//最大support值
    protected int maxDeg;//最大degree
    protected long triNum;//三角形数量
    protected String gFilePath;//存储图的文件路径
    protected HashMap<Integer, HashMap<Integer, Integer>> sup;//support

    private final static Logger logger = Logger.getLogger("InfoLogger");

    //深拷贝构造新图
    public Graph(LinkedList<Integer> nodes,
                 HashMap<Integer, HashSet<Integer>> graph,
                 int nodenum,
                 int edgenum,
                 int maxSup,
                 int triNum,
                 int maxDeg,
                 String gFilePath,
                 HashMap<Integer, HashMap<Integer, Integer>> sup) throws IOException, InterruptedException {
        this.nodes = (HashSet<Integer>) DeepCopy.copy(nodes);
        this.graph = (HashMap<Integer, HashSet<Integer>>) DeepCopy.copy(graph);
        this.sup = (HashMap<Integer, HashMap<Integer, Integer>>) DeepCopy.copy(sup);
        this.nodenum = nodenum;
        this.edgenum = edgenum;
        this.maxSup = maxSup;
        this.gFilePath = gFilePath;
        this.triNum = triNum;
        this.maxDeg = maxDeg;
    }

    //通过文件路径构造新图
    public Graph(String gFilePath) throws IOException {
        logger.debug(gFilePath + ": 构造Graph");
        this.nodes = new HashSet<>();
        this.graph = new HashMap<>();
        this.sup = new HashMap<>();
        this.gFilePath = gFilePath;
        logger.debug("Graph属性初始化完成");
        this.buildGraphFromFile();
        logger.debug("Graph读取完成");
        this.supComputation();
        logger.debug("Support计算完成");
        this.maxDegree();
        logger.debug("MaxDegree计算完成");
        this.triangleNum();
        logger.debug("TriangleNum计算完成");
    }

    //通过graph复制一个图（深拷贝）
    public Graph(HashMap<Integer, HashSet<Integer>> $graph) throws IOException, InterruptedException {
        logger.debug("邻接表graph深拷贝构造Graph");
        this.graph = (HashMap<Integer, HashSet<Integer>>) DeepCopy.copy($graph);
        this.nodes = new HashSet<>();
        this.sup = new HashMap<>();
        //计算nodenum edgenum
        this.nodenum = this.graph.size();
        this.nodes.addAll(this.graph.keySet());
        for (int node : this.graph.keySet()) {
            this.edgenum += this.graph.get(node).size();
        }

        this.edgenum /= 2;
        this.supComputation();
        logger.debug("Support计算完成");
        this.maxDegree();
        logger.debug("MaxDegree计算完成");
        this.triangleNum();
        logger.debug("TriangleNum计算完成");
    }

    /*
     * graph nodes nodenum edgenum
     * */
    public void buildGraphFromFile() throws IOException {
        long startTime = System.currentTimeMillis();

        BufferedReader br = new BufferedReader(new FileReader(this.gFilePath));
        String line = "";
        while ((line = br.readLine()) != null) {
            String[] edge = line.split(" ");//读取一条边并解析
            int nodeA = Integer.parseInt(edge[0]);
            int nodeB = Integer.parseInt(edge[1]);
            if (nodeA == nodeB)
                continue;//不加入自环
            //如果node的邻接表为空，则创建一个邻接表
            if (this.graph.get(nodeA) == null) this.graph.put(nodeA, new HashSet<>());
            if (this.graph.get(nodeB) == null) this.graph.put(nodeB, new HashSet<>());
            //向邻接表中加入邻居
            this.graph.get(nodeA).add(nodeB);
            this.graph.get(nodeB).add(nodeA);
            //加入点集
            this.nodes.add(nodeA);
            this.nodes.add(nodeB);
        }
        //计算edgenum
        for (int node : this.graph.keySet()) {
            this.edgenum += this.graph.get(node).size();
        }
        this.edgenum /= 2;
        this.nodenum = this.nodes.size();
        br.close();
        long endTime = System.currentTimeMillis();
        logger.debug("文件读取完成，耗时: " + (double) (endTime - startTime) / 1000 + "s");
    }

    public void supComputation() {
        long startTime = System.currentTimeMillis();
        this.maxSup = 0;
        //遍历每个节点
        for (int i : this.graph.keySet()) {
            if (this.sup.get(i) == null) this.sup.put(i, new HashMap<>());
            for (int j : this.graph.get(i)) { //i的邻居j
                //deg(u) > deg(v)
                int u = this.graph.get(i).size() > this.graph.get(j).size() ? i : j;
                int v = this.graph.get(i).size() > this.graph.get(j).size() ? j : i;
                int comNeibor = 0;
                for (int w : this.graph.get(v)) { //v的邻居w
                    if (this.graph.get(u).contains(w)) { //u也包含w
                        comNeibor++;//说明是共同邻居
                    }
                }
                if (this.sup.get(i).get(j) == null)
                    this.sup.get(i).put(j, comNeibor);
                this.maxSup = Math.max(this.maxSup, comNeibor);//最大support
            }
        }
        long endTime = System.currentTimeMillis();
        logger.debug("support计算完成，耗时: " + (double) (endTime - startTime) / 1000 + "s");
    }

    public static HashMap<Integer, HashMap<Integer, Integer>> supComputation(HashMap<Integer, HashSet<Integer>> graph) {
        HashMap<Integer, HashMap<Integer, Integer>> sup = new HashMap<>();
        for (int i : graph.keySet()) {
            if (sup.get(i) == null) sup.put(i, new HashMap<>());
            for (int j : graph.get(i)) { //i的邻居j
                //deg(u) > deg(v)
                int u = graph.get(i).size() > graph.get(j).size() ? i : j;
                int v = graph.get(i).size() > graph.get(j).size() ? j : i;
                int comNeibor = 0;
                for (int w : graph.get(v)) { //v的邻居w
                    if (graph.get(u).contains(w)) { //u也包含w
                        comNeibor++;//说明是共同邻居
                    }
                }
                if (sup.get(i).get(j) == null)
                    sup.get(i).put(j, comNeibor);
            }
        }
        return sup;
    }

    //深拷贝
    public Graph copyGraph() throws IOException, InterruptedException {
        return (Graph) DeepCopy.copy(this);
    }

    //获得的scope范围内的节点
    public HashSet<Integer> nodeInScope(HashSet<Integer> truss, int scope) {
        long startTime = System.currentTimeMillis();
        HashSet<Integer> nodes = new HashSet<>();
        nodes.addAll(truss);
        HashSet<Integer> alt = new HashSet<>();//辅助set
        HashSet<Integer> del = new HashSet<>();
        del.addAll(truss);//最后需要删除所有原来的truss节点
        while (scope > 0) {
            for (int node : nodes) {
                alt.addAll(this.graph.get(node));//将node的邻接节点全部加入alt
            }
            nodes.addAll(alt);
            --scope;
        }
        nodes.removeAll(del);
        long endTime = System.currentTimeMillis();
        logger.debug("获得Scope范围内点，耗时: " + (double) (endTime - startTime) / 1000);
        return nodes;
    }

    //获得scope跳邻居节点
    public HashSet<Integer> nHopNeibor(int node, int scope) {
        long startTime = System.currentTimeMillis();
        HashSet<Integer> nodes = new HashSet<>();
        HashSet<Integer> alt = new HashSet<>();
        HashSet<Integer> del = new HashSet<>();
        nodes.add(node);
        del.add(node);
        while (scope > 0) {
            for (int n : nodes) {
                alt.addAll(this.graph.get(n));//nodes的所有邻接节点加入
            }
            nodes.clear();
            alt.removeAll(del);
            nodes.addAll(alt);
            del.addAll(alt);
            --scope;
        }
        long endTime = System.currentTimeMillis();
        logger.debug("获得n-Hop邻居，耗时: " + (double) (endTime - startTime) / 1000);
        return nodes;
    }

    //计算alter各个点，到truss的平均距离
    public LinkedList<Map.Entry<Integer, Double>> getNodeDistSort(HashSet<Integer> truss, HashSet<Integer> alter) {
        HashMap<Integer, Double> dis = new HashMap<>();
        for (int node : alter) {
            dis.put(node, 0.0);
        }
        int num = alter.size();
        for (int node : truss) {
            int visit = 0;
            if (alter.contains(node)) visit++;
            int scope = 1;
            while (visit < num) {
                HashSet<Integer> nHop = this.nHopNeibor(node, scope);
                nHop.retainAll(alter);//获得alter中距离node为scope跳的邻居
                for (int neibor : nHop) {
                    dis.replace(neibor, dis.get(neibor) + scope);
                    ++visit;
                }
                ++scope;
            }
        }
        for (int i : dis.keySet()) {
            dis.replace(i, dis.get(i) / truss.size());
        }
        LinkedList<Map.Entry<Integer, Double>> sortList = new LinkedList<>();
        sortList.addAll(dis.entrySet());
        Collections.sort(sortList, (o1, o2) -> {
            return o1.getValue().compareTo(o2.getValue());
        });
        return sortList;
    }

    //node到truss的平均距离
    public double getNodeAvgDist(int node, HashSet<Integer> truss) {
        double distSum = 0.0;
        int visit = 0;
        int scope = 1;
        if (truss.contains(node)) visit++;//自己到自己的距离只能在这里被遍历
        while (visit != truss.size()) {
            HashSet<Integer> nHop = this.nHopNeibor(node, scope);
            nHop.retainAll(truss);//保留scope-hop邻居和truss的交集
            distSum += nHop.size() * scope;
            visit += nHop.size();
            ++scope;
        }
        return distSum / truss.size();
    }
    //计算node到Truss的最短距离
    public int getNodeMinDist(int node, HashSet<Integer> truss) {
        int scope = 1;
        if (truss.contains(node))
            return 0;
        while (true) {
            HashSet<Integer> nHop = this.nHopNeibor(node, scope);
            if (nHop.size() == 0)
                return -1;//不连通
            nHop.retainAll(truss);
            if (nHop.size() > 0)
                return scope;
            else
                scope++;
        }
    }

    public int getNodeMaxDist(int node, HashSet<Integer> truss) {
        int visit = 0;
        int scope = 1;
        if (truss.contains(node)) visit++;//自己到自己的距离只能在这里被遍历
        while (visit != truss.size()) {
            HashSet<Integer> nHop = this.nHopNeibor(node, scope);
            nHop.retainAll(truss);//保留scope-hop邻居和truss的交集
            visit += nHop.size();
            ++scope;
        }
        return scope;
    }

    public int maxDegree() {
        int max = 0;
        for (int i : this.graph.keySet()) {
            max = Math.max(max, this.graph.get(i).size());
        }
        this.maxDeg = max;
        return max;
    }

    public int maxSupport(){
        int max = 0;
        for (int i : this.sup.keySet()) {
            for (int j : this.sup.get(i).keySet()) {
                max = Math.max(max, this.getSup(i, j));
            }
        }
        this.maxSup = max;
        return max;
    }

    //获得图三角形数量 所有edge的support之和 / 3（同一个三角形被三条边各计了一次）
    //由于(i, j)和(j, i)是同一条边所以/6
    public long triangleNum() {
        long num = 0;
        for (int i : this.sup.keySet()) {
            for (int j : this.sup.get(i).keySet()) {
                num += this.sup.get(i).get(j);
            }
        }
        this.triNum = num / 6;
        return num / 6;
    }

    // getter
    public HashSet<Integer> getNodes() {
        return nodes;
    }

    public HashMap<Integer, HashSet<Integer>> getGraph() {
        return graph;
    }

    public int getNodenum() {
        return nodenum;
    }

    public int getEdgenum() {
        return edgenum;
    }

    public int getMaxSup() {
        return maxSup;
    }

    public String getgFilePath() {
        return gFilePath;
    }

    public int getMaxDeg() {
        return maxDeg;
    }

    public HashMap<Integer, HashMap<Integer, Integer>> getSup() {
        return sup;
    }

    public int getSup(int i, int j) {
        return this.sup.get(i).get(j);
    }

    public long getTriNum() {
        return triNum;
    }

    //setter
    public void setNodes(HashSet<Integer> nodes) {
        this.nodes = nodes;
    }

    public void setGraph(HashMap<Integer, HashSet<Integer>> graph) {
        this.graph = graph;
    }

    public void setNodenum(int nodenum) {
        this.nodenum = nodenum;
    }

    public void setEdgenum(int edgenum) {
        this.edgenum = edgenum;
    }

    public void setMaxSup(int maxSup) {
        this.maxSup = maxSup;
    }

    public void setgFilePath(String gFilePath) {
        this.gFilePath = gFilePath;
    }

    public void setSup(HashMap<Integer, HashMap<Integer, Integer>> sup) {
        this.sup = sup;
    }

    public void setMaxDeg(int maxDeg) {
        this.maxDeg = maxDeg;
    }

    public void setTriNum(int triNum) {
        this.triNum = triNum;
    }

    //设置(i, j)的support为value
    public void setSup(int i, int j, int value) {
        this.sup.get(i).replace(j, value);
    }

    //nodeNum增量
    public void alterNodeNum(int n) {
        this.nodenum += n;
    }
    //edgeNum增量
    public void alterEdgeNum(int n) {
        this.edgenum += n;
    }

    //从graph中删除一个点 i
    public void removeNode(int i) {
        if (this.graph.get(i) == null)
            throw new NullPointerException();//没有该节点
        //删除一个点，要删除该点和其他节点邻接表中的记录，support中的边记录
        HashSet<Integer> neibor = new HashSet<>();
        neibor.addAll(this.graph.get(i));
        neibor.forEach(j -> {//去除邻接节点与被删节点的关联
            this.removeEdge(i, j, false);
        });
        this.alterNodeNum(-1);
        this.graph.remove(i);
        this.sup.remove(i);
        this.nodes.remove(i);
    }

    public void removeNode(int i, boolean update) {
        if (this.graph.get(i) == null)
            throw new NullPointerException();//没有该节点
        //删除一个点，要删除该点和其他节点邻接表中的记录，support中的边记录
        HashSet<Integer> neibor = new HashSet<>();
        neibor.addAll(this.graph.get(i));
        neibor.forEach(j -> {//去除邻接节点与被删节点的关联
            this.removeEdge(i, j, update);
        });
        this.alterNodeNum(-1);
        this.graph.remove(i);
        this.sup.remove(i);
        this.nodes.remove(i);

        //若删除孤立节点，则不需要更新degree和support
        if (!neibor.isEmpty() && update){
            this.maxDegree();
            this.maxSupport();
        }
    }

    // 删除edge(i, j) and (j, i)
    // 改变受影响的support值
    // 更新triNum
    // 更新edgeNum
    // 返回共同邻居, 可以用于观察iw 和 jw边的support变化
    //使用update位来确定是否更新maxSup和maxDegree, 删一次边更新一次效率太低, 是否更新由调用方确定
    public HashSet<Integer> removeEdge(int i, int j, boolean update){
        //修改受影响的support值
        HashSet<Integer> comNeibor = new HashSet<>();
        comNeibor.addAll(this.graph.get(i));
        comNeibor.retainAll(this.graph.get(j));
        comNeibor.forEach(w -> {
            this.setSup(i, w, this.getSup(i, w) - 1);
            this.setSup(j, w, this.getSup(j, w) - 1);
            this.setSup(w, i, this.getSup(w, i) - 1);
            this.setSup(w, j, this.getSup(w, j) - 1);
        });
        //删除sup中的记录项
        this.sup.get(i).remove(j);
        this.sup.get(j).remove(i);
        //删除graph中的信息
        this.graph.get(i).remove(j);
        this.graph.get(j).remove(i);
        //边的数量-1
        this.alterEdgeNum(-1);
        //三角形数量
        this.triNum -= comNeibor.size();
        if (update) {
            this.maxSupport();//设置是否更新support
            this.maxDegree();
        }
        return comNeibor;
    }

}
