package code.model;

import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.DMatrixSparseTriplet;
import org.ejml.ops.ConvertDMatrixStruct;
import org.ejml.sparse.csc.CommonOps_DSCC;

import java.text.DecimalFormat;
import java.util.HashMap;

public class HopModel {
    private double alpha;
    private int xnode;//其他节点的超点个数
    private final HashMap<String, Integer> nodeMap = new HashMap<>();
    private final HashMap<Integer, HashMap<Integer, Double>> tranMatrix = new HashMap<>();

    private static HashMap<Integer, HashMap<Double, Double>> heatmap = new HashMap<>();//(hop, alpha/gamma) -> p
    static {
        //hop
        DecimalFormat df = new DecimalFormat("#.0");
        double j = 0.1;
        for (int i = 2; i <= 10; i++) {
            heatmap.put(i, new HashMap<>());
        }
    }

    public HopModel(double alpha, int hop) {
        assert hop >= 2 : "跳数必须 > 2";
        this.alpha = alpha;
        this.xnode = hop - 1;
        //构建编号表
        this.nodeMap.put("Q", 0);
        this.nodeMap.put("K", 1);
        for (int i = 0; i < this.xnode; i++) {
            this.nodeMap.put("X" + i, 2 + i);
        }
        //初始化计算转移概率
        this.tranMatrix.put(index("Q"), new HashMap<>());
        this.tranMatrix.put(index("K"), new HashMap<>());
        for (int i = 0; i < this.xnode; i++) {
            this.tranMatrix.put(index("X" + i), new HashMap<>());
        }
        //计算转移概率
        this.tranMatrix.get(index("Q")).put(index("Q"), 0.01);
        this.tranMatrix.get(index("Q")).put(index("K"), 0.99 / (alpha + 1));
        this.tranMatrix.get(index("K")).put(index("Q"), 1.0 / (alpha + 1));
        this.tranMatrix.get(index("X0")).put(index("Q"), 0.5);
        this.tranMatrix.get(index("Q")).put(index("X0"), (double) alpha * 0.99 / (alpha + 1));
        this.tranMatrix.get(index("X" + (this.xnode - 1))).put(index("K"), 0.5);
        this.tranMatrix.get(index("K")).put(index("X" + (this.xnode - 1)), (double) alpha / (alpha + 1));
        for (int i = 0; i < this.xnode - 1; i++) {
            this.tranMatrix.get(index("X" + i)).put(index("X" + (i + 1)), 0.5);
            this.tranMatrix.get(index("X" + (i + 1))).put(index("X" + i), 0.5);
        }




    }

    public double kHitPossibility(int iteration) {
        DMatrixRMaj entity = new DMatrixRMaj(this.xnode + 2,1);
        DMatrixRMaj tmp = new DMatrixRMaj(this.xnode + 2,1);
        DMatrixSparseCSC mat1 = new DMatrixSparseCSC(this.xnode + 2, this.xnode + 2);
        DMatrixSparseTriplet data = new DMatrixSparseTriplet(this.xnode + 2, this.xnode + 2, 2 * (this.xnode + 2) + 1);
        data.addItem(index("Q"), index("Q"), this.tranMatrix.get(index("Q")).get(index("Q")));
        data.addItem(index("K"), index("Q"), this.tranMatrix.get(index("Q")).get(index("K")));
        data.addItem(index("Q"), index("K"), this.tranMatrix.get(index("K")).get(index("Q")));
        data.addItem(index("Q"), index("X0"), this.tranMatrix.get(index("X0")).get(index("Q")));
        data.addItem(index("X0"), index("Q"), this.tranMatrix.get(index("Q")).get(index("X0")));
        data.addItem(index("X" + (this.xnode - 1)), index("K"), this.tranMatrix.get(index("K")).get(index("X" + (this.xnode - 1))));
        data.addItem(index("K"), index("X" + (this.xnode - 1)), this.tranMatrix.get(index("X" + (this.xnode - 1))).get(index("K")));
        for (int i = 0; i < this.xnode - 1; i++) {
            data.addItem(index("X" + (i + 1)), index("X" + i), this.tranMatrix.get(index("X" + i)).get(index("X" + (i + 1))));
            data.addItem(index("X" + i), index("X" + (i + 1)), this.tranMatrix.get(index("X" + (i + 1))).get(index("X" + i)));
        }
        ConvertDMatrixStruct.convert(data, mat1);
        entity.set(index("Q"), 0, 1);

        while ((iteration--) > 0) {
            CommonOps_DSCC.mult(mat1, entity, tmp);
            entity.set(tmp);
            //entity.print();
        }
        //System.out.println("[" + alpha + "," + (xnode + 1) + "," + entity.get(index("K"), 0) + "],");
        return entity.get(index("K"), 0);
    }

    private Integer index(String node) {
        return this.nodeMap.get(node);
    }

    public static void main(String[] args) {
        DecimalFormat df = new DecimalFormat("#.0");
        double alpha = 0.1;
        for (int hop = 2; hop <= 10; hop++) {
            while (alpha <= 10) {
                HopModel hopModel = new HopModel(alpha, hop);
                heatmap.get(hop).put(alpha, hopModel.kHitPossibility(15000));

                if (alpha < 1.0) {
                    alpha += 0.1;
                }
                else {
                    alpha += 1.0;
                }
                alpha = Double.parseDouble(df.format(alpha));
            }
            alpha = 0.1;
        }
        System.out.println(heatmap);
        //System.out.print("[");
        alpha = 1;
        for (int hop = 2; hop <= 10; hop++) {
            //System.out.print("[");
            while (alpha <= 10) {
                System.out.print(heatmap.get(hop).get(alpha));
                if (alpha != 10) System.out.print("\t");
                if (alpha < 1.0) {
                    alpha += 0.1;
                }
                else {
                    alpha += 1;
                }
                alpha = Double.parseDouble(df.format(alpha));
            }
            //System.out.println();
            System.out.println();
            alpha = 1;
        }
        //System.out.println("]");
    }
}
//Double.parseDouble(df.format(i))