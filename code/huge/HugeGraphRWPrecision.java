package code.huge;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import code.approximate.*;
import code.baseline.CKSBottomUp;
import code.graph.Graph;
import code.graph.RWGraph;
import code.service.ObjectSerializer;
import code.service.ScopeLimitator;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

public class HugeGraphRWPrecision {
    private final static Logger logger = Logger.getLogger("InfoLogger");
    //orkut 4 2 1 1 100 150 4
    public static void main(String[] args) throws IOException, InterruptedException {
        String file = args[0];
        int type = Integer.parseInt(args[1]);
        int scope = Integer.parseInt(args[2]);//游走范围
        double alpha = Double.parseDouble(args[3]);
        int step = Integer.parseInt(args[4]);
        int bound = Integer.parseInt(args[5]);//几个点
        int iteration = Integer.parseInt(args[6]);
        int limit = Integer.parseInt(args[7]);//缩小图的范围

        FileAppender fileAppender = (FileAppender) logger.getAppender("I");
        fileAppender.setFile("./rwprecision/" + file + "_type" + type + "_scope" + scope + "_alpha" + alpha + "_it" + iteration + "_limit" + limit + ".log");
        fileAppender.activateOptions();
        fileAppender = (FileAppender) Logger.getRootLogger().getAppender("D");
        fileAppender.setFile("./rwprecision/" + "$debug_" + file + "_type" + type + "_scope" + scope + "_alpha" + alpha + "_it" + iteration + "_limit" + limit + ".log");
        fileAppender.activateOptions();

        final HashMap<Integer, Double> precision = new HashMap<>();//坏点集合

        LinkedList<Integer> source = new LinkedList<>();
        File sourceFile = new File("./query/" + file + ".txt");
        BufferedReader br = new BufferedReader(new FileReader(sourceFile));
        String line = br.readLine();
        String[] node = line.split(" ");
        for (int i = 0; i < node.length; i++) { // 将备选查询节点集加入
            source.add(Integer.parseInt(node[i]));
        }

        if (bound > source.size()) {
            bound = source.size();
            logger.debug("bound过大，调整为: " + bound);
        }

        double acc1 = 0.0;
        double acc2 = 0.0;
        int cnt = 0;
        for (int i = 0; i < bound; i += step) {
            HashSet<Integer> query = new HashSet<>();
            for (int j = i; j < i + step && j < source.size(); j++){// 选择当前的节点
                query.add(source.get(j));
            }
            logger.info("查询节点: " + query);

            //限定大图范围
            logger.debug("限制大图范围，limitation = " + limit);
            ScopeLimitator sl = new ScopeLimitator(file, limit);
            HashMap<Integer, HashSet<Integer>> $graph = sl.getScope(query);
            logger.debug("大小限制，节点数量: " + $graph.keySet().size());

            Map.Entry<Double, Double> res = compute(query, $graph, type, scope, alpha, iteration);
            acc1 += res.getKey();
            acc2 += res.getValue();
            logger.info("精确率: " + res.getKey());
            logger.info("优化后精确率: " + res.getValue());
            //查询节点特征分析
            precision.put(source.get(i), res.getKey());//优化前精确率
            cnt++;
            $graph = null;
            query = null;
        }
        logger.info("无优化平均精确率: " + acc1 / cnt);
        logger.info("优化后平均精确率: " + acc2 / cnt);
        logger.info(precision.toString());
        ObjectSerializer.serialize(precision, "./precision/" + file + "_type" + type + "_scope" + scope + "_alpha" + alpha + "_it" + iteration + "_limit" + limit + "precision.dat");
    }

    public static Map.Entry<Double, Double> compute(HashSet<Integer> query, HashMap<Integer, HashSet<Integer>> $graph, int type, int scope, double alpha, int iteration) throws IOException, InterruptedException {
        CKSBottomUp cksBottomUp = new CKSBottomUp($graph, query);
        cksBottomUp.computeKeyMember();
        HashSet<Integer> validation = new HashSet<>();
        validation.addAll(cksBottomUp.getSecondTruss().getG().getGraph().keySet());// 验证集

        RWGraph rwGraph = new RWGraph($graph);
        RandWalk rw;
        switch (type) {
            case 0:
                rw = new RandWalk(rwGraph, query, scope, validation.size());
                break;
            case 1:
                rw = new RWAvgsup(rwGraph, query, scope, validation.size());
                break;
            case 2:
                rw = new RWAvgSkew(rwGraph, query, scope, validation.size(), alpha);
                break;
            case 3:
                rw = new RWAvgUB(rwGraph, query, scope, validation.size());
                break;
            case 4:
                rw = new RWAvgSkewUB(rwGraph, query, scope, validation.size(), alpha);
                break;
            case 5:
                rw = new RWTrueTrussness(rwGraph, query, scope, validation.size());
                break;
            default:
                rw = new RandWalk(rwGraph, query, scope, validation.size());
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
        double p1 = (double)v1.size() / keyMember.size();
        double p2 = (double)v2.size() / opti.size();
        ArrayList<Integer> arrayList = new ArrayList<>();
        arrayList.addAll(keyMember);
        logger.info("rw: " + density(cksBottomUp.getOriginGraph().getG(), arrayList));
        logger.info(distance(cksBottomUp.getOriginGraph().getG(), arrayList));
        arrayList.clear();
        arrayList.addAll(validation);
        logger.info("truss: " + density(cksBottomUp.getOriginGraph().getG(), arrayList));

        //validation
        //LinkedList<Integer> keyMember = rw.TopN(iteration);
        DecimalFormat df = new DecimalFormat("#.0");
        System.out.println(validation.size() + " " + keyMember.size());
        int valiSize = validation.size();
        for (double i = 0.1; i <= 1; i = Double.parseDouble(df.format(i + 0.1))){
            int topn = (int) (valiSize * i);
            System.out.println(topn);
            LinkedList<Integer> km = rw.getTopNode(iteration, topn);
            HashSet<Integer> vali = new HashSet<>();
            vali.addAll(validation);
            HashSet<Integer> intersection = new HashSet<>();
            intersection.addAll(km);
            intersection.retainAll(validation);
            double precision = ((double) intersection.size() / km.size());
            double recall = ((double) intersection.size() / vali.size());
            double fscore = 2 * precision * recall / (precision + recall);
            logger.info(i + "精确率: " + precision);
            logger.info(i + "召回率: " + recall);
            logger.info(i + "f1-score: " + fscore);
        }

        return Map.entry(p1, p2);
    }

    public static double density(Graph Initial_graph, ArrayList<Integer> a){
        int edge_num = 0;
        Map<Integer, HashSet<Integer>> graph = Initial_graph.getGraph();
        int len = a.size();
        for(int i = 0; i < len; i++){
            HashSet<Integer> nei = graph.get(a.get(i));
            for(int j = 0; j < len; j++){
                if(nei.contains(a.get(j))){
                    edge_num++;
                }
            }
        }
        edge_num /= 2;
        double den = edge_num/(double) a.size();
        return den;
    }

    public static int distance(Graph Initial_graph, ArrayList<Integer> a){
        int max = -1;
        Map<Integer, HashSet<Integer>> graph = Initial_graph.getGraph();
        int len = a.size();
        int[][] adjMatrix = new int[len][len];
        for(int i = 0; i < len; i++){
            HashSet<Integer> nei = graph.get(a.get(i));
            for(int j = 0; j < len; j++){
                if(nei.contains(a.get(j))){
                    adjMatrix[i][j] = 1;
                }else{
                    adjMatrix[i][j] = max;
                }
            }
        }
        getShortestPaths(adjMatrix);
        int diameter = 0;
        for(int i = 0; i < len; i++){
            for(int j = 0; j < len; j++){
                diameter = Math.max(diameter, adjMatrix[i][j]);
            }
        }
        return diameter;
    }

    public static void getShortestPaths(int[][] adjMatrix) {
        for(int k = 0;k < adjMatrix.length;k++) {
            for(int i = 0;i < adjMatrix.length;i++) {
                for(int j = 0;j < adjMatrix.length;j++) {
                    if(adjMatrix[i][k] != -1 && adjMatrix[k][j] != -1) {
                        int temp = adjMatrix[i][k] + adjMatrix[k][j];  //含有中间节点k的顶点i到顶点j的距离
                        if(adjMatrix[i][j] == -1 || adjMatrix[i][j] > temp)
                            adjMatrix[i][j] = temp;
                    }
                }
            }
        }
    }
}
