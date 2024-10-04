package code.tiny;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class HeatMapReader {
    public static void main(String[] args) throws IOException {
        String file = args[0];
        String type = args[1];
        String scope = args[2];
        String alpha = args[3];
        String it = args[4];
        String heatFile = file + "_type" + type + "_scope" + scope + "_alpha" + alpha + "_it" + it + ".log";

        BufferedReader br = new BufferedReader(new FileReader("./heatmap/" + heatFile));
        String line = "";
        System.out.print("[");
        int count = 0;
        while ((line = br.readLine()) != null) {
            line = line.substring(15);//固定第15个字符开始是数据起始位置
            String[] dataStr = line.split(" ");

            System.out.print("[" + dataStr[0] + "," + dataStr[1] + "," + dataStr[2] + "],");
            count++;
        }
        System.out.println("];");
        System.out.println(count);
    }
}
