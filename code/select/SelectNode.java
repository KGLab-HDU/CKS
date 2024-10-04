package code.select;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import code.service.QueryNodeSelector;

import java.io.IOException;

/**
 * 获得KM周围所有的1-hop邻居
 */
public class SelectNode {
    private final static Logger logger = Logger.getLogger("InfoLogger");

    /**
     * data=facebook
     * @param args
     * @throws IOException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
        String file = args[0];

        FileAppender fileAppender = (FileAppender) logger.getAppender("I");
        fileAppender.setFile("query/" + file + ".log");
        fileAppender.activateOptions();
        fileAppender = (FileAppender) Logger.getRootLogger().getAppender("D");
        fileAppender.setFile("query/" + "$debug_" + file + ".log");
        fileAppender.activateOptions();

        QueryNodeSelector selectQueryNode = new QueryNodeSelector(file + ".txt");
        logger.info(selectQueryNode.getCandidate());
    }
}
