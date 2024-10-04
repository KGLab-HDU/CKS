package code.runnable;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import code.approximate.RWAvgSkew;
import code.approximate.RWAvgSkewUB;
import code.approximate.RWAvgsup;
import code.approximate.RandWalk;
import code.baseline.CKSBottomUp;
import code.graph.RWGraph;
import code.service.RWGraphSerializer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class PaperExtentionExperiment {
    private final static Logger logger = Logger.getLogger("InfoLogger");

    private final static int[] m = new int[]{1, 2, 3};// 游走范围
    private final static int[] r = new int[]{10, 30, 50, 70, 90, 110, 130, 150, 170 ,190, 210, 230, 250};// 迭代次数
    private final static double[] alpha = new double[]{0.5, 1, 1.5, 2};// alpha
    private final static double[] f = new double[]{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0};// top-N
    private final static int[] Q = new int[]{1, 2, 3};// 查询节点个数

    // 参数顺序: bound r alpha f Q
    private final static int[] dftParam = new int[]{1, 7, 1, 9, 0};

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        String alg = args[0];
        String data = args[1];
        int n = Integer.parseInt(args[2]);// 总共跑多少节点
        boolean hasAlpha = false;
        if (alg.equals("ASUB") || alg.equals("ASkew")) hasAlpha = true;

        ArrayList<Integer> source = new ArrayList<>();

        FileAppender fileAppender = (FileAppender) logger.getAppender("I");
        fileAppender.setFile("./extends/" + alg + "_" + data + ".log");
        fileAppender.activateOptions();
        fileAppender = (FileAppender) Logger.getRootLogger().getAppender("D");
        fileAppender.setFile("./extends/$debug_" + alg + "_" + data + ".log");
        fileAppender.activateOptions();

        //读取查询节点
        BufferedReader br = new BufferedReader(new FileReader("./query/" + data + ".txt"));
        for (String node : br.readLine().split(" ")) {
            source.add(Integer.parseInt(node));
        }
        br.close();
        if (n > source.size())
            n = source.size();

        //读取rwg数据
        RWGraph rwGraph = RWGraphSerializer.antiSerialize("./rwgraph/" + data + ".dat");
        //初始化结果存储器
        HashMap<String, Double> precision = new HashMap<>();
        HashMap<String, Double> recall = new HashMap<>();
        HashMap<String, Double> time = new HashMap<>();
        for (int j = 0; j < m.length; j++) precision.put("m" + m[j], 0.0);
        for (int j = 0; j < r.length; j++) precision.put("r" + r[j], 0.0);
        for (int j = 0; j < alpha.length; j++) precision.put("alpha" + alpha[j], 0.0);
        for (int j = 0; j < f.length; j++) precision.put("f" + f[j], 0.0);
        for (int j = 0; j < f.length; j++) recall.put("f" + f[j], 0.0);
        for (int j = 0; j < Q.length; j++) precision.put("Q" + Q[j], 0.0);

        for (int j = 0; j < m.length; j++) time.put("m" + m[j], 0.0);
        for (int j = 0; j < r.length; j++) time.put("r" + r[j], 0.0);
        for (int j = 0; j < alpha.length; j++) time.put("alpha" + alpha[j], 0.0);
        for (int j = 0; j < f.length; j++) time.put("f" + f[j], 0.0);
        for (int j = 0; j < Q.length; j++) time.put("Q" + Q[j], 0.0);

        // 遍历查询节点
        long rwtime = 0;
        double p = 0.0;
        for (int i = 0; i < n; i++) {
            HashSet<Integer> query = new HashSet<>();
            query.add(source.get(i));
            logger.info("查询节点: " + query);
            //获得当前节点的精确解
            CKSBottomUp exactSolution = new CKSBottomUp("./graph/" + data + ".dat", query, true);
            exactSolution.computeKeyMember();
            // 获得验证集
            HashSet<Integer> validation = new HashSet<>();
            validation.addAll(exactSolution.getSecondTruss().getG().getGraph().keySet());
            if (validation.size() == 0)
                continue;
            //实验一: 不同游走范围
            RandWalk randWalk = selectAlg(alg);
            logger.info("Effect of m-bound: ");
            for (int j = 0; j < m.length; j++){
                randWalk.setParam(rwGraph, query, m[j], (int) (f[dftParam[3]] * validation.size()), alpha[dftParam[2]]);
                randWalk.selectNodes();
                randWalk.computePossibility();
                HashSet<Integer> rwKM = new HashSet<>();
                rwKM.addAll(randWalk.TopN(r[dftParam[1]]));// 随机游走结果
                rwtime = randWalk.time;
                HashSet<Integer> vKM = new HashSet<>();
                vKM.addAll(validation);
                vKM.retainAll(rwKM);
                p = (double) vKM.size() / rwKM.size();
                precision.replace("m" + m[j], precision.get("m" + m[j]) + p);// 累加结果
                time.replace("m" + m[j], time.get("m" + m[j]) + rwtime);// 累加结果
                logger.info(m[j] + "-bound结果: ");
                logger.info("随机游走时间: " + rwtime);
                logger.info("精确率: " + p);
            }
            //实验二: 不同迭代次数
            logger.info("Effect of r: ");
            for (int j = 0; j < r.length; j++){
                randWalk.setParam(rwGraph, query, m[dftParam[0]], (int) (f[dftParam[3]] * validation.size()), alpha[dftParam[2]]);
                randWalk.selectNodes();
                randWalk.computePossibility();
                HashSet<Integer> rwKM = new HashSet<>();
                rwKM.addAll(randWalk.TopN(r[j]));// 随机游走结果
                rwtime = randWalk.time;
                HashSet<Integer> vKM = new HashSet<>();
                vKM.addAll(validation);
                vKM.retainAll(rwKM);
                p = (double) vKM.size() / rwKM.size();
                precision.replace("r" + r[j], precision.get("r" + r[j]) + p);// 累加结果
                time.replace("r" + r[j], time.get("r" + r[j]) + rwtime);// 累加结果
                logger.info(r[j] + "次迭代结果: ");
                logger.info("随机游走时间: " + rwtime);
                logger.info("精确率: " + p);
            }
            //实验三: 不同alpha
            if (hasAlpha) {
                logger.info("Effect of alpha: ");
                for (int j = 0; j < alpha.length; j++) {
                    randWalk.setParam(rwGraph, query, m[dftParam[0]], (int) (f[dftParam[3]] * validation.size()), alpha[j]);
                    randWalk.selectNodes();
                    randWalk.computePossibility();
                    HashSet<Integer> rwKM = new HashSet<>();
                    rwKM.addAll(randWalk.TopN(r[dftParam[1]]));// 随机游走结果
                    rwtime = randWalk.time;
                    HashSet<Integer> vKM = new HashSet<>();
                    vKM.addAll(validation);
                    vKM.retainAll(rwKM);
                    p = (double) vKM.size() / rwKM.size();
                    precision.replace("alpha" + alpha[j], precision.get("alpha" + alpha[j]) + p);// 累加结果
                    time.replace("alpha" + alpha[j], time.get("alpha" + alpha[j]) + rwtime);// 累加结果
                    logger.info("alpha = " + alpha[j] + ": ");
                    logger.info("随机游走时间: " + rwtime);
                    logger.info("精确率: " + p);
                }
            }

            //实验四: 不同topn个数
            logger.info("Effect of top-N: ");
            double rcall = 0.0;
            for (int j = 0; j < f.length; j++){
                randWalk.setParam(rwGraph, query, m[dftParam[0]], (int) Math.ceil((f[j] * validation.size())), alpha[dftParam[2]]);
                randWalk.selectNodes();
                randWalk.computePossibility();
                HashSet<Integer> rwKM = new HashSet<>();
                rwKM.addAll(randWalk.TopN(r[dftParam[1]]));// 随机游走结果
                rwtime = randWalk.time;
                HashSet<Integer> vKM = new HashSet<>();
                vKM.addAll(validation);
                vKM.retainAll(rwKM);
                p = (double) vKM.size() / rwKM.size();
                rwKM.retainAll(validation);
                rcall = (double) rwKM.size() / validation.size();
                precision.replace("f" + f[j], precision.get("f" + f[j]) + p);// 累加结果
                recall.replace("f" + f[j], recall.get("f" + f[j]) + rcall);// 累加结果
                time.replace("f" + f[j], time.get("f" + f[j]) + rwtime);// 累加结果
                logger.info("f = " + f[j] + ": ");
                logger.info("随机游走时间: " + rwtime);
                logger.info("精确率: " + p);
            }
        }

        logger.info("Effect of |Q|: ");
        for (int j = 0; j < Q.length; j++){
            for (int i = 0; i < n && i + Q[j] <= n; i += Q[j]){
                HashSet<Integer> query = new HashSet<>();
                for (int k = i; k < i + Q[j]; k++)
                    query.add(source.get(k));
                logger.info("查询节点: " + query);
                //获得当前节点的精确解
                CKSBottomUp exactSolution = new CKSBottomUp("./graph/" + data + ".dat", query, true);
                exactSolution.computeKeyMember();
                // 获得验证集
                HashSet<Integer> validation = new HashSet<>();
                validation.addAll(exactSolution.getSecondTruss().getG().getGraph().keySet());
                if (validation.size() == 0)
                    continue;
                RandWalk randWalk = selectAlg(alg);

                randWalk.setParam(rwGraph, query, m[dftParam[0]], (int) (f[dftParam[3]] * validation.size()), alpha[dftParam[2]]);
                randWalk.selectNodes();
                randWalk.computePossibility();
                HashSet<Integer> rwKM = new HashSet<>();
                rwKM.addAll(randWalk.TopN(r[dftParam[1]]));// 随机游走结果
                rwtime = randWalk.time;
                HashSet<Integer> vKM = new HashSet<>();
                vKM.addAll(validation);
                vKM.retainAll(rwKM);
                p = (double) vKM.size() / rwKM.size();
                precision.replace("Q" + Q[j], precision.get("Q" + Q[j]) + p);// 累加结果
                time.replace("Q" + Q[j], time.get("Q" + Q[j]) + rwtime);// 累加结果
                logger.info("Q = " + Q[j] + ": ");
                logger.info("随机游走时间: " + rwtime);
                logger.info("精确率: " + p);
            }
        }
        for (int j = 0; j < m.length; j++) precision.replace("m" + m[j], precision.get("m" + m[j]) / n);
        for (int j = 0; j < r.length; j++) precision.replace("r" + r[j], precision.get("r" + r[j]) / n);
        for (int j = 0; j < alpha.length; j++) precision.replace("alpha" + alpha[j], precision.get("alpha" + alpha[j]) / n);
        for (int j = 0; j < f.length; j++) precision.replace("f" + f[j], precision.get("f" + f[j]) / n);
        for (int j = 0; j < f.length; j++) recall.replace("f" + f[j], recall.get("f" + f[j]) / n);
        for (int j = 0; j < Q.length; j++) precision.replace("Q" + Q[j], precision.get("Q" + Q[j]) / (n / Q[j]));

        for (int j = 0; j < m.length; j++) time.replace("m" + m[j], time.get("m" + m[j]) / n);
        for (int j = 0; j < r.length; j++) time.replace("r" + r[j], time.get("r" + r[j]) / n);
        for (int j = 0; j < alpha.length; j++) time.replace("alpha" + alpha[j], time.get("alpha" + alpha[j]) / n);
        for (int j = 0; j < f.length; j++) time.replace("f" + f[j], time.get("f" + f[j]) / n);
        for (int j = 0; j < Q.length; j++) time.replace("Q" + Q[j], time.get("Q" + Q[j]) / (n / Q[j]));


        ObjectMapper mapper = new ObjectMapper();
        logger.info(mapper.writeValueAsString(precision));
        logger.info(mapper.writeValueAsString(time));
        logger.info(mapper.writeValueAsString(recall));

    }
    // 参数顺序: bound r alpha f Q
    public static RandWalk selectAlg(String alg) {
        RandWalk randWalk = null;
        switch(alg) {
            case "Basic":
                randWalk = new RandWalk();
                break;
            case "AvgSup":
                randWalk = new RWAvgsup();
                break;
            case "ASkew":
                randWalk = new RWAvgSkew();
                break;
            case "ASUB":
                randWalk = new RWAvgSkewUB();
                break;
            default:
                randWalk = null;
                break;
        }
        return randWalk;
    }

}
