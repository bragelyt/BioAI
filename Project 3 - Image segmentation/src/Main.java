import java.util.ArrayList;
import java.util.Arrays;

public class Main {
    static ArrayList<String> dataFile = new ArrayList<>(Arrays.asList("t1", "t2", "t3", "t4", "t5", "t6"));

    public static void main(String[] args) {
        for (String fileName : dataFile){
            NSGA_II nsga = new NSGA_II(fileName);
            nsga.run();
        }
    }
}
