package code.runnable;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import code.service.GraphFeatureComputer;

import java.io.IOException;

public class GraphFeature {
    private final static Logger logger = Logger.getLogger("InfoLogger");

    /**
     * data=facebook
     * @param args
     * @throws IOException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        String file = args[0];
        FileAppender fileAppender = (FileAppender) logger.getAppender("I");
        fileAppender.setFile("./feature/" + file + ".log");
        fileAppender.activateOptions();
        fileAppender = (FileAppender) Logger.getRootLogger().getAppender("D");
        fileAppender.setFile("./feature/" + "$debug_" + file + ".log");
        fileAppender.activateOptions();

        GraphFeatureComputer graphInfomation = new GraphFeatureComputer(file + ".txt");
        graphInfomation.showInfo();
    }
}
