package code.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

public class ScopeLimitator {
    private final HashMap<Integer, HashSet<Integer>> graph = new HashMap<>();
    private String file;
    private final HashSet<Integer> nodes = new HashSet<>();
    private int scope;

    public ScopeLimitator(String file, int scope) {
        this.file = file;
        this.scope = scope;
    }

    //输入查询节点，生成查询节点周围scope范围内的点
    public HashMap<Integer, HashSet<Integer>> getScope(HashSet<Integer> source) throws IOException {
        this.graph.clear();
        this.nodes.clear();
        HashSet<Integer> buffer = new HashSet<>();
        this.nodes.addAll(source);
        source.forEach(query -> {
            this.graph.put(query, new HashSet<>());
        });
        //对于i-scope，需要遍历i + 1次，第i + 1次遍历加入第i次加入的点之间的边
        for (int i = 1; i <= this.scope + 1; i++) {
            System.out.println("第" + i + "次遍历");
            /*
            * 对于每一个循环
            * 1. 加入与当前所包含节点直接相邻的节点
            * 2. 将已经在nodes里面的点的边加入图中
            * */
            FileReader fr = new FileReader(this.file + ".txt");
            BufferedReader br = new BufferedReader(fr);
            String line = "";
            int cnt = 0;
            while ((line = br.readLine()) != null) {
                ++cnt;
                String[] nodeStr = line.split(" ");
                int node1 = Integer.parseInt(nodeStr[0]);
                int node2 = Integer.parseInt(nodeStr[1]);
                if (this.nodes.contains(node1) && this.nodes.contains(node2)) {
                    //都包含在已加入的点集中，添加边到子途中
                    this.graph.get(node1).add(node2);
                    this.graph.get(node2).add(node1);
                }
                else if (this.nodes.contains(node1)) {
                    buffer.add(node2);
                }
                else if (this.nodes.contains(node2)) {
                    buffer.add(node1);
                }
            }
            //如果不是第scope + 1次，将buffer中的点加入nodes供下一次遍历时做加边操作
            if (i < scope + 1) {
                this.nodes.addAll(buffer);
                buffer.forEach(node -> {
                    this.graph.put(node, new HashSet<>());
                });
            }
            buffer.clear();
            br.close();
            fr.close();
        }
        return this.graph;
    }

    //demo
    /*public static void main(String[] args) throws IOException, ClassNotFoundException {
        long start = System.currentTimeMillis();
        ScopeLimitator sl = new ScopeLimitator("orkut", 3);
        HashSet<Integer> source = new HashSet<>();
        source.add(60370);
        HashMap<Integer, HashSet<Integer>> subgraph = sl.getScope(source);
        System.out.println(subgraph.keySet().size());
        System.out.println((double) (System.currentTimeMillis() - start) / 1000 + "s");
    }*/
}
