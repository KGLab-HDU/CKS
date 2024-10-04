package code.select;

import org.apache.log4j.Logger;
import code.baseline.CKSBottomUp;
import code.graph.Graph;
import code.service.GraphSerializer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

public class KMSelect {
    private final static Logger logger = Logger.getLogger("InfoLogger");

    /**
     * 随机选取两个点作为查询节点，找到其km的一跳邻居
     * @param args
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        String file = args[0];

        Graph graph = GraphSerializer.antiSerialize("./graph/" + file + ".dat");
        LinkedList<Integer> nodes = new LinkedList<>();
        nodes.addAll(graph.getNodes());

        Random rd = new Random(System.currentTimeMillis());//选择查询节点
        int node1 = nodes.get(rd.nextInt(nodes.size()));
        int node2 = nodes.get(rd.nextInt(nodes.size()));
        HashSet<Integer> source = new HashSet<>();
        source.add(node1);
        source.add(node2);

        //得到两个查询节点对应的KM
        CKSBottomUp cksBottomUp = new CKSBottomUp("./graph/" + file + ".dat", source, true);
        boolean exist = cksBottomUp.computeKeyMember();
        HashSet<Integer> keyMember = new HashSet<>();
        if (exist) {
            //存在包含查询节点的社区，也就是存在KeyMember
            keyMember.addAll(cksBottomUp.getSecondTruss().getG().getNodes());
        }
        HashSet<Integer> candidate = graph.nodeInScope(keyMember, 1);//找到候选查询节点
        LinkedList<Map.Entry<Integer, Double>> sorted = graph.getNodeDistSort(keyMember, candidate);
        LinkedList<Integer> sortedCandidate = new LinkedList<>();
        sorted.forEach(entry -> {
            sortedCandidate.add(entry.getKey());
        });
        BufferedWriter br = new BufferedWriter(new FileWriter("./query/" + file + ".txt"));
        for (int node : sortedCandidate) {
            br.write(String.valueOf(node));
            br.write(" ");
        }
        br.close();
    }
}
