package code.service;

import java.io.*;

public class ObjectSerializer {
    public static void serialize(Object g, String objFile) throws IOException {
        FileOutputStream fos = new FileOutputStream(objFile);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(g);
        oos.close();
        fos.close();
    }

    public static Object antiSerialize(String objFile) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(objFile);
        ObjectInputStream ois = new ObjectInputStream(fis);
        return ois.readObject();
    }
}
