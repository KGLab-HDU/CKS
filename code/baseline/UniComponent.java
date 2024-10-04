package code.baseline;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class UniComponent {//不修改graph内容，只根据graph得到component
    private final HashMap<Integer, Boolean> visited = new HashMap<>();//是否被遍历过
    private final HashMap<Integer, Integer> cid = new HashMap<>();//node对应的连通分量id
    private int cnt;//连通分量数量
    private HashMap<Integer, HashSet<Integer>> graph;//接收来自Graph的graph
    private final HashMap<Integer, HashSet<Integer>> component = new HashMap<>();

    public UniComponent(HashMap<Integer, HashSet<Integer>> graph) {
        this.graph = graph;
        this.cnt = 0;
        this.markComponent();
        this.generateComponent();
    }

    //通过BFS方式确定各自连通分量
    public void markComponent() {
        //初始化访问位, 连通分量编号
        this.graph.keySet().forEach(node -> {
            this.visited.put(node, false);
            this.cid.put(node, 0);
        });
        //确定连通分量编号
        this.graph.keySet().forEach(node -> {
            if (!this.visited.get(node)) {
                BFS(node);
                this.cnt++;
            }
        });
    }


    private void BFS(int node) {
        Queue<Integer> q = new LinkedList<>();
        this.visited.replace(node, true);
        this.cid.replace(node, cnt);
        q.add(node);
        while (!q.isEmpty()) {
            int head = q.poll();
            this.graph.get(head).forEach(i -> {
                if (!this.visited.get(i)){
                    q.add(i);
                    this.cid.replace(i, this.cnt);
                    this.visited.replace(i, true);
                }
            });
        }
    }

    private void generateComponent(){
        this.cid.keySet().forEach(node -> {
            int c = this.cid.get(node);
            if (this.component.get(c) == null)
                this.component.put(c, new HashSet<>());
            this.component.get(c).add(node);
        });
    }
    public HashMap<Integer, Integer> getCid() {
        return cid;
    }

    public int getCnt() {
        return cnt;
    }

    public HashMap<Integer, HashSet<Integer>> getComponent() {
        return component;
    }
}
