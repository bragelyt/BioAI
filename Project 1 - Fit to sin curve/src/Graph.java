import javax.swing.JFrame;
import java.util.ArrayList;

public class Graph{

    public Graph(ArrayList<Double> xCoords, ArrayList<Double> yCoords, int generation, double fit) {
        Plott plott = new Plott(xCoords, yCoords);
        JFrame frame = new JFrame();
        frame.setSize(720, 450);
        frame.setTitle("Sin(x) fit - gen " + generation + " ,avg fit: " + fit);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.getContentPane().add(plott);
        frame.setVisible(true);
    }
}
