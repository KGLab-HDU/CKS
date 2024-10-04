package code.tiny;

import java.io.*;

public class LowPrecisionRelation {
    public static void main(String[] args) throws IOException {
        String filePath = "./tmpData/data.log";
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        BufferedWriter bw = new BufferedWriter(new FileWriter("./tmpData/new.txt"));

        String line = "";
        while ((line = br.readLine()) != null) {
            String[] strs = line.split(" ");
            double pre = Double.parseDouble(strs[2]);
            if (pre < 0.30) {
                bw.write(line);
                bw.newLine();
            }
        }
        bw.close();
        br.close();
    }
}
