package code.model;

import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.DMatrixSparseTriplet;
import org.ejml.ops.ConvertDMatrixStruct;
import org.ejml.sparse.csc.CommonOps_DSCC;

import java.text.DecimalFormat;
import java.util.HashMap;

public class TrussnessModel {
    private final HashMap<SuperNode, HashMap<SuperNode, Double>> tranMatrix = new HashMap<>();
    private double alpha;
    private double beta;
    private double gamma;

    private static HashMap<Double, HashMap<Double, Double>> heatMap = new HashMap<>();

    static {
        DecimalFormat df = new DecimalFormat("#.0");
        double i = 0.1;
        while (i <= 10) {
            heatMap.put(i, new HashMap<>());

            if (i < 1.0) {
                i += 0.1;
            }
            else {
                i += 1.0;
            }
            i = Double.parseDouble(df.format(i));
        }
        System.out.println(heatMap);
    }

    public TrussnessModel(double alpha, double beta, double gamma){
        this.alpha = alpha;
        this.beta = beta;
        this.gamma = gamma;
        this.tranMatrix.put(SuperNode.Q, new HashMap<>());
        this.tranMatrix.put(SuperNode.X, new HashMap<>());
        this.tranMatrix.put(SuperNode.K, new HashMap<>());
        //六条转移概率
        this.tranMatrix.get(SuperNode.Q).put(SuperNode.K, (double) gamma / (alpha + gamma));
        this.tranMatrix.get(SuperNode.Q).put(SuperNode.X, (double) alpha / (alpha + gamma));
        this.tranMatrix.get(SuperNode.X).put(SuperNode.Q, (double) alpha / (alpha + beta));
        this.tranMatrix.get(SuperNode.X).put(SuperNode.K, (double) beta / (alpha + beta));
        this.tranMatrix.get(SuperNode.K).put(SuperNode.Q, (double) gamma / (beta + gamma));
        this.tranMatrix.get(SuperNode.K).put(SuperNode.X, (double) beta / (beta + gamma));

    }

    //根据tranMatrix的一步转移概率，计算it步后从Q到K的转移概率
    public double kHitPossibility(int iteration) {
        DecimalFormat df = new DecimalFormat("#.0");
        DMatrixRMaj entity = new DMatrixRMaj(3,1);
        DMatrixRMaj tmp = new DMatrixRMaj(3,1);
        DMatrixSparseCSC mat1 = new DMatrixSparseCSC(3, 3);
        DMatrixSparseTriplet data = new DMatrixSparseTriplet(3, 3, 6);
        int Q = SuperNode.Q.ordinal();
        int K = SuperNode.K.ordinal();
        int X = SuperNode.X.ordinal();
        data.addItem(K, Q, this.tranMatrix.get(SuperNode.Q).get(SuperNode.K));
        data.addItem(X, Q, this.tranMatrix.get(SuperNode.Q).get(SuperNode.X));
        data.addItem(K, X, this.tranMatrix.get(SuperNode.X).get(SuperNode.K));
        data.addItem(Q, X, this.tranMatrix.get(SuperNode.X).get(SuperNode.Q));
        data.addItem(Q, K, this.tranMatrix.get(SuperNode.K).get(SuperNode.Q));
        data.addItem(X, K, this.tranMatrix.get(SuperNode.K).get(SuperNode.X));
        ConvertDMatrixStruct.convert(data, mat1);
        entity.set(Q, 0, 1);
        while ((iteration--) > 0) {
            CommonOps_DSCC.mult(mat1, entity, tmp);
            entity.set(tmp);
            //entity.print();
        }
        double ag = Double.parseDouble(df.format(alpha / gamma));
        double bg = Double.parseDouble(df.format(beta / gamma));
        heatMap.get(bg).put(ag, entity.get(K, 0));
        return entity.get(K, 0);
    }

    public static void main(String[] args) {
        double alpha;
        double beta;
        double gamma = 1.0;
        for (double k1 = 1; k1 <= 10; k1 += 1) {
            for (double k2 = 1; k2 <= 10; k2 += 1) {
                beta = gamma * k2;
                alpha = gamma * k1;
                TrussnessModel tm = new TrussnessModel(alpha, beta, gamma);
                tm.kHitPossibility(150);
            }
        }
        for (double k1 = 0.1; k1 <= 1; k1 += 0.1) {
            for (double k2 = 0.1; k2 <= 1; k2 += 0.1) {
                beta = gamma * k2;
                alpha = gamma * k1;
                TrussnessModel tm = new TrussnessModel(alpha, beta, gamma);
                tm.kHitPossibility(150);
            }
        }
        for (double k1 = 0.1; k1 <= 1; k1 += 0.1) {
            for (double k2 = 1; k2 <= 10; k2 += 1) {
                beta = gamma * k2;
                alpha = gamma * k1;
                TrussnessModel tm = new TrussnessModel(alpha, beta, gamma);
                tm.kHitPossibility(150);
            }
        }
        for (double k1 = 1; k1 <= 10; k1 += 1) {
            for (double k2 = 0.1; k2 <= 1; k2 += 0.1) {
                beta = gamma * k2;
                alpha = gamma * k1;
                TrussnessModel tm = new TrussnessModel(alpha, beta, gamma);
                tm.kHitPossibility(150);
            }
        }
        System.out.println(heatMap);
        DecimalFormat df = new DecimalFormat("#.0");
        double i = 1;
        double j = 1;
        //System.out.print("[");
        while (i <= 10){
            //System.out.print("[");
            while (j <= 10) {
                //System.out.println(i + " " + j);
                System.out.print(heatMap.get(i).get(j));
                if (j != 10) System.out.print("\t");
                if (j < 1.0) {
                    j += 0.1;
                }
                else {
                    j += 1.0;
                }
                j = Double.parseDouble(df.format(j));
            }
            //if (i != 10) System.out.print("],");
            //else System.out.print("]");
            System.out.println();
            if (i < 1.0) {
                i += 0.1;
            }
            else {
                i += 1.0;
            }
            i = Double.parseDouble(df.format(i));
            j = 1;
        }
        //System.out.print("]");
    }

}
