package code.tiny;

import java.io.*;
import java.util.LinkedList;
import java.util.Map;

public class DistributeReader {
    public static void main(String[] args) throws IOException {
        String file = args[0];
        LinkedList<Map.Entry<Integer, Integer>> list = new LinkedList<>();
        FileReader fr = new FileReader("./distribution/" + file + ".log");
        BufferedReader br = new BufferedReader(fr);
        String line;
        while ((line = br.readLine()) != null) {
            String k = line.substring(line.lastIndexOf("-") + 1, line.lastIndexOf(":"));
            line = br.readLine();
            String size = line.substring(line.lastIndexOf(":") + 1);
            System.out.println(k + "      " + size);
        }
        br.close();
    }
}
