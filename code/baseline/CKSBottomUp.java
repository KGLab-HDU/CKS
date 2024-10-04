package code.baseline;

import org.apache.log4j.Logger;
import code.util.DeepCopy;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

public class CKSBottomUp {
    private TDBottomUp originGraph;//原始graph
    private TDBottomUp firstTruss;//第一个truss
    private TDBottomUp secondTruss;//第二个truss
    private String gFilePath;
    private HashSet<Integer> source;//查询节点
    private int firstK;
    private int secondK;
    private final static Logger logger = Logger.getLogger("InfoLogger");
    public CKSBottomUp(String gFilePath, HashSet<Integer> source) throws IOException {
        this.gFilePath = gFilePath;
        this.source = source;
        this.originGraph = new TDBottomUp(this.gFilePath);
        this.firstK = 0;
        this.secondK = 0;
    }

    //flag: 是否从序列化文件读取图
    public CKSBottomUp(String gFilePath, HashSet<Integer> source, boolean flag) throws IOException, ClassNotFoundException {
        if (flag) {
            this.gFilePath = gFilePath;
            this.source = source;
            this.originGraph = new TDBottomUp(this.gFilePath, flag);
            logger.debug("反序列化成功");
            this.firstK = 0;
            this.secondK = 0;
        }
        else {
            this.gFilePath = gFilePath;
            this.source = source;
            this.originGraph = new TDBottomUp(this.gFilePath);
            this.firstK = 0;
            this.secondK = 0;
        }
    }

    public CKSBottomUp(HashMap<Integer, HashSet<Integer>> $graph, HashSet<Integer> source) throws IOException, InterruptedException {
        this.gFilePath = "$graph";
        this.source = source;
        this.originGraph = new TDBottomUp($graph);
        this.firstK = 0;
        this.secondK = 0;
    }

    public boolean computeKeyMember() throws IOException, InterruptedException {
        int k = 2;
        HashMap<Integer, HashSet<Integer>> tmpGraph = this.originGraph.getG().getGraph();
        boolean exist = false;//如果第一次分解就是false，说明查询节点不包含在任何社区
        //深拷贝初始化firstTruss
        this.firstTruss = new TDBottomUp(this.originGraph.getG().getGraph());
        while (this.firstTruss.trussDecomposition(k, source)) {//当存在k-truss的连通分量包含所有source就继续
            //暂存当前graph
            exist = true;
            tmpGraph = (HashMap<Integer, HashSet<Integer>>) DeepCopy.copy(this.firstTruss.getG().getGraph());
            this.firstK = k;
            ++k;
        }
        this.firstTruss = new TDBottomUp(tmpGraph);//firstTruss计算完成
        logger.debug("已找到第一个truss，k = " + this.firstK);
        this.secondTruss = new TDBottomUp(tmpGraph);//初始化secondTruss

        for (k = this.firstK; ; k++){
            this.secondTruss.getTruss(k);
            if (this.secondTruss.getG().getNodenum() == 0)
                break;
            this.secondK = k;
            //暂存当前graph
            tmpGraph = (HashMap<Integer, HashSet<Integer>>) DeepCopy.copy(this.secondTruss.getG().getGraph());
        }
        logger.debug("已找到KeyMember，k = " + this.secondK);
        this.secondTruss = new TDBottomUp(tmpGraph);
        return exist;
    }

    public void showDetials(boolean more){
        //origin Graph information
        System.out.println("原图信息: ");
        System.out.println("最大support: " + this.originGraph.getG().getMaxSup());
        System.out.println("最大degree: " + this.originGraph.getG().getMaxDeg());
        System.out.println("节点数量: " + this.originGraph.getG().getNodenum());
        System.out.println("边数量: " + this.originGraph.getG().getEdgenum());
        System.out.println("三角形数量: " + this.originGraph.getG().getTriNum());
        //firstTruss information
        System.out.println("------------------------------------------------------------------");
        System.out.println("FirstTruss: ");
        System.out.println(this.firstK + "-truss");
        System.out.println("最大support: " + this.firstTruss.getG().getMaxSup());
        System.out.println("最大degree: " + this.firstTruss.getG().getMaxDeg());
        System.out.println("节点数量: " + this.firstTruss.getG().getNodenum());
        System.out.println("边数量: " + this.firstTruss.getG().getEdgenum());
        System.out.println("三角形数量: " + this.firstTruss.getG().getTriNum());
        //secondTruss information
        System.out.println("------------------------------------------------------------------");
        System.out.println("SecondTruss: ");
        System.out.println(this.secondK + "-truss");
        System.out.println("最大support: " + this.secondTruss.getG().getMaxSup());
        System.out.println("最大degree: " + this.secondTruss.getG().getMaxDeg());
        System.out.println("节点数量: " + this.secondTruss.getG().getNodenum());
        System.out.println("边数量: " + this.secondTruss.getG().getEdgenum());
        System.out.println("三角形数量: " + this.secondTruss.getG().getTriNum());
        System.out.println("Key Member: " + this.secondTruss.getG().getNodenum());
    }

    public TDBottomUp getOriginGraph() {
        return originGraph;
    }

    public TDBottomUp getFirstTruss() {
        return firstTruss;
    }

    public TDBottomUp getSecondTruss() {
        return secondTruss;
    }

    public String getgFilePath() {
        return gFilePath;
    }

    public HashSet<Integer> getSource() {
        return source;
    }

    public int getFirstK() {
        return firstK;
    }

    public int getSecondK() {
        return secondK;
    }
}
