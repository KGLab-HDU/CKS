package code.util;

import org.apache.log4j.Logger;

import java.io.*;

public class DeepCopy {
    private final static Logger logger = Logger.getLogger("InfoLogger");
    public static Object copy(Object obj) throws IOException, InterruptedException {
        long startTime = System.currentTimeMillis();
        PipedOutputStream pos = new PipedOutputStream();
        PipedInputStream pis = new PipedInputStream();
        pos.connect(pis);
        ObjectWriter ow = new ObjectWriter(obj, pos);
        ObjectReader or = new ObjectReader(pis);

        Thread t1 = new Thread(ow);
        Thread t2 = new Thread(or);
        t1.start();
        t2.start();
        t2.join();
        t1.join();
        long endTime = System.currentTimeMillis();
        logger.debug("深拷贝完成，耗时: " + (double) (endTime - startTime) / 1000);
        return or.obj;
    }
}

class ObjectWriter implements Runnable{
    protected Object obj;
    protected PipedOutputStream pos;

    public ObjectWriter(Object obj, PipedOutputStream pos) {
        this.obj = obj;
        this.pos = pos;
    }

    @Override
    public void run() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(pos);
            oos.writeObject(obj);
            oos.close();
            pos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

class ObjectReader implements Runnable{
    protected Object obj;
    protected PipedInputStream pis;

    public ObjectReader(PipedInputStream pis) {
        this.pis = pis;
    }

    @Override
    public void run() {
        try {
            ObjectInputStream ois = new ObjectInputStream(pis);
            obj = ois.readObject();
            ois.close();
            pis.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}