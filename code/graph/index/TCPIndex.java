package code.graph.index;

import code.baseline.TDBottomUp;
import code.util.DeepCopy;

import java.io.IOException;
import java.util.*;

public class TCPIndex {
    private HashMap<Integer, HashSet<Integer>> graph;
    private HashMap<Integer, HashMap<Integer, Integer>> trussness;
    private final HashMap<Integer, MST> tcpIndex = new HashMap<>();

    private int firstK = 0;
    private int secondK = 0;

    public TCPIndex(HashMap<Integer, HashSet<Integer>> graph, HashMap<Integer, HashMap<Integer, Integer>> trussness) {
        this.graph = graph;
        this.trussness = trussness;
    }

    public void indexConstruction() {
        for (int node : this.graph.keySet()) {
            HashMap<Integer, HashMap<Integer, Integer>> weight = new HashMap<>();
            weight.put(node, new HashMap<>());
            for (int neibor: this.graph.get(node)) {
                weight.put(neibor, new HashMap<>());
                weight.get(node).put(neibor, this.trussness.get(node).get(neibor));
            }
            //构造ego-network
            for (int i : this.graph.get(node)) {
                for (int j : this.graph.get(node)){
                    if (this.graph.get(i).contains(j)) {
                        weight.get(i).put(j, this.trussness.get(i).get(j));
                    }
                }
            }
            MST index = new MST(node, weight);
            this.tcpIndex.put(node, index);
        }
    }

    //获得node节点主导的部分k-truss
    public HashSet<Integer> localTruss(int node, int k) {
        MST mst = this.tcpIndex.get(node);
        return mst.getLocalTruss(k);
    }

    public HashSet<Integer> firstTruss(HashSet<Integer> source) {
        Queue<Integer> q = new LinkedList<>();
        HashSet<Integer> truss = new HashSet<>();
        int cur = source.iterator().next();// 仅遍历一个节点
        int k = Integer.MAX_VALUE;
        for (int node : source) { // 包含所有source的truss必定是最大trussness的最小值，这是查找上限
            k = Math.min(k, nodeMaxTrussness(node));
        }
        while (k >= 2) {
            for (int nei : this.graph.get(cur)) {
                if(truss.contains(nei)) continue;
                if(this.trussness.get(cur).get(nei) < k) continue; //遍历所有trussness >= k的节点
                truss.add(nei);
                q.add(nei);
                while (!q.isEmpty()) {
                    int n = q.poll();
                    HashSet<Integer> local = localTruss(n, k);
                    for (int i : local){
                        if (!truss.contains(i)){
                            q.add(i);
                            truss.add(i);
                        }
                    }
                }
            }
            //得到的k-truss没有包含所有的查询节点
            if (!truss.containsAll(source)){
                truss.clear();
                q.clear();
                k--;
            }
            else {
                this.firstK = k;
                break;
            }
        }
        return truss;
    }

    public HashSet<Integer> keyMember(HashSet<Integer> firstTruss) {
        int k = 0;
        for(int node : firstTruss){
            int trussness = nodeMaxTrussness(node);
            if(trussness > k){
                k = trussness;
            }
        }
        this.secondK = k;
        HashSet<Integer> truss = new HashSet<>();
        for (int node : firstTruss) {
            if (nodeMaxTrussness(node) == this.secondK) {
                truss.add(node);
            }
        }
        return truss;
    }

    public int nodeMaxTrussness(int node) {
        int max = 0;
        for (int neibor : this.trussness.get(node).keySet()) {
            max = Math.max(max, this.trussness.get(node).get(neibor));
        }
        return max;
    }

    public int getFirstK() {
        return firstK;
    }

    public int getSecondK() {
        return secondK;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        TDBottomUp tdButtomUp = new TDBottomUp("orkut.txt");
        HashMap<Integer, HashSet<Integer>> graph = (HashMap<Integer, HashSet<Integer>>) DeepCopy.copy(tdButtomUp.getG().getGraph());

        for (int i = 2; !tdButtomUp.getG().getGraph().isEmpty(); i++) {
            System.out.println(tdButtomUp.getG().getGraph().size());
            tdButtomUp.getTruss(i);
        }
        TCPIndex tcp = new TCPIndex(graph, tdButtomUp.getTrussness());
        tcp.indexConstruction();
    }
}
