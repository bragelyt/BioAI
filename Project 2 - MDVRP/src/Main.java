import java.lang.reflect.Array;
import java.util.ArrayList;

public class Main {
    //(12), ((13)), (((15))), (16), (18), (19), (((21))), (22)
    static String dataFile = "p12";

    public static void main(String[] args) {
        Train train = new Train(dataFile, 200, 1.0, 834.75, 0.025, 300);
        train.run();
        /*
        ResultTester rt;
            for(int i = 1; i<10; i++){
                rt = new ResultTester("p0"+i);
            }
            for(int j = 10; j<24; j++){
                rt = new ResultTester("p"+j);
            }
         */
    }
}
