package code.graph.index;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class MST {
    private Integer root;//MST树根
    private int k = 0;
    HashMap<Integer, Node> tree = new HashMap<>();//MST中节点序号对应节点实体

    public MST(Integer root, HashMap<Integer, HashMap<Integer, Integer>> weight){
        this.root = root;
        for (int i : weight.keySet()) {
            if (i == this.root.intValue()) continue;//不考虑遍历root
            for (int j : weight.get(i).keySet()) {
                int ri = weight.get(root).get(i);
                int rj = weight.get(root).get(j);
                int ij = weight.get(i).get(j);
                int min = Math.min(ri < rj ? ri : rj, ij);
                this.k = Math.max(this.k, min);
                weight.get(i).replace(j, min);
            }
        }
        for (int i : weight.get(this.root).keySet()) {
            this.tree.put(i, new Node(i));
        }
        //贪心算法构造MST
        for (int i = this.k; i >= 2; i--) {
            for (int x : weight.keySet()) {
                if (x == this.root.intValue()) continue;
                for (int y : weight.get(x).keySet()){
                    Node xnode = this.tree.get(x);
                    Node ynode = this.tree.get(y);
                    if (weight.get(x).get(y) == i && !isReachable(xnode, ynode)) {
                        xnode.add(ynode, weight.get(x).get(y));
                    }
                }
            }
        }
    }

    public boolean isReachable(Node x, Node y) {
        //从x开始遍历，是否能到达y
        Queue<Node> q = new LinkedList<>();
        HashSet<Node> visited = new HashSet<>();
        q.addAll(x.neibor.keySet());
        visited.addAll(x.neibor.keySet());
        while (!q.isEmpty()) {
            if (q.contains(y))
                return true;
            Node cur = q.poll();
            for (Node node : cur.neibor.keySet()) {
                if (!visited.contains(node)) {// 还没被遍历过
                    visited.add(node);
                    q.add(node);
                }
            }
        }
        return false;
    }

    //获得root所构成的ego-network的部分k-truss
    public HashSet<Integer> getLocalTruss(int k) {
        HashSet<Integer> localTruss = new HashSet<>();
        Queue<Node> q = new LinkedList<>();
        for (int serial : this.tree.keySet()) {// BFS遍历, 将遍历过程中边的权重大于等于k的加入
            Node node = this.tree.get(serial);
            for (Node next : node.neibor.keySet()) {
                if (node.neibor.get(next) >= k && !localTruss.contains(next.serial)) {
                    localTruss.add(next.serial);
                    q.add(next);
                }
            }
            while (!q.isEmpty()) {
                Node cur = q.poll();
                for (Node next : cur.neibor.keySet()) {
                    if (cur.neibor.get(next) >= k && !localTruss.contains(next.serial)) {
                        localTruss.add(next.serial);
                        q.add(next);
                    }
                }
            }
        }
        return localTruss;
    }

    class Node {
        private HashMap<Node, Integer> neibor = new HashMap<>();// 当前节点到邻居的权重
        private Integer serial;// 当前节点序号
        public Node(Integer serial) {
            this.serial = serial;
        }
        public void add(Node another, int weight) {
            this.neibor.put(another, weight);
        }
    }
}
