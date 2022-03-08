import javax.swing.JFrame;
import java.util.ArrayList;

public class Graph{

    public Graph(ArrayList<Node> customers, ArrayList<Node> depots, ArrayList<ArrayList<Integer>> routes) {
        Plot plot = new Plot(customers, depots, routes);
        JFrame frame = new JFrame();
        frame.setSize(1000, 1000);
        frame.setTitle("Route");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.getContentPane().add(plot);
        frame.setVisible(true);
    }
}
