package code.tiny;

import code.baseline.TDBottomUp;
import code.baseline.UniComponent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

public class ButtomUp {
    public static void main(String[] args) throws IOException {
        TDBottomUp tdBottomUp = new TDBottomUp("artist.txt");
        for (int k = 2; ; k++) {
            File file = new File("./bottomup/BottomUp_" + k + "-truss.txt");
            tdBottomUp.getTruss(k);
            if (tdBottomUp.getG().getNodenum() == 0)
                break;
            UniComponent uniComponent = new UniComponent(tdBottomUp.getG().getGraph());
            HashMap<Integer, HashSet<Integer>> component = uniComponent.getComponent();
            FileWriter fw = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fw);
            component.forEach((i, set) -> {
                set.forEach(node -> {
                    try {
                        bw.write(node + " ");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                try {
                    bw.newLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            bw.close();
            fw.close();
        }
    }
}
