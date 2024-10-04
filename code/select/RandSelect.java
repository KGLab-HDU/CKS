package code.select;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import code.graph.Graph;
import code.service.GraphSerializer;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

public class RandSelect {
    private final static Logger logger = Logger.getLogger("InfoLogger");

    /**
     * 随机选择覆盖指定数量的查询节点
     * data=facebook
     * @param args
     * @throws IOException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
        String file = args[0];
        int cover = Integer.parseInt(args[1]);

        FileAppender fileAppender = (FileAppender) logger.getAppender("I");
        fileAppender.setFile("./query/" + file + "_rand" + ".txt");
        fileAppender.activateOptions();
        fileAppender = (FileAppender) Logger.getRootLogger().getAppender("D");
        fileAppender.setFile("./query/" + "$debug_" + file + "_rand" + ".log");
        fileAppender.activateOptions();

        Graph graph = GraphSerializer.antiSerialize("./graph/" + file + ".dat");
        HashSet<Integer> nodeSet = graph.getNodes();
        LinkedList<Integer> nodes = new LinkedList<>();
        nodes.addAll(nodeSet);
        HashSet<Integer> querys = new HashSet<>();
        int count = cover;
        Random rd = new Random();

        while (querys.size() < count) {
            querys.add(nodes.get(rd.nextInt(nodes.size())));
        }

        logger.info(querys);
        logger.debug(querys.size());
    }
}
