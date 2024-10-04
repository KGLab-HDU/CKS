package code.runnable;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import code.approximate.*;
import code.baseline.CKSBottomUp;
import code.graph.RWGraph;
import code.service.RWGraphSerializer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

public class PRinFirstTruss {
    private final static Logger logger = Logger.getLogger("InfoLogger");

    /**
     * data=facebook [0精确率, 1召回率] type=[0,1,2,3,4,5] scope=2 alpha=0.5 step=1 bound=5 iteration=200
     *
     * @param args
     * @throws IOException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
        String file = args[0];
        int pr = Integer.parseInt(args[1]);//精确率还是召回率
        int type = Integer.parseInt(args[2]);
        int scope = Integer.parseInt(args[3]);
        double alpha = Double.parseDouble(args[4]);
        int step = Integer.parseInt(args[5]);
        int bound = Integer.parseInt(args[6]);
        int iteration = Integer.parseInt(args[7]);
        String typeStr = pr == 0 ? "precision" : "recall";

        FileAppender fileAppender = (FileAppender) logger.getAppender("I");
        fileAppender.setFile("PRinFTruss/" + file + "_" + typeStr + "_type" + type + "_scope" + scope + "_alpha" + alpha + "_it" + iteration + ".log");
        fileAppender.activateOptions();
        fileAppender = (FileAppender) Logger.getRootLogger().getAppender("D");
        fileAppender.setFile("PRinFTruss/" + "$debug_" + file + "_" + typeStr + "_type" + type + "_scope" + scope + "_alpha" + alpha + "_it" + iteration + ".log");
        fileAppender.activateOptions();

        LinkedList<Integer> source = new LinkedList<>();
        File sourceFile = new File("query/" + file + ".txt");
        BufferedReader br = new BufferedReader(new FileReader(sourceFile));
        String line = br.readLine();
        String[] node = line.split(" ");
        for (int i = 0; i < node.length; i++) { // 将备选查询节点集加入
            source.add(Integer.parseInt(node[i]));
        }
        HashMap<Integer, Double> acc1 = new HashMap<>();// 无优化精确率
        HashMap<Integer, Double> acc2 = new HashMap<>();// 优化后精确率

        for (int n = 1; n <= 4; n++) {
            acc1.put(n, 0.0);
            acc2.put(n, 0.0);
        }
        System.out.println();
        if (bound > source.size()) {
            bound = source.size();
            logger.debug("查询点数量过大，调整为: " + bound);
        }

        int cnt = 0;
        for (int i = 0; i < bound; i += step) {
            HashSet<Integer> query = new HashSet<>();
            for (int j = i; j < i + step && j < source.size(); j++) {// 选择当前的节点
                query.add(source.get(j));
            }
            HashMap<Integer, Map.Entry<Double, Double>> res = computeP(query, file, type, scope, alpha, iteration);
            acc1.replace(1, acc1.get(1) + res.get(1).getKey());
            acc2.replace(1, acc2.get(1) + res.get(1).getValue());
            acc1.replace(2, acc1.get(2) + res.get(2).getKey());
            acc2.replace(2, acc2.get(2) + res.get(2).getValue());
            acc1.replace(3, acc1.get(3) + res.get(3).getKey());
            acc2.replace(3, acc2.get(3) + res.get(3).getValue());
            acc1.replace(4, acc1.get(4) + res.get(4).getKey());
            acc2.replace(4, acc2.get(4) + res.get(4).getValue());
            logger.info(typeStr + " 1: " + res.get(1).getKey());
            logger.info(typeStr + " 2: " + res.get(2).getKey());
            logger.info(typeStr + " 3: " + res.get(3).getKey());
            logger.info(typeStr + " 4: " + res.get(4).getKey());
            logger.info("优化后" + typeStr + " 1: " + res.get(1).getValue());
            logger.info("优化后" + typeStr + " 2: " + res.get(2).getValue());
            logger.info("优化后" + typeStr + " 3: " + res.get(3).getValue());
            logger.info("优化后" + typeStr + " 4: " + res.get(4).getValue());
            cnt++;
        }

        for (int n = 1; n <= 4; n++) {
            logger.info(n + "倍|km|, 无优化平均" + typeStr + ": " + acc1.get(n) / cnt);
            logger.info(n + "倍|km|, 优化后平均精确率" + typeStr + ": " + acc2.get(n) / cnt);
        }
    }

    public static HashMap<Integer, Map.Entry<Double, Double>> computeP(HashSet<Integer> query, String gFilePath, int type, int scope, double alpha, int iteration) throws IOException, InterruptedException, ClassNotFoundException {
        CKSBottomUp cksBottomUp = new CKSBottomUp("./graph/" + gFilePath + ".dat", query, true);
        cksBottomUp.computeKeyMember();
        HashSet<Integer> validation = new HashSet<>();
        validation.addAll(cksBottomUp.getFirstTruss().getG().getGraph().keySet());// 验证集

        HashMap<Integer, Map.Entry<Double, Double>> res = new HashMap<>();
        RWGraph rwGraph = RWGraphSerializer.antiSerialize("./rwgraph/" + gFilePath + ".dat");

        for (int topn = 1; topn <= 4; topn++) {
            int top = cksBottomUp.getSecondTruss().getG().getGraph().keySet().size();
            if (topn == 1)
                top = cksBottomUp.getSecondTruss().getG().getGraph().keySet().size();
            else if (topn == 2)
                top = (int) (validation.size() * 0.25) < 2 * top ? 2 * top : (int) (validation.size() * 0.25);
            else if (topn == 3)
                top = (int) (validation.size() * 0.5) < 3 * top ? 3 * top : (int) (validation.size() * 0.5);
            else if (topn == 4)
                top = validation.size();

            logger.debug("获得topn个数: " + top);
            if (top > validation.size()) {
                logger.debug(topn + "倍|km|超过验证集大小" + validation.size() + " - 赋值为验证集大小");
                top = validation.size();
            }
            RandWalk rw;
            switch (type) {
                case 0:
                    rw = new RandWalk(rwGraph, query, scope, top);
                    break;
                case 1:
                    rw = new RWAvgsup(rwGraph, query, scope, top);
                    break;
                case 2:
                    rw = new RWAvgSkew(rwGraph, query, scope, top, alpha);
                    break;
                case 3:
                    rw = new RWAvgUB(rwGraph, query, scope, top);
                    break;
                case 4:
                    rw = new RWAvgSkewUB(rwGraph, query, scope, top, alpha);
                    break;
                case 5:
                    rw = new RWTrueTrussness(rwGraph, query, scope, top);
                    break;
                default:
                    rw = new RandWalk(rwGraph, query, scope, top);
            }
            rw.selectNodes();
            rw.computePossibility();
            LinkedList<Integer> keyMember = rw.TopN(iteration);// 裸跑
            LinkedList<Integer> opti = rw.optimization(keyMember);// 优化

            HashSet<Integer> v1 = new HashSet<>();
            HashSet<Integer> v2 = new HashSet<>();
            v1.addAll(validation);
            v2.addAll(validation);
            v1.retainAll(keyMember);
            v2.retainAll(opti);
            res.put(topn, Map.entry((double) v1.size() / keyMember.size(), (double) v2.size() / opti.size()));
        }
        //返回一个无优化，一个优化精确率
        return res;
    }

    /*public static Map.Entry<Double, Double> computeR(HashSet<Integer> query, String gFilePath, int type, int scope, double alpha, int iteration) throws IOException, InterruptedException, ClassNotFoundException {
        CKSBottomUp cksBottomUp = new CKSBottomUp("./graph/" + gFilePath + ".dat", query);
        cksBottomUp.computeKeyMember();
        HashSet<Integer> validation = new HashSet<>();
        validation.addAll(cksBottomUp.getFirstTruss().getG().getGraph().keySet());// 验证集

        int top = topn == 4 ? validation.size() : topn * cksBottomUp.getSecondTruss().getG().getGraph().size();
        if (top > validation.size()){
            logger.debug(topn + "倍|km|超过验证集大小" + validation.size() + " - 赋值为验证集大小");
            top = validation.size();
        }

        RWGraph rwGraph = RWGraphSerializer.antiSerialize("./rwgraph/" + gFilePath + ".dat");
        RandWalk rw;
        switch (type) {
            case 0:
                rw = new RandWalk(rwGraph, query, scope, top);
                break;
            case 1:
                rw = new RWAvgsup(rwGraph, query, scope, top);
                break;
            case 2:
                rw = new RWAvgSkew(rwGraph, query, scope, top, alpha);
                break;
            case 3:
                rw = new RWAvgUB(rwGraph, query, scope, top);
                break;
            case 4:
                rw = new RWAvgSkewUB(rwGraph, query, scope, top, alpha);
                break;
            case 5:
                rw = new RWTrueTrussness(rwGraph, query, scope, top);
                break;
            default:
                rw = new RandWalk(rwGraph, query, scope, top);
        }
        rw.selectNodes();
        rw.computePossibility();
        LinkedList<Integer> keyMember = rw.TopN(iteration);// 裸跑
        LinkedList<Integer> opti = rw.optimization(keyMember);// 优化

        keyMember.retainAll(validation);
        opti.retainAll(validation);
        return Map.entry((double)keyMember.size() / validation.size(), (double)opti.size() / validation.size());
    }
}*/
}
/*
调用方法
CKSBottomUp cksBottomUp = new CKSBottomUp("artist.txt", source);
cksBottomUp.computeKeyMember();

RWGraph rwGraph = new RWGraph("artist.txt");
RWAvgsup rw = new RWAvgsup(rwGraph, source, 2, km.size());
rw.selectNodes();
rw.computePossibility();
LinkedList<Integer> akm = rw.TopN(200);
LinkedList<Integer> rakm = rw.optimization(akm);
*/