package code.service;

import code.graph.Graph;

import java.io.*;

public class GraphSerializer {
    public static void serialize(Graph g, String objFile) throws IOException {
        FileOutputStream fos = new FileOutputStream(objFile);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(g);
        oos.close();
        fos.close();
    }

    public static Graph antiSerialize(String objFile) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(objFile);
        ObjectInputStream ois = new ObjectInputStream(fis);
        return (Graph) ois.readObject();
    }
}
