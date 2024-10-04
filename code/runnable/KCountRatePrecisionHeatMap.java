package code.runnable;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import code.approximate.*;
import code.baseline.CKSBottomUp;
import code.graph.RWGraph;
import code.service.RWGraphSerializer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;

public class KCountRatePrecisionHeatMap {
    private final static Logger logger = Logger.getLogger("InfoLogger");

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        String file = args[0];
        int type = Integer.parseInt(args[1]);
        int scope = Integer.parseInt(args[2]);
        double alpha = Double.parseDouble(args[3]);
        int iteration = Integer.parseInt(args[4]);

        FileAppender fileAppender = (FileAppender) logger.getAppender("I");
        fileAppender.setFile("./heatmap/" + file + "_type" + type + "_scope" + scope + "_alpha" + alpha + "_it" + iteration + ".log");
        fileAppender.activateOptions();
        fileAppender = (FileAppender) Logger.getRootLogger().getAppender("D");
        fileAppender.setFile("./heatmap/" + "$debug_" + file + "_type" + type + "_scope" + scope + "_alpha" + alpha + "_it" + iteration+ ".log");
        fileAppender.activateOptions();

        //获得查询节点
        BufferedReader br = new BufferedReader(new FileReader("./query/" + file + ".txt"));
        String[] nodes = br.readLine().split(" ");
        HashSet<Integer> source = new HashSet<>();
        for (String node : nodes) {
            source.add(Integer.parseInt(node));
        }

        for (int node : source) {
            HashSet<Integer> query = new HashSet<>();
            query.add(node);
            logger.debug("查询节点: " + node);
            //计算精确解
            CKSBottomUp cksBottomUp = new CKSBottomUp("./graph/" + file + ".dat", query, true);
            boolean exist = cksBottomUp.computeKeyMember();
            if (!exist) {
                logger.info("不存在社区");
                continue;
            }
            HashSet<Integer> community = new HashSet<>();
            community.addAll(cksBottomUp.getFirstTruss().getG().getGraph().keySet());
            HashSet<Integer> keyMember = new HashSet<>();
            keyMember.addAll(cksBottomUp.getSecondTruss().getG().getGraph().keySet());
            int firstK = cksBottomUp.getFirstK();
            int secondK = cksBottomUp.getSecondK();
            int cntC = community.size();
            int cntK = keyMember.size();
            //计算近似解
            RWGraph rwGraph = RWGraphSerializer.antiSerialize("./rwgraph/" + file + ".dat");
            RandWalk rw;
            switch (type) {
                case 0:
                    rw = new RandWalk(rwGraph, query, scope, cntK);
                    break;
                case 1:
                    rw = new RWAvgsup(rwGraph, query, scope, cntK);
                    break;
                case 2:
                    rw = new RWAvgSkew(rwGraph, query, scope, cntK, alpha);
                    break;
                case 3:
                    rw = new RWAvgUB(rwGraph, query, scope, cntK);
                    break;
                case 4:
                    rw = new RWAvgSkewUB(rwGraph, query, scope, cntK, alpha);
                    break;
                case 5:
                    rw = new RWTrueTrussness(rwGraph, query, scope, cntK);
                    break;
                default:
                    rw = new RandWalk(rwGraph, query, scope, cntK);
            }
            rw.selectNodes();
            rw.computePossibility();
            LinkedList<Integer> approxiKM = rw.TopN(iteration);// 裸跑
            LinkedList<Integer> opti = rw.optimization(approxiKM);// 优化
            //精确率
            keyMember.retainAll(opti);
            double precision = (double) keyMember.size() / opti.size();
            double rateK = (double) secondK / firstK;
            double rateC = (double) cntK / cntC;
            logger.info(rateC + " " + rateK + " " + precision);
        }

    }
}
