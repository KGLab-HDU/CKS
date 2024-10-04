package code.baseline;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashSet;

public class CKSTopDown {
    private TDTopDown firstTruss;

    private TDTopDown secondTruss;

    private String gFilePath;

    private HashSet<Integer> source;

    private int firstK;

    private int secondK;
    private final static Logger logger = Logger.getLogger("InfoLogger");

    public CKSTopDown(String gFilePath, HashSet<Integer> source) throws IOException {
        this.gFilePath = gFilePath;
        this.source = source;
        this.firstTruss = new TDTopDown(this.gFilePath);
        this.firstK = 0;
        this.secondK = 0;
    }

    public CKSTopDown(String gFilePath, HashSet<Integer> source, boolean flag) throws IOException, ClassNotFoundException {
        if (flag) {
            this.gFilePath = gFilePath;
            this.source = source;
            this.firstTruss = new TDTopDown(this.gFilePath, flag);
            logger.debug("反序列化成功");
            this.firstK = 0;
            this.secondK = 0;
        }
        else {
            this.gFilePath = gFilePath;
            this.source = source;
            this.firstTruss = new TDTopDown(this.gFilePath);
            this.firstK = 0;
            this.secondK = 0;
        }
    }

    public void computeKeyMember() throws IOException, InterruptedException {
        int supUB = 0;
        //计算k上限maxSup + 2
        for (int i : this.source) {
            for (int j : this.firstTruss.getOriginGraph().getGraph().get(i)) {
                supUB = Math.max(supUB, this.firstTruss.getOriginGraph().getSup(i, j));
            }
        }
        int k = supUB + 2;
        //计算第一个truss
        while (this.firstTruss.trussDecomposition(k, this.source) == false) {
            --k;
        }
        this.firstK = k;
        logger.debug("已找到第一个truss，k = " + this.firstK);
        //计算keymember
        this.secondTruss = new TDTopDown(this.firstTruss.getTruss().getGraph());
        supUB = 0;
        for (int i : this.secondTruss.getOriginGraph().getGraph().keySet()) {
            for (int j : this.secondTruss.getOriginGraph().getGraph().get(i)) {
                supUB = Math.max(supUB, this.secondTruss.getOriginGraph().getSup(i, j));
            }
        }
        k = supUB + 2;
        while (this.secondTruss.trussDecomposition(k) == false) {
            --k;
        }
        this.secondK = k;
        logger.debug("已找到第二个truss，k = " + this.secondK);
    }

    public void showDetials(boolean more){
        //origin Graph information
        System.out.println("原图信息: ");
        System.out.println("最大support: " + this.firstTruss.getOriginGraph().getMaxSup());
        System.out.println("最大degree: " + this.firstTruss.getOriginGraph().getMaxDeg());
        System.out.println("节点数量: " + this.firstTruss.getOriginGraph().getNodenum());
        System.out.println("边数量: " + this.firstTruss.getOriginGraph().getEdgenum());
        System.out.println("三角形数量: " + this.firstTruss.getOriginGraph().getTriNum());
        //firstTruss information
        System.out.println("------------------------------------------------------------------");
        System.out.println("FirstTruss: ");
        System.out.println(this.firstK + "-truss");
        System.out.println("最大support: " + this.firstTruss.getTruss().getMaxSup());
        System.out.println("最大degree: " + this.firstTruss.getTruss().getMaxDeg());
        System.out.println("节点数量: " + this.firstTruss.getTruss().getNodenum());
        System.out.println("边数量: " + this.firstTruss.getTruss().getEdgenum());
        System.out.println("三角形数量: " + this.firstTruss.getTruss().getTriNum());
        //secondTruss information
        System.out.println("------------------------------------------------------------------");
        System.out.println("SecondTruss: ");
        System.out.println(this.secondK + "-truss");
        System.out.println("最大support: " + this.secondTruss.getTruss().getMaxSup());
        System.out.println("最大degree: " + this.secondTruss.getTruss().getMaxDeg());
        System.out.println("节点数量: " + this.secondTruss.getTruss().getNodenum());
        System.out.println("边数量: " + this.secondTruss.getTruss().getEdgenum());
        System.out.println("三角形数量: " + this.secondTruss.getTruss().getTriNum());
        System.out.println("Key Member: " + this.secondTruss.getTruss().getNodenum());
    }

    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
        String file = "orkut";
        HashSet<Integer> query = new HashSet<>();
        query.add(1582915);
        CKSBottomUp cksBottomUp = new CKSBottomUp("./graph/" + file + ".dat", query, true);
        cksBottomUp.computeKeyMember();
        cksBottomUp.showDetials(false);
    }
}
