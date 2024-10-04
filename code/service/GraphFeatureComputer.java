package code.service;

import org.apache.log4j.Logger;
import code.baseline.TDBottomUp;
import code.graph.Graph;

import java.io.File;
import java.io.IOException;

public class GraphFeatureComputer {
    private Graph G;
    private final static Logger logger = Logger.getLogger("InfoLogger");
    public GraphFeatureComputer(String gFilePath) throws IOException {
        this.G = new Graph(gFilePath);
    }

    public void showInfo() throws IOException, InterruptedException {
        File file = new File(this.G.getgFilePath());
        double size = file.length();
        String[] unit = new String[]{"B", "KB", "MB", "GB", "TB", "EB", "PB"};
        int level = 0;
        while (size / 1024 > 1) {
            level++;
            size /= 1024;
        }
        logger.info("节点数量: " + G.getNodenum());
        logger.info("边数量: " + G.getEdgenum());
        logger.info("文件大小数量: " + size + unit[level]);
        logger.info("最大Degree: " + G.getMaxDeg());
        logger.info("最大Support: " + G.getMaxSup());
        logger.info("三角形数量: " + G.getTriNum());

        TDBottomUp tdButtomUp = new TDBottomUp(this.G.getGraph());
        logger.debug("开始bottomup分解");
        int i = 1;
        do {
            i++;
            tdButtomUp.getTruss(i);
            logger.debug(i + "-truss完成");
        } while(tdButtomUp.getG().getEdgenum() != 0);
        logger.info("最大trussness: " +  (i - 1));
    }
}
