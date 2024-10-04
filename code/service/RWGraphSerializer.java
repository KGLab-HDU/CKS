package code.service;

import code.graph.RWGraph;

import java.io.*;

public class RWGraphSerializer {
    public static void serialize(RWGraph g, String objFile) throws IOException {
        FileOutputStream fos = new FileOutputStream(objFile);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(g);
        oos.close();
        fos.close();
    }

    public static RWGraph antiSerialize(String objFile) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(objFile);
        ObjectInputStream ois = new ObjectInputStream(fis);
        return (RWGraph) ois.readObject();
    }
}
