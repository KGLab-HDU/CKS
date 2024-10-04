package code.runnable;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import code.approximate.RWAvgSkewUB;
import code.approximate.RandWalk;
import code.baseline.CKSBottomUp;
import code.graph.RWGraph;
import code.service.ObjectSerializer;
import code.service.RWGraphSerializer;

import java.io.IOException;
import java.util.*;

public class BadNodesFeature {
    private final static Logger logger = Logger.getLogger("InfoLogger");

    /**
     * data=facebook type=5 alpha=0.5
     *
     * @param args
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        String file = args[0];
        String type = args[1];
        int scope = Integer.parseInt(args[2]);
        double alpha = Double.parseDouble(args[3]);
        int iteration = Integer.parseInt(args[4]);

        FileAppender fileAppender = (FileAppender) logger.getAppender("I");
        fileAppender.setFile("./precision/feature/" + file + "_type" + type + "_scope" + scope + "_alpha" + alpha + "_it" + iteration + ".log");
        fileAppender.activateOptions();
        fileAppender = (FileAppender) Logger.getRootLogger().getAppender("D");
        fileAppender.setFile("./precision/feature/" + "$debug_" + file + "_type" + type + "_scope" + scope + "_alpha" + alpha + "_it" + iteration + ".log");
        fileAppender.activateOptions();

        logger.debug("读取RWGraph数据");
        RWGraph rwg = RWGraphSerializer.antiSerialize("./rwgraph/" + file + ".dat");
        logger.debug("RWGraph数据读取完成");
        rwg.computeTrussness();

        //遍历过程精确率从低到高
        HashMap<Integer, Double> precision =
                (HashMap<Integer, Double>) ObjectSerializer.antiSerialize("./precision/" + file + "_type" + type + "_scope" + scope + "_alpha" + alpha + "_it" + iteration + "precision.dat");
        LinkedList<Map.Entry<Integer, Double>> sortedPrecision = new LinkedList<>();
        precision.entrySet().forEach(entry -> {
            sortedPrecision.add(entry);
        });
        sortedPrecision.sort((o1, o2) -> {
            return o1.getValue().compareTo(o2.getValue());
        });

        for (Map.Entry<Integer, Double> entry : sortedPrecision) {
            int j = entry.getKey();
            HashMap<Integer, Double> possible = new HashMap<>();//当前节点到邻居的转移概率
            //HashMap<Integer, Double> nAvgSup = new HashMap<>();//邻居的avgSupport
            //HashMap<Integer, Double> nASR = new HashMap<>();//邻居的Average Support Refiniment
            HashMap<Boolean, HashSet<Integer>> inKM = new HashMap<>();//邻居是否在KM里面
            inKM.put(false, new HashSet<>());
            inKM.put(true, new HashSet<>());

            HashSet<Integer> source = new HashSet<>();
            source.add(j);
            logger.info("查询节点: " + source);
            CKSBottomUp cksBottomUp = new CKSBottomUp("./graph/" + file + ".dat", source, true);//计算keymember，方便后面查看坏点邻居分布情况
            cksBottomUp.computeKeyMember();
            HashSet<Integer> keymem = cksBottomUp.getSecondTruss().getG().getNodes();

            RandWalk rw = new RWAvgSkewUB(rwg, source, 2, 0, alpha);
            rw.selectNodes();
            rw.computePossibility();

            double avgSup = rwg.getNodeAvgSup().get(j);//平均support
            double skew = rwg.getNodeSupSkew().get(j);//skew
            //获得该点的trussness
            int nodeTrussness = 0;
            double avgTrussness = 0;
            double KMPossibleSum = 0;
            for (int w : rwg.getGraph().get(j)) {//遍历j的邻居
                if (rwg.getTrussness(j, w) > nodeTrussness) {//计算节点trussness
                    nodeTrussness = rwg.getTrussness(j, w);
                    avgTrussness += rwg.getTrussness(j, w);
                }
                if (rw.getNode2index().keySet().contains(w)) {//在游走范围内
                    possible.put(w, rw.getTransitionMatrix().get(j).get(w));
                    //nAvgSup.put(w, rwg.getNodeAvgSup().get(w));
                    //nASR.put(w, rwg.getNodeAvgSup().get(w) * avgRefine(rwg.getNodeSupSkew().get(w), alpha));
                    if (keymem.contains(w)) {
                        inKM.get(true).add(w);//在km里面
                    } else {
                        inKM.get(false).add(w);//不在km里面
                    }
                }
            }

            logger.info("当前节点到KeyMember的最短距离: " + rwg.getNodeMinDist(j, keymem));
            logger.info("当前节点到KeyMember的最长距离: " + rwg.getNodeMaxDist(j, keymem));

            //得到km和计算范围的交集
            HashSet<Integer> keyMem$Bound = new HashSet<>();
            keyMem$Bound.addAll(keymem);
            keyMem$Bound.retainAll(rw.getNode2index().keySet());
            //计算it步转移概率
            //构建转移矩阵
            logger.debug("开始计算" + iteration + "步转移概率");
            double[] rank = rw.rank(j, iteration).data;
            logger.debug(iteration + "步转移概率计算完成");
            for (Integer km : keyMem$Bound) {
                int kmIndex = rw.getNode2index().get(km);
                KMPossibleSum += rank[kmIndex];
            }

            avgTrussness /= rwg.getGraph().get(j).size();
            logger.info("当前节点Degree: " + rwg.getGraph().get(j).size() + " -> " + entry.getValue());
            logger.info("当前节点邻居在KeyMember中的比例: " + (double) inKM.get(true).size() / rwg.getGraph().get(j).size());
            logger.info("当前节点转移到KeyMember的总" + iteration + "步概率: " + KMPossibleSum);
            logger.info("当前节点平均Support: " + avgSup);
            logger.info("当前节点的Trussness: " + nodeTrussness);
            logger.info("当前节点经过Skew优化后的AvgSupport: " + avgSup * avgRefine(skew, alpha));
            logger.info("邻居边的平均Trussness: " + avgTrussness);
            logger.info("-----------------------------------------------------------------------");
        }
    }

    public static double avgRefine(double skew, double alpha) {
        double e = Math.pow(Math.E, skew);
        double result = 1 + alpha * (1.0 / (1 + e) - 0.5);
        return result;
    }
}
